package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Parser;

interface NodeParseContext {
    SymbolFormatter symbolFormatter();
    Parser parser();
    RootParseContext root();
    int depth();
    void addChildContext(NodeParseContext childCtx);
    DerivationTree resolve(DerivationTree parent);
    NodeParseContext getParent();
}