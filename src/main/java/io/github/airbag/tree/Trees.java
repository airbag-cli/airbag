package io.github.airbag.tree;

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

    public static void getDescendants(boolean includeRoot,
                                      DerivationTree t,
                                      List<DerivationTree> accumulator) {
        if (includeRoot) {
            accumulator.add(t);
        }
        for (var child : t.children()) {
            getDescendants(true, child, accumulator);
        }
    }

}