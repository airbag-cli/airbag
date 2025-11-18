package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;
import io.github.airbag.tree.pattern.TreePatternBuilder.CompositePatternElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreePattern {

    private final int rootIndex;
    private final CompositePatternElement compositePattern;

    TreePattern(int rootIndex, CompositePatternElement compositePatternElement) {
        this.rootIndex = rootIndex;
        this.compositePattern = compositePatternElement;
    }

    public int index() {
        return rootIndex;
    }

    public boolean matches(DerivationTree t) {
        return compositePattern.match(new TreePatternContext(t));
    }

    public TreeMatchResult match(DerivationTree t) {
        TreePatternContext ctx = new TreePatternContext(t);
        if (!(t.index() == rootIndex)) {
            return new TreeMatchResult(false, t, Collections.emptyMap());
        }
        boolean success = compositePattern.match(ctx);
        return new TreeMatchResult(success, t, ctx.getLabels());
    }

    public List<DerivationTree> findAll(DerivationTree t) {
        List<DerivationTree> descendants = Trees.getDescendants(true, t);
        List<DerivationTree> matches = new ArrayList<>();
        for (var child : descendants) {
            if (matches(child)) {
                matches.add(child);
            }
        }
        return matches;
    }
}