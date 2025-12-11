package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.Recognizer;

interface NodeParseContext {
    SymbolFormatter symbolFormatter();
    PatternFormatter patternFormatter();
    Recognizer<?,?> recognizer();
    RootParseContext root();
    int depth();
    void addChildContext(NodeParseContext childCtx);
    DerivationTree resolve(DerivationTree parent);
    NodeParseContext getParent();
}