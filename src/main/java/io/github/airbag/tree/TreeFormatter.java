package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;

import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Map.entry;

public class TreeFormatter {


    public ConcreteSyntaxTree parseCST(CharSequence text) {
        Map<String, BiFunction<? super Node<?>, Object, ? extends Node<?>>> connectors = Map.ofEntries(
                entry("rule", (parent, index) -> ConcreteSyntaxTree.Rule.attachTo(
                        (ConcreteSyntaxTree) parent,
                        (int) index)),
                entry("terminal", (parent, symbol) -> ConcreteSyntaxTree.Terminal.attachTo(
                        (ConcreteSyntaxTree) parent,
                        (Symbol) symbol)),
                entry("error", (parent, symbol) -> ConcreteSyntaxTree.Error.attachTo(
                        (ConcreteSyntaxTree) parent,
                        (Symbol) symbol))
        );
        return null;
    }

}
