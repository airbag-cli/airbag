package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;

/**
 * Represents a node in a derivation tree. This interface is the base for different types of nodes
 * that can appear in a tree, such as rules, terminals, errors, and patterns.
 *
 * @param <T> The type of the nodes in the tree, following the CRTP pattern.
 */
public interface Node<T extends DerivationTree<T>> extends DerivationTree<T> {

    /**
     * A marker interface representing a rule node in the tree. A rule node corresponds to a
     * non-terminal symbol in the grammar and has children that represent the production of that rule.
     *
     * @param <T> The type of the nodes in the tree.
     */
    interface Rule<T extends DerivationTree<T>> extends Node<T> {

    }

    /**
     * Represents a terminal node in the tree. A terminal node corresponds to a terminal symbol
     * (e.g., a token) in the grammar and has no children.
     *
     * @param <T> The type of the nodes in the tree.
     */
    interface Terminal<T extends DerivationTree<T>> extends Node<T> {

        /**
         * Gets the underlying symbol associated with this terminal node.
         *
         * @return The {@link Symbol} representing the token.
         */
        Symbol getSymbol();
    }

    /**
     * Represents an error node in the tree. An error node indicates a syntax error at a particular
     * point in the input string.
     *
     * @param <T> The type of the nodes in the tree.
     */
    interface Error<T extends DerivationTree<T>> extends Node<T> {

        /**
         * Gets the underlying symbol associated with this error node.
         *
         * @return The {@link Symbol} where the error was detected.
         */
        Symbol getSymbol();
    }

    /**
     * Represents a pattern node in the tree. This can be used to represent repeated patterns
     * or other structural elements not directly corresponding to a single grammar rule.
     *
     * @param <T> The type of the nodes in the tree.
     */
    interface Pattern<T extends DerivationTree<T>> extends Node<T> {

    }
}