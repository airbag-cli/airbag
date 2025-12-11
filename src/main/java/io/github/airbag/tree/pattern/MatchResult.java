package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;

import java.util.Map;

public record MatchResult(boolean isSuccess, DerivationTree tree, Map<String, DerivationTree> labels) {

    public DerivationTree getLabel() {
        return tree();
    }

    public DerivationTree getLabel(String label) {
        return labels.get(label);
    }
}