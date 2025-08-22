package io.github.airbag.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for implementing the {@link ValidationTree} interface.
 * <p>
 * This class provides a skeletal implementation of the {@link ValidationTree} interface to minimize
 * the effort required to implement it. It manages the parent-child relationships in the validation tree,
 * allowing subclasses to focus on the specific details of the nodes they represent.
 */
public non-sealed abstract class AbstractValidationNode implements ValidationTree {

    /**
     * The parent of this validation node. If this is the root of the validation tree, the parent is this node itself.
     */
    private final AbstractValidationNode parent;

    /**
     * The list of children of this validation node.
     */
    private final List<ValidationTree> children;

    /**
     * Constructs a new AbstractValidationNode with the specified parent.
     *
     * @param parent The parent of this validation node. If null, this node is considered the root.
     */
    protected AbstractValidationNode(ValidationTree parent) {
        children = new ArrayList<>();
        if (parent == null) {
            this.parent = this;
        } else {
            this.parent = (AbstractValidationNode) parent;
            this.parent.children.add(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationTree getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationTree getChild(int i) {
        return children.get(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount() {
        return children.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toStringTree() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRoot() {
        return parent == this;
    }
}
