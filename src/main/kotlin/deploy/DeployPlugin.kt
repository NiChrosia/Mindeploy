package deploy

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Suppress("unused")
open class DeployPlugin : Plugin<Project> {
    open val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    val Project.mindeployCache: File
        get() {
            return File("${project.buildDir}/mindeploy/")
        }

    val Project.runDir: File
        get() {
            return File("${project.projectDir}/run/")
        }

    open fun Project.locateMindustryJar(version: String): File {
        return File("$mindeployCache/Mindustry-$version.jar")
    }

    fun Project.generateMindeployFolders() {
        val libs = File("$buildDir/libs/")

        val run = project.runDir
        val mods = File("$run/mods/")

        if (!libs.exists()) {
            libs.mkdir()
        }

        if (!mindeployCache.exists()) {
            mindeployCache.mkdir()
        }

        if (!run.exists()) {
            run.mkdir()

            if (!mods.exists()) {
                mods.mkdir()
            }
        }
    }

    open fun Project.downloadMindustryRelease(tag: String): File {
        val release = "https://api.github.com/repos/Anuken/Mindustry/releases/tags/$tag".let(::URL)
        val file = File("${mindeployCache}/release.json")

        val releaseChannel = Channels.newChannel(release.openStream())
        val releaseStream = FileOutputStream(file)

        releaseStream.channel.transferFrom(releaseChannel, 0, Long.MAX_VALUE)

        return file
    }

    open fun Project.downloadMindustryJar(version: String, json: JsonObject): File {
        val assets = json["assets"].asJsonArray
        val asset = assets.first {
            if (it is JsonObject) {
                val name = it["name"].asString
                name == "Mindustry.jar"
            } else {
                false
            }
        }.asJsonObject

        val artifact = asset["browser_download_url"].asString.let(::URL)
        val file = locateMindustryJar(version)

        val artifactChannel = Channels.newChannel(artifact.openStream())
        val artifactStream = FileOutputStream(file)

        artifactStream.channel.transferFrom(artifactChannel, 0, Long.MAX_VALUE)

        return file
    }

    override fun apply(project: Project) {
        project.apply<Project> {
            val androidSdkRoot: String by extra
            val androidSdkVersion: String by extra

            val mindustryVersion: String by extra

            generateMindeployFolders()

            tasks.named<Jar>("jar") {
                archiveFileName.set("${project.name}-Desktop.jar")
                duplicatesStrategy = DuplicatesStrategy.INCLUDE

                from(configurations.named("runtimeClasspath").map { configuration ->
                    val tree = files().asFileTree

                    configuration.asFileTree.fold(tree) { collection, file ->
                        if (file.isDirectory) {
                            collection
                        } else {
                            collection.plus(zipTree(file))
                        }
                    }
                })

                from(projectDir) {
                    include("mod.hjson", "mod.json")
                    include("icon.png")
                    include("preview.png")
                }

                outputs.upToDateWhen { false }
            }

            task<Jar>("androidJar") {
                dependsOn("jar")

                archiveFileName.set("${project.name}-Android.jar")

                doLast {
                    val files = mutableListOf<File>()

                    files.addAll(configurations.named("compileClasspath").get().files)
                    files.addAll(configurations.named("runtimeClasspath").get().files)
                    files.add(File("$androidSdkRoot/platforms/android-$androidSdkVersion/android.jar"))

                    val dependencies = files.joinToString(" ") { "--classpath $it" }

                    exec {
                        workingDir = File("${buildDir}/libs/")
                        val command = "d8 $dependencies --min-api 14 --output ${project.name}-Android.jar ${project.name}-Desktop.jar"

                        commandLine(command.split(" "))
                    }
                }

                outputs.upToDateWhen { false }
            }

            task<Jar>("deployMod") {
                dependsOn("jar")

                val desktop = "$buildDir/libs/${project.name}-Desktop.jar"

                from(zipTree(desktop))

                outputs.upToDateWhen { false }
            }

            task<Jar>("deployDexedMod") {
                dependsOn("jar", "androidJar")

                val build = "$buildDir/libs/"
                val desktop = "$build/${project.name}-Desktop.jar"
                val android = "$build/${project.name}-Android.jar"

                from(zipTree(desktop), zipTree(android))

                outputs.upToDateWhen { false }
            }

            task("generateMindeployCache") {
                generateMindeployFolders()

                val libs = buildDir.toPath().resolve("libs/").toFile()
                val cache = buildDir.toPath().resolve("mindeploy/").toFile()

                val mods = runDir.toPath().resolve("mods/").toFile()

                outputs.upToDateWhen { buildDir.exists() && libs.exists() && cache.exists() && runDir.exists() && mods.exists() }
            }

            task("copyModJars") {
                dependsOn("generateMindeployCache", "jar")

                doLast {
                    val buildDirectory = File("${buildDir}/libs/")
                    val modsDirectory = File("${runDir}/mods/")

                    val jars = buildDirectory.listFiles { file -> file.path.endsWith(".jar") }

                    jars?.forEach { jar ->
                        val destination = modsDirectory.toPath().resolve(jar.name)

                        Files.move(jar.toPath(), destination, StandardCopyOption.REPLACE_EXISTING)
                    }
                }

                outputs.upToDateWhen { false }
            }

            task("downloadMindustry") {
                dependsOn("generateMindeployCache")

                val release = downloadMindustryRelease(mindustryVersion)

                val json = gson.fromJson(FileReader(release), JsonObject::class.java)
                downloadMindustryJar(mindustryVersion, json)

                release.delete()

                outputs.upToDateWhen { locateMindustryJar(mindustryVersion).exists() }
            }

            task<JavaExec>("runMindustry") {
                dependsOn("downloadMindustry", "copyModJars")

                val mindustry = locateMindustryJar(mindustryVersion)

                mainClass.set("-jar")
                args(mindustry.path)

                environment["MINDUSTRY_DATA_DIR"] = runDir.path
            }
        }
    }
}