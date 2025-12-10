package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;
import io.github.airbag.tree.pattern.TreePatternBuilder.TreePatternElement;

import java.util.ArrayList;
import java.util.List;

public class TreePattern {

    public static final TreePattern WILDCARD = new TreePatternBuilder().appendWildcard().toPattern();

    public static final TreePattern EMPTY = new TreePatternBuilder().toPattern();

    private final TreePatternElement pattern;

    TreePattern(TreePatternElement compositePatternElement) {
        this.pattern = compositePatternElement;
    }

    public boolean matches(DerivationTree t) {
        return pattern.match(new TreePatternContext(t));
    }

    public TreeMatchResult match(DerivationTree t) {
        TreePatternContext ctx = new TreePatternContext(t);
        boolean success = pattern.match(ctx);
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

    TreePatternElement[] getElements() {
        if (pattern instanceof TreePatternBuilder.CompositePatternElement compositePatternElement) {
            return compositePatternElement.elements();
        } else {
            return new TreePatternElement[] {pattern};
        }
    }
}