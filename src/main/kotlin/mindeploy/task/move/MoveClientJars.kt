package mindeploy.task.move

import org.gradle.api.tasks.Internal
import java.io.File

open class MoveClientJars : MoveModJars() {
    @Internal
    override val outputDirectory = File("./run/client/mods/")
}