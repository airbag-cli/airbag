package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.TreePatternFormatter;
import org.antlr.v4.runtime.Recognizer;

public class NodeFormatContext {

    //Later final
    private DerivationTree node;
    private final SymbolFormatter symbolFormatter;
    private final TreePatternFormatter patternFormatter;
    private final Recognizer<?,?> recognizer;
    private final boolean doNotRecurse;



    public NodeFormatContext(SymbolFormatter symbolFormatter, TreePatternFormatter patternFormatter, Recognizer<?, ?> recognizer, boolean doNotRecurse) {
        this.symbolFormatter = symbolFormatter;
        this.patternFormatter = patternFormatter;
        this.recognizer = recognizer;
        this.doNotRecurse = doNotRecurse;
    }

    public NodeFormatContext(SymbolFormatter symbolFormatter, TreePatternFormatter patternFormatter, Recognizer<?,?> recognizer) {
        this(symbolFormatter, patternFormatter, recognizer, false);
    }

    public SymbolFormatter symbolFormatter() {
        return symbolFormatter;
    }

    public TreePatternFormatter patternFormatter() {
        return patternFormatter;
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