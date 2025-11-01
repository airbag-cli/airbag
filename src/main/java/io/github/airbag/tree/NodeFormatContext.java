package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Parser;

public class NodeFormatContext {

    //Later final
    private DerivationTree node;
    private final SymbolFormatter symbolFormatter;
    private final Parser parser;

    public NodeFormatContext(SymbolFormatter symbolFormatter, Parser parser) {
        this.symbolFormatter = symbolFormatter;
        this.parser = parser;
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

    public Parser recognizer() {
        return parser;
    }
}