package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

public class NodeFormatContext {

    //Later final
    private DerivationTree node;
    private final SymbolFormatter symbolFormatter;
    private final Recognizer<?,?> recognizer;

    public NodeFormatContext(SymbolFormatter symbolFormatter, Recognizer<?, ?> recognizer) {
        this.symbolFormatter = symbolFormatter;
        this.recognizer = recognizer;
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
}