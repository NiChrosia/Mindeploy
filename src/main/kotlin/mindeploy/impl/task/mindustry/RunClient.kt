package mindeploy.impl.task.mindustry

import java.io.File
import javax.inject.Inject

open class RunClient(dataDirectory: File) : RunMindustry("Client", dataDirectory) {
    @Suppress("unused") // used internally by gradle
    @Inject
    constructor() : this(File("./run/modded/client/"))
}