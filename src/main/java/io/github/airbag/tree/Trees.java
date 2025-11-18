package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;

public class Trees {

    private Trees() {
    }

    public static List<DerivationTree> getDescendants(boolean includeRoot, DerivationTree t) {
        List<DerivationTree> accumulator = new ArrayList<>();
        getDescendants(includeRoot, t, accumulator);
        return accumulator;
    }

    private static void getDescendants(boolean includeRoot,
                                      DerivationTree t,
                                      List<DerivationTree> accumulator) {
        if (includeRoot) {
            accumulator.add(t);
        }
        for (var child : t.children()) {
            getDescendants(true, child, accumulator);
        }
    }

    public static List<Symbol> getSymbols(DerivationTree t) {
        List<DerivationTree> descendants = getDescendants(true, t);
        List<Symbol> symbols = new ArrayList<>();
        for (var node : descendants) {
            if (node instanceof DerivationTree.Terminal terminal) {
                symbols.add(terminal.symbol());
            }
        }
        return symbols;
    }

}