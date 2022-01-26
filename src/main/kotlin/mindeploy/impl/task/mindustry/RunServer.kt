package mindeploy.impl.task.mindustry

import java.io.File
import javax.inject.Inject

open class RunServer(dataDirectory: File) : RunMindustry("Server", dataDirectory) {
    @Suppress("unused") // used internally by gradle
    @Inject
    constructor() : this(File("./run/modded/server/"))
}