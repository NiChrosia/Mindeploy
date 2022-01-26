package mindeploy.dsl.tree

import mindeploy.impl.tree.Node
import java.io.File

fun Node<File>.child(name: String, block: Node<File>.() -> Unit = {}): Node<File> {
    return child(content.toPath().resolve(name).toFile(), block)
}