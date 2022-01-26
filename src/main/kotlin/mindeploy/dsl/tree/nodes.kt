package mindeploy.dsl.tree

import mindeploy.impl.tree.Node

fun <T> treeOf(root: T, block: Node<T>.() -> Unit): Node<T> {
    val node = Node(null, root, mutableListOf())

    return node.apply(block)
}

fun <T> Node<T>.child(content: T, block: Node<T>.() -> Unit = {}): Node<T> {
    val node = Node(this, content, mutableListOf())
    children.add(node)

    return node.apply(block)
}

fun <T> Node<T>.toRecursiveList(): List<T> {
    val all = children.map { it.toRecursiveList() }.flatten().toMutableList()
    all.add(content)

    return all
}