package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.TreePatternFormatter;
import org.antlr.v4.runtime.Recognizer;

interface NodeParseContext {
    SymbolFormatter symbolFormatter();
    TreePatternFormatter patternFormatter();
    Recognizer<?,?> recognizer();
    RootParseContext root();
    int depth();
    void addChildContext(NodeParseContext childCtx);
    DerivationTree resolve(DerivationTree parent);
    NodeParseContext getParent();
}