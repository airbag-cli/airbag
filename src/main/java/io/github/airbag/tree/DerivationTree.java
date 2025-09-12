package io.github.airbag.tree;

import java.util.Iterator;
import java.util.List;

/**
 * A derivation tree is a data structure that represents the derivation of a string from a grammar.
 * It shows how the rules of the grammar are applied to derive the string.
 * This interface is the base interface for all derivation trees, defining the fundamental
 * properties and behaviors of a node within a tree structure.
 *
 * @param <T> The type of the nodes in the tree, following the Curiously Recurring Template Pattern (CRTP)
 *           to ensure type safety in the tree's methods.
 */
public interface DerivationTree<T extends DerivationTree<T>> extends Iterable<T>  {

    /**
     * Returns the index of this node. For a rule node, this is the rule index. For a terminal node,
     * this is the token type.
     *
     * @return The integer index of the node.
     */
    int index();

    /**
     * Retrieves the parent node of this node in the tree.
     *
     * @return The parent node, or the node itself if it is the root of the tree.
     */
    T getParent();

    /**
     * Retrieves the list of children of this node.
     *
     * @return An unmodifiable list of child nodes. Returns an empty list if this is a leaf node.
     */
    List<T> children();

    /**
     * Retrieves the child node at the specified position in this node's children list.
     *
     * @param i The index of the child to return.
     * @return The child node at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()).
     */
    default T getChild(int i) {
        return children().get(i);
    }

    /**
     * Returns the number of children of this node.
     *
     * @return The number of child nodes.
     */
    default int size() {
        return children().size();
    }

    /**
     * Returns an iterator over the children of this node. This allows the node's children
     * to be iterated over in a for-each loop.
     *
     * @return An iterator for the list of child nodes.
     */
    @Override
    default Iterator<T> iterator() {
        return children().iterator();
    }
}
