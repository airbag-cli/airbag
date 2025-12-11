package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;
import io.github.airbag.tree.pattern.PatternBuilder.TreePatternElement;

import java.util.ArrayList;
import java.util.List;

public class Pattern {

    public static final Pattern WILDCARD = new PatternBuilder().appendWildcard().toPattern();

    public static final Pattern EMPTY = new PatternBuilder().toPattern();

    private final TreePatternElement pattern;

    Pattern(TreePatternElement compositePatternElement) {
        this.pattern = compositePatternElement;
    }

    public boolean matches(DerivationTree t) {
        return pattern.match(new PatternContext(t));
    }

    public MatchResult match(DerivationTree t) {
        PatternContext ctx = new PatternContext(t);
        boolean success = pattern.match(ctx);
        return new MatchResult(success, t, ctx.getLabels());
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
        if (pattern instanceof PatternBuilder.CompositePatternElement compositePatternElement) {
            return compositePatternElement.elements();
        } else {
            return new TreePatternElement[] {pattern};
        }
    }
}