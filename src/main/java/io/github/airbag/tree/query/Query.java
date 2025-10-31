package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;

import java.util.*;

public class Query {

    private final QueryElement[] elements;

    public Query(QueryElement[] elements) {
        this.elements = elements;
    }

    public List<DerivationTree> evaluate(DerivationTree t) {
        SequencedSet<DerivationTree> work = new LinkedHashSet<>();
        work.add(t);
        SequencedSet<DerivationTree> next = new LinkedHashSet<>();
        for (var element : elements) {
            next.clear();
            for (var node : work) {
                Collection<DerivationTree> matching = element.evaluate(node);
                next.addAll(matching);
            }
            work.clear();
            work.addAll(next);
        }
        return List.copyOf(work);
    }
}