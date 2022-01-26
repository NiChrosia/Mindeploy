package mindeploy.impl.tree

import mindeploy.dsl.tree.toRecursiveList
import java.io.File
import java.nio.file.Path

data class Node<T>(val parent: Node<T>?, val content: T, val children: MutableList<Node<T>>) : Collection<T> {
    override val size: Int
        get() = children.sumOf { it.size }

    val depth: Int
        get() = parent?.depth?.let { it + 1 } ?: 0

    override fun contains(element: T): Boolean {
        return children.any { it.contains(element) }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun isEmpty(): Boolean {
        return children.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return toRecursiveList().iterator()
    }

    override fun toString(): String {
        val base = when(content) {
            is Path -> content.toFile().name
            is File -> content.name
            else -> content.toString()
        }

        return base + children.fold("") { accumulative, node ->
            val hyphens = "\t".repeat(node.depth)
            val spacing = "\n $hyphens "

            accumulative + spacing + node
        }
    }
}