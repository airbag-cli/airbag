package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.w3c.dom.xpath.XPathResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A node in a derivation tree. A node can be a rule, a terminal, an error, or a pattern.
 * Nodes are the building blocks of {@link ConcreteSyntaxTree} and {@link ValidationSyntaxTree}.
 */
public abstract sealed class Node implements Iterable<Node> permits Node.Rule, Node.Terminal, Node.Error, Node.Pattern  {

    private final Node.Rule parent;
    private final int index;

    protected Node(Node parent, int index) {
        switch (parent) {
            case Rule rule -> {
                this.parent = rule;
                rule.children.add(this);
            }
            case null -> this.parent = null;
            default -> throw new IllegalArgumentException("Node %s cannot have children".formatted(parent));
        }
        this.index = index;
    }

    public int index() {
        return index;
    }

    public Node.Rule getParent() {
        return parent;
    }

    public int size() {
        return children().size();
    }

    public List<Node> children() {
        return Collections.emptyList();
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * A rule node represents a rule in the grammar.
     */
    public static final class Rule extends Node implements ConcreteSyntaxTree, ValidationSyntaxTree {

        private final List<Node> children = new ArrayList<>();

        public Rule(Node parent, int index) {
            super(parent, index);
        }

        @Override
        public List<Node> children() {
            return Collections.unmodifiableList(children);
        }

        @Override
        public Iterator<Node> iterator() {
            return children().iterator();
        }
    }

    /**
     * A terminal node represents a terminal symbol in the grammar.
     */
    public static final class Terminal extends Node implements ConcreteSyntaxTree, ValidationSyntaxTree {

        public Terminal(Node parent, Symbol symbol) {
            super(parent, symbol.index());
        }


    }

    /**
     * An error node represents a syntax error.
     */
    public static final class Error extends Node implements ConcreteSyntaxTree, ValidationSyntaxTree {

        public Error(Node parent, Symbol symbol) {
            super(parent, symbol.index());
        }
    }

    /**
     * A pattern node represents a pattern in the grammar.
     */
    public static final class Pattern extends Node implements ValidationSyntaxTree {

        public Pattern(Node parent, ParseTreePattern pattern) {
            super(parent, pattern.getPatternRuleIndex());
        }

    }

}