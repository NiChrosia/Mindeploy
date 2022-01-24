package mindeploy

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import mindeploy.task.mindustry.RunClient
import mindeploy.task.mindustry.RunServer
import mindeploy.task.move.MoveClientJars
import mindeploy.task.move.MoveServerJars
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.task
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.net.URL
import java.nio.channels.Channels
import kotlin.collections.first
import kotlin.collections.fold
import kotlin.collections.joinToString
import kotlin.collections.mutableListOf

@Suppress("unused")
open class Mindeploy : Plugin<Project> {
    open val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    val Project.mindeployCache: File
        get() {
            return File("${project.buildDir}/${Mindeploy.name}/")
        }

    val Project.runDir: File
        get() {
            return File("${project.projectDir}/run/")
        }

    open fun download(link: URL, file: File): File {
        val channel = Channels.newChannel(link.openStream())
        val stream = FileOutputStream(file)

        stream.channel.transferFrom(channel, 0, Long.MAX_VALUE)

        return file
    }

    /** @param side The network side of the jar file. Accepts either `Client` or `Server`. */
    open fun Project.locateMindustryJar(side: String, version: String): File {
        return File("$mindeployCache/Mindustry-$side-$version.jar")
    }

    open fun Project.locateMindustryJars(version: String): List<File> {
        return listOf(locateMindustryJar("Client", version), locateMindustryJar("Server", version))
    }

    /** @return whether all necessary folders exist */
    fun Project.generateMindeployFolders(): Boolean {
        val libs = File("$buildDir/libs/")

        val run = project.runDir

        val clientRun = run.toPath().resolve("client").toFile()
        val clientMods = clientRun.toPath().resolve("mods").toFile()

        val serverRun = run.toPath().resolve("server").toFile()
        val serverMods = serverRun.toPath().resolve("config").resolve("mods").toFile()

        if (!libs.exists()) {
            libs.mkdirs()
        }

        if (!mindeployCache.exists()) {
            mindeployCache.mkdirs()
        }

        if (!run.exists()) {
            run.mkdirs()

            if (!clientRun.exists()) {
                clientRun.mkdirs()

                if (!clientMods.exists()) {
                    clientMods.mkdirs()
                }
            }

            if (!serverRun.exists()) {
                serverRun.mkdirs()

                if (!serverMods.exists()) {
                    serverMods.mkdirs()
                }
            }
        }

        val folders = listOf(
            libs,
            run,
            clientRun,
            clientMods,
            serverRun,
            serverMods
        )

        return folders.all { it.exists() }
    }

    open fun Project.downloadMindustryRelease(tag: String): File {
        val link = "https://api.github.com/repos/Anuken/Mindustry/releases/tags/$tag".let(::URL)
        val file = File("${mindeployCache}/release.json")

        return download(link, file)
    }

    open fun Project.downloadMindustryJars(version: String, json: JsonObject): List<File> {
        val assets = json["assets"].asJsonArray

        val jsonAssets = assets.filterIsInstance<JsonObject>()
        val client = jsonAssets.first { it["name"].asString == "Mindustry.jar" }
        val server = jsonAssets.first { it["name"].asString == "server-release.jar" }

        val clientLink = client["browser_download_url"].asString.let(::URL)
        val clientJar = locateMindustryJar("Client", version)

        val serverLink = server["browser_download_url"].asString.let(::URL)
        val serverJar = locateMindustryJar("Server", version)

        download(clientLink, clientJar)
        download(serverLink, serverJar)

        return listOf(clientJar, serverJar)
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
                    include("mod.hjson", "mod.json", "plugin.hjson", "plugin.json")
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
                val upToDate = generateMindeployFolders()

                outputs.upToDateWhen { upToDate }
            }

            task<MoveClientJars>("moveClientJars")
            task<MoveServerJars>("moveServerJars")

            task("downloadMindustry") {
                dependsOn("generateMindeployCache")

                val release = downloadMindustryRelease(mindustryVersion)

                val json = gson.fromJson(FileReader(release), JsonObject::class.java)
                downloadMindustryJars(mindustryVersion, json)

                release.delete()

                outputs.upToDateWhen { locateMindustryJars(mindustryVersion).all { it.exists() } }
            }

            task<RunClient>("runClient") {
                dependsOn("moveClientJars")
            }

            task<RunServer>("runServer") {
                dependsOn("moveServerJars")
            }
        }
    }

    companion object {
        const val name = "mindeploy"
    }
}