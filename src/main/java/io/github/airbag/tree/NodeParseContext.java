package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

interface NodeParseContext {
    SymbolFormatter symbolFormatter();
    Recognizer<?,?> recognizer();
    RootParseContext root();
    void addChildContext(NodeParseContext childCtx);
    DerivationTree resolve(DerivationTree parent);
}