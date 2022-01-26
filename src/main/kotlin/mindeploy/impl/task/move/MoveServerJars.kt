package mindeploy.impl.task.move

import org.gradle.api.tasks.Internal
import java.io.File

open class MoveServerJars : MoveModJars() {
    @Internal
    override val outputDirectory = File("./run/modded/server/config/mods/")
}