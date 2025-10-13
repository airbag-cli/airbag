package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

class RootParseContext implements NodeParseContext{

    private final SymbolFormatter symbolFormatter;
    private final Recognizer<?,?> recognizer;
    private NodeParseContext root;

    RootParseContext(SymbolFormatter symbolFormatter, Recognizer<?, ?> recognizer) {
        this.symbolFormatter = symbolFormatter;
        this.recognizer = recognizer;
    }

    @Override
    public SymbolFormatter symbolFormatter() {
        return symbolFormatter;
    }

    @Override
    public Recognizer<?, ?> recognizer() {
        return recognizer;
    }

    @Override
    public RootParseContext root() {
        return this;
    }

    @Override
    public void addChildContext(NodeParseContext childCtx) {
        if (root != null) {
            throw new RuntimeException("Can have only a single root node");
        }
        root = childCtx;
    }

    @Override
    public DerivationTree resolve(DerivationTree parent) {
        return root.resolve(parent);
    }

    public DerivationTree resolve() {
        return root.resolve(null);
    }

    class Rule implements NodeParseContext {

        private int index;
        private final List<NodeParseContext> children = new ArrayList<>();

        public int index() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        @Override
        public SymbolFormatter symbolFormatter() {
            return symbolFormatter;
        }

        @Override
        public Recognizer<?, ?> recognizer() {
            return recognizer;
        }

        @Override
        public RootParseContext root() {
            return RootParseContext.this.root();
        }

        @Override
        public void addChildContext(NodeParseContext childCtx) {
            children.add(childCtx);
        }

        @Override
        public DerivationTree resolve(DerivationTree parent) {
            DerivationTree rule = Node.Rule.attachTo(parent, index);
            for (var childCtx : children) {
                childCtx.resolve(rule);
            }
            return rule;
        }
    }

    class Terminal implements NodeParseContext {

        private Symbol symbol;

        public Symbol getSymbol() {
            return symbol;
        }

        public void setSymbol(Symbol symbol) {
            this.symbol = symbol;
        }

        @Override
        public SymbolFormatter symbolFormatter() {
            return symbolFormatter;
        }

        @Override
        public Recognizer<?, ?> recognizer() {
            return recognizer;
        }

        @Override
        public RootParseContext root() {
            return RootParseContext.this.root();
        }

        @Override
        public void addChildContext(NodeParseContext childCtx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DerivationTree resolve(DerivationTree parent) {
            return Node.Terminal.attachTo(parent, symbol);
        }
    }

    class Error implements NodeParseContext {

        private Symbol symbol;

        public Symbol getSymbol() {
            return symbol;
        }

        public void setSymbol(Symbol symbol) {
            this.symbol = symbol;
        }

        @Override
        public SymbolFormatter symbolFormatter() {
            return symbolFormatter;
        }

        @Override
        public Recognizer<?, ?> recognizer() {
            return recognizer;
        }

        @Override
        public RootParseContext root() {
            return RootParseContext.this.root();
        }

        @Override
        public void addChildContext(NodeParseContext childCtx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DerivationTree resolve(DerivationTree parent) {
            return Node.Error.attachTo(parent, symbol);
        }
    }
}