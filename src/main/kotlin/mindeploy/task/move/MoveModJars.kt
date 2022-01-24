package mindeploy.task.move

import org.gradle.api.DefaultTask
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class MoveModJars : DefaultTask() {
    abstract val outputDirectory: File

    init {
        dependsOn("generateMindeployCache", "jar")

        doLast {
            val buildDirectory = File("${project.buildDir}/libs/")

            val jars = buildDirectory.listFiles { file -> file.path.endsWith(".jar") }

            jars?.forEach { jar ->
                val destination = outputDirectory.toPath().resolve(jar.name)

                Files.move(jar.toPath(), destination, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        outputs.upToDateWhen { false }
    }
}