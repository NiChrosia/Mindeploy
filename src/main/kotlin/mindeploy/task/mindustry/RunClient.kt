package mindeploy.task.mindustry

import org.gradle.api.tasks.Internal
import java.io.File

abstract class RunClient : RunMindustry() {
    @Internal
    override val side = "Client"

    @Internal
    override val dataDirectory = File("./run/client/")
}