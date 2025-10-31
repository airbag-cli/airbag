package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
            case DESCENDANTS -> getDescendants(false, t);
            case ALL -> getDescendants(true, t);
            case CHILDREN -> t.children();
        };
        return collection.stream().filter(filter()).toList();
    }

    public static List<DerivationTree> getDescendants(boolean includeRoot, DerivationTree t) {
        List<DerivationTree> accumulator = new ArrayList<>();
        getDescendants(includeRoot, t, accumulator);
        return accumulator;
    }

    public static void getDescendants(boolean includeRoot,
                                      DerivationTree t,
                                      List<DerivationTree> accumulator) {
        if (includeRoot) {
            accumulator.add(t);
        }
        for (var child : t.children()) {
            getDescendants(true, child, accumulator);
        }
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