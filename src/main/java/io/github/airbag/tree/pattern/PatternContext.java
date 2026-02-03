package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;

import java.util.HashMap;
import java.util.Map;

class PatternContext {

    private final Map<String, DerivationTree> labels = new HashMap<>();
    private DerivationTree tree;

    public PatternContext(DerivationTree tree) {
        this.tree = tree;
    }

    public DerivationTree getTree() {
        return tree;
    }

    public void setTree(DerivationTree tree) {
        this.tree = tree;
    }

    public void addLabel(String label, DerivationTree t) {
        labels.put(label, t);
    }

    public DerivationTree getLabel(String label) {
        return labels.get(label);
    }

    public Map<String, DerivationTree> getLabels() {
        return labels;
    }
}