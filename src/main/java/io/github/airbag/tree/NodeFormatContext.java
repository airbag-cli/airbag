package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

public class NodeFormatContext {

    //Later final
    private DerivationTree node;
    private final SymbolFormatter symbolFormatter;
    private final Recognizer<?,?> recognizer;
    private final boolean doNotRecurse;



    public NodeFormatContext(SymbolFormatter symbolFormatter, Recognizer<?, ?> recognizer, boolean doNotRecurse) {
        this.symbolFormatter = symbolFormatter;
        this.recognizer = recognizer;
        this.doNotRecurse = doNotRecurse;
    }

    public NodeFormatContext(SymbolFormatter symbolFormatter, Recognizer<?,?> recognizer) {
        this(symbolFormatter, recognizer, false);
    }

    public SymbolFormatter symbolFormatter() {
        return symbolFormatter;
    }

    public DerivationTree node() {
        return node;
    }

    public void setNode(DerivationTree node) {
        this.node = node;
    }

    public Recognizer<?, ?> recognizer() {
        return recognizer;
    }

    public boolean doNotRecurse() {
        return doNotRecurse;
    }
}