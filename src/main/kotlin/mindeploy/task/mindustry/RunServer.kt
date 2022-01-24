package mindeploy.task.mindustry

import org.gradle.api.tasks.Internal
import java.io.File

abstract class RunServer : RunMindustry() {
    @Internal
    override val side = "Server"

    @Internal
    override val dataDirectory = File("./run/server/")
}