package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

public abstract class QueryElement {

    private final Navigator navigator;

    protected QueryElement(Navigator navigator) {
        this.navigator = navigator;
    }

    protected abstract Predicate<? super DerivationTree> filter();

    public Collection<DerivationTree> evaluate(DerivationTree t) {
        Collection<DerivationTree> collection = switch (navigator) {
            case ROOT -> Collections.singleton(t);
            case DESCENDANTS -> Trees.getDescendants(false, t);
            case ALL -> Trees.getDescendants(true, t);
            case CHILDREN -> t.children();
        };
        return collection.stream().filter(filter()).toList();
    }



    public enum Navigator {
        ROOT, ALL, CHILDREN, DESCENDANTS
    }

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
                    return false;
                }
            };
        }
    }

}