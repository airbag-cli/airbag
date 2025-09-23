package io.github.airbag.tree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A derivation tree is a data structure that represents the derivation of a string from a grammar.
 * It shows how the rules of the grammar are applied to derive the string.
 * This interface is the base interface for all derivation trees, defining the fundamental
 * properties and behaviors of a node within a tree structure.
 *
 * @param <T> The type of the nodes in the tree, following the Curiously Recurring Template Pattern (CRTP)
 *            to ensure type safety in the tree's methods.
 */
public interface DerivationTree<T extends DerivationTree<T>> {

    /**
     * Returns the index of this node. For a rule node, this is the rule index. For a terminal node,
     * this is the token type.
     *
     * @return The integer index of the node.
     */
    int index();

    /**
     * Returns this tree as a node.
     *
     * @return this tree as a node.
     */
    Node<T> toNode();

    /**
     * Get the distance to root
     *
     * @return the distance to root
     */
    default int depth() {
        int depth = 0;
        Node<?> node = toNode();
        while (node.getParent() != node) {
            node = node.getParent();
            depth++;
        }
        return depth;
    }

    /**
     * The biggest distance to any subnode
     *
     * @return the biggest distance to any subnode
     */
    default int height() {
        Set<Node<?>> terminalNodes = new HashSet<>(toNode().children());
        while (terminalNodes.stream().anyMatch(node -> node.size() != 0)) {
            for (var node : terminalNodes) {
                if (node.size() != 0) {
                    terminalNodes.remove(node);
                    terminalNodes.addAll(node.children());
                }
            }
        }
        int maxDepth = terminalNodes.stream()
                .map(DerivationTree::depth)
                .max(Integer::compareTo)
                .orElse(0);
        return maxDepth - depth();
    }
}