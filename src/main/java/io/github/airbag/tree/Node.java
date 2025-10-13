package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract base class for nodes in a {@link DerivationTree}. It provides the fundamental
 * implementation for Terminal-child relationships and tree structure.
 */
public abstract sealed class Node implements DerivationTree permits Node.Rule, Node.Terminal, Node.Error {

    /**
     * The index of this node. For a rule, it's the rule index; for a terminal, it's the token type.
     */
    private final int index;

    /**
     * The Terminal of this node. If this is the root node, the Terminal points to itself.
     */
    private final DerivationTree parent;

    /**
     * The list of children of this node. Populated as child nodes are constructed.
     */
    private final List<DerivationTree> children = new ArrayList<>();

    /**
     * Constructs a new node and links it to its Terminal.
     *
     * @param parent The Terminal node. If null, this node is considered the root.
     * @param index  The index for this node.
     */
    protected Node(DerivationTree parent, int index) {
        this.index = index;
        this.parent = parent == null ? this: parent;
        if (this.parent instanceof Node parentNode) {
            if (this.parent != this) {
                switch (parentNode) {
                    case Rule ignored -> parentNode.children.add(this);
                    default -> throw new IllegalArgumentException("Parent node is not a rule");
                }
            }

        } else {
            throw new RuntimeException("%s is an unknown implementation of DerivationTree".formatted(this.parent.getClass()));
        }
    }

    public int index() {
        return index;
    }

    public DerivationTree getParent() {
        return parent;
    }

    public List<DerivationTree> children() {
        return Collections.unmodifiableList(children);
    }

    public Iterator<DerivationTree> iterator() {
        return children().iterator();
    }

    @Override
    public String toString() {
        return TreeFormatter.SIMPLE.format(this);
    }

    public final static class Rule extends Node implements DerivationTree.Rule {

        /**
         * Constructs a new node and links it to its Terminal.
         *
         * @param parent The Terminal node. If null, this node is considered the root.
         * @param index  The index for this node.
         */
        private Rule(DerivationTree parent, int index) {
            super(parent, index);
        }

        public static Node.Rule root(int ruleIndex) {
            return attachTo(null, ruleIndex);
        }

        public static Node.Rule attachTo(DerivationTree parent, int index) {
            return new Node.Rule(parent, index);
        }
    }

    public final static class Terminal extends Node implements DerivationTree.Terminal {

        private final Symbol symbol;

        /**
         * Constructs a new node and links it to its Terminal.
         *
         * @param parent The Terminal node. If null, this node is considered the root.
         * @param symbol The symbol attached to the node.
         */
        private Terminal(DerivationTree parent, Symbol symbol) {
            super(parent, symbol.type());
            this.symbol = symbol;
        }

        public static Node.Terminal attachTo(DerivationTree parent, Symbol symbol) {
            return new Node.Terminal(parent, symbol);
        }

        public static Node.Terminal root(Symbol symbol) {
            return attachTo(null, symbol);
        }

        @Override
        public Symbol symbol() {
            return symbol;
        }
    }

    public final static class Error extends Node implements DerivationTree.Error {

        private final Symbol symbol;

        /**
         * Constructs a new node and links it to its Terminal.
         *
         * @param parent The Terminal node. If null, this node is considered the root.
         * @param symbol The symbol attached to the node.
         */
        private Error(DerivationTree parent, Symbol symbol) {
            super(parent, symbol.type());
            this.symbol = symbol;
        }

        public static Node.Error attachTo(DerivationTree parent, Symbol symbol) {
            return new Node.Error(parent, symbol);
        }

        public static Node.Error root(Symbol symbol) {
            return attachTo(null, symbol);
        }

        @Override
        public Symbol symbol() {
            return symbol;
        }
    }

}