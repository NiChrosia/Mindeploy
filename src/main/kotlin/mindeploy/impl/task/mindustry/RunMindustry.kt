package mindeploy.impl.task.mindustry

import mindeploy.Mindeploy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File

abstract class RunMindustry(@Internal open val side: String, @Internal open val dataDirectory: File) : JavaExec() {
    init {
        dependsOn("downloadMindustry")

        val mindustryVersion: String by project.extra
        val mindustry = File("${project.buildDir}/${Mindeploy.name}/Mindustry-$side-$mindustryVersion.jar")

        mainClass.set("-jar")
        args(mindustry.path)

        environment["MINDUSTRY_DATA_DIR"] = dataDirectory
    }
}