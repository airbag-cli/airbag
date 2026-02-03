package io.github.airbag.tree.query;

import io.github.airbag.tree.DerivationTree;

import java.util.*;

/**
 * Represents a compiled query for navigating and selecting nodes within a {@link DerivationTree}.
 * A {@code Query} is typically created by a {@link QueryProvider} from a string-based query language.
 * It consists of a sequence of {@link QueryElement}s, each defining a step in the tree traversal
 * and selection process.
 * <p>
 * Once compiled, a {@code Query} can be evaluated against a starting {@link DerivationTree}
 * to find all matching subtrees.
 *
 * @see QueryProvider
 * @see QueryElement
 * @see DerivationTree
 */
public class Query {

    //Underlying query element array used for navigation logic
    private final QueryElement[] elements;

    /**
     * Constructs a new {@code Query} from an array of {@link QueryElement}s.
     * This constructor is typically used internally by {@link QueryProvider}.
     *
     * @param elements The array of {@link QueryElement}s that define the query's steps.
     */
    public Query(QueryElement[] elements) {
        this.elements = elements;
    }

    /**
     * Evaluates this query against a given {@link DerivationTree}, returning a list of all
     * {@link DerivationTree} nodes that match the query. The evaluation proceeds by
     * iteratively applying each {@link QueryElement} to the set of currently matched nodes.
     *
     * @param t The starting {@link DerivationTree} from which to evaluate the query.
     * @return A {@link List} of {@link DerivationTree} instances that match the query,
     *         or an empty list if no matches are found. The order of results is determined
     *         by the traversal order during evaluation.
     */
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