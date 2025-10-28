package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;

import java.util.Collection;

public abstract class QueryElement {

    public abstract Collection<DerivationTree> evaluate(DerivationTree t);

}