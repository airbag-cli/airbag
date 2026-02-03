package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.*;

class RootParseContext implements NodeParseContext{

    private final SymbolFormatter symbolFormatter;
    private final PatternFormatter patternFormatter;
    private final Recognizer<?,?> recognizer;
    private NodeParseContext root;
    private int maxError;
    private final Set<String> errorMessages = new TreeSet<>();

    RootParseContext(SymbolFormatter symbolFormatter, PatternFormatter patternFormatter, Recognizer<?, ?> recognizer) {
        this.symbolFormatter = symbolFormatter;
        this.patternFormatter = patternFormatter;
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

    @Override
    public NodeParseContext getParent() {
        return this;
    }

    @Override
    public int depth() {
        return -1;
    }

    public int getMaxError() {
        return maxError;
    }

    public String getErrorMessages() {
        StringJoiner joiner = new StringJoiner("%n".formatted());
        errorMessages.forEach(joiner::add);
        return joiner.toString();
    }

    public void recordError(int errorIndex, String message) {
        if (errorIndex > maxError) {
            maxError = errorIndex;
            errorMessages.clear();
            errorMessages.add(message);
        } else if (errorIndex == maxError){
            errorMessages.add(message);
        }
    }

    @Override
    public PatternFormatter patternFormatter() {
        return patternFormatter;
    }

    class Rule implements NodeParseContext {

        private int index;
        private final NodeParseContext parent;
        private final List<NodeParseContext> children = new ArrayList<>();

        public Rule(NodeParseContext parent) {
            this.parent = parent;
        }

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
        public PatternFormatter patternFormatter() {
            return patternFormatter;
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

        @Override
        public NodeParseContext getParent() {
            return parent;
        }

        @Override
        public int depth() {
            return 1 + parent.depth();
        }
    }

    class Terminal implements NodeParseContext {

        private final NodeParseContext parent;
        private Symbol symbol;

        public Terminal(NodeParseContext parent) {
            this.parent = parent;
        }

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
        public PatternFormatter patternFormatter() {
            return patternFormatter;
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

        @Override
        public NodeParseContext getParent() {
            return parent;
        }

        @Override
        public int depth() {
            return 1 + parent.depth();
        }
    }

    class Error implements NodeParseContext {

        private final NodeParseContext parent;
        private Symbol symbol;

        public Error(NodeParseContext parent) {
            this.parent = parent;
        }

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
        public PatternFormatter patternFormatter() {
            return patternFormatter;
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

        @Override
        public NodeParseContext getParent() {
            return parent;
        }

        @Override
        public int depth() {
            return 1 + parent.depth();
        }
    }

    class Pattern implements NodeParseContext {

        private final NodeParseContext parent;
        private int index;
        private io.github.airbag.tree.pattern.Pattern pattern;

        Pattern(NodeParseContext parent) {
            this.parent = parent;
        }

        @Override
        public SymbolFormatter symbolFormatter() {
            return symbolFormatter;
        }

        @Override
        public PatternFormatter patternFormatter() {
            return patternFormatter;
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
        public int depth() {
            return 1 + parent.depth();
        }

        @Override
        public void addChildContext(NodeParseContext childCtx) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DerivationTree resolve(DerivationTree parent) {
            return Node.Pattern.attachTo(parent ,index, pattern);
        }

        @Override
        public NodeParseContext getParent() {
            return parent;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setPattern(io.github.airbag.tree.pattern.Pattern pattern) {
            this.pattern = pattern;
        }
    }
}