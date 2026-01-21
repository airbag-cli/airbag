package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * An abstract base class representing a single step or component within a {@link Query}.
 * Each {@code QueryElement} defines how to navigate through a {@link DerivationTree}
 * and a condition to filter matching nodes.
 * This class and its implementations are primarily for internal use by {@link QueryProvider}.
 */
public abstract class QueryElement {

    private final Navigator navigator;

    /**
     * Constructs a {@code QueryElement} with a specified navigation strategy.
     * @param navigator The navigation strategy for this element.
     */
    protected QueryElement(Navigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Returns a {@link Predicate} that defines the filtering condition for this query element.
     * Subclasses must implement this method to specify what kind of {@link DerivationTree} nodes they match.
     * @return A predicate for filtering {@link DerivationTree} nodes.
     */
    protected abstract Predicate<? super DerivationTree> filter();

    /**
     * Evaluates this query element against a given {@link DerivationTree}.
     * It first applies the navigation strategy to the input tree to get a collection of candidate nodes,
     * and then filters these candidates using the element's specific filter predicate.
     * @param t The {@link DerivationTree} to evaluate this element against.
     * @return A collection of {@link DerivationTree} nodes that match this element's criteria.
     */
    public Collection<DerivationTree> evaluate(DerivationTree t) {
        Collection<DerivationTree> collection = switch (navigator) {
            case ROOT -> Collections.singleton(t);
            case DESCENDANTS -> Trees.getDescendants(false, t);
            case ALL -> Trees.getDescendants(true, t);
            case CHILDREN -> t.children();
        };
        return collection.stream().filter(filter()).toList();
    }


    /**
     * Defines the different navigation strategies available for a {@link QueryElement}.
     * These strategies dictate how to traverse from a given {@link DerivationTree} node
     * to find potential matches.
     */
    public enum Navigator {
        /** Start from the root of the given tree. */
        ROOT,
        /** Include all descendants (including the given tree itself). */
        ALL,
        /** Include only direct children of the given tree. */
        CHILDREN,
        /** Include all descendants (excluding the given tree itself). */
        DESCENDANTS
    }

    /**
     * A {@link QueryElement} that acts as a wildcard, matching any {@link DerivationTree} node.
     */
    static class Wildcard extends QueryElement {

        private final boolean isInverted;

        protected Wildcard(Navigator navigator, boolean isInverted) {
            super(navigator);
            this.isInverted = isInverted;
        }

        @Override
        protected Predicate<? super DerivationTree> filter() {
            return t -> !isInverted;
        }
    }

    /**
     * A {@link QueryElement} that matches a {@link DerivationTree.Rule} node with a specific rule index.
     */
    static class Rule extends QueryElement {

        private final boolean isInverted;
        private final int ruleIndex;

        Rule(Navigator navigator, boolean isInverted, int ruleIndex) {
            super(navigator);
            this.isInverted = isInverted;
            this.ruleIndex = ruleIndex;
        }

        @Override
        protected Predicate<? super DerivationTree> filter() {
            return t -> {
                if (t instanceof DerivationTree.Rule ruleNode) {
                    return !isInverted && (ruleIndex == ruleNode.index()) ||
                           (isInverted && (ruleIndex != ruleNode.index()));
                } else {
                    return false;
                }
            };
        }
    }

    /**
     * A {@link QueryElement} that matches a {@link DerivationTree.Terminal} node with a specific token type.
     */
    static class Token extends QueryElement {
        private final boolean isInverted;
        private final int tokenType;

        Token(Navigator navigator, boolean isInverted, int tokenType) {
            super(navigator);
            this.isInverted = isInverted;
            this.tokenType = tokenType;
        }

        @Override
        protected Predicate<? super DerivationTree> filter() {
            return t -> {
                if (t instanceof DerivationTree.Terminal terminal) {
                    return !isInverted && (tokenType == terminal.index()) ||
                           (isInverted && (tokenType != terminal.index()));
                } else {
                    return isInverted;
                }
            };
        }
    }

}