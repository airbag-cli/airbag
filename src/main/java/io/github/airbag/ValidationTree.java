package io.github.airbag;

import org.antlr.v4.runtime.tree.Tree;

/**
 * Represents a validation tree for an ANTLR parse tree.
 * <p>
 * A validation tree is a tree structure that can be used to validate the structure of an ANTLR parse tree.
 * It is a simplified representation of the parse tree, containing only the information necessary for validation.
 * <p>
 * This interface is sealed and permits only {@link AbstractValidationNode} to implement it.
 */
public sealed interface ValidationTree extends Tree permits AbstractValidationNode {

    /**
     * {@inheritDoc}
     */
    @Override
    ValidationTree getParent();

    /**
     * {@inheritDoc}
     */
    @Override
    ValidationTree getChild(int i);

    /**
     * Return {@code true} if the node has itself as parent.
     * @return {@code true} if the node has itself as parent.
     */
    boolean isRoot();
}
