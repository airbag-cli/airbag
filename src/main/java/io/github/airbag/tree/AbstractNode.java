package io.github.airbag.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract base class for nodes in a {@link DerivationTree}. It provides the fundamental
 * implementation for parent-child relationships and tree structure.
 *
 * @param <T> The type of the nodes in the tree, following the CRTP pattern.
 */
public abstract class AbstractNode<T extends DerivationTree<T>> implements Node<T> {

    /**
     * The index of this node. For a rule, it's the rule index; for a terminal, it's the token type.
     */
    private final int index;

    /**
     * The parent of this node. If this is the root node, the parent points to itself.
     */
    private final T parent;

    /**
     * The list of children of this node. Populated as child nodes are constructed.
     */
    private final List<T> children = new ArrayList<>();

    /**
     * Constructs a new node and links it to its parent.
     *
     * @param parent The parent node. If null, this node is considered the root.
     * @param index  The index for this node.
     */
    protected AbstractNode(AbstractNode<T> parent, int index) {
        this.index = index;
        this.parent = parent == null ? self() : parent.self();
        if (parent != null) {
            parent.children.add(self());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int index() {
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> children() {
        return Collections.unmodifiableList(children);
    }

    /**
     * A method required by the CRTP to get the correctly typed `this` reference.
     *
     * @return The instance of the concrete subclass.
     */
    protected abstract T self();

}