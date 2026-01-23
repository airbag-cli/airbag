package io.github.airbag.tree.pattern;

import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;
import io.github.airbag.tree.pattern.PatternBuilder.TreePatternElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a compiled tree pattern used for matching against {@link DerivationTree} instances.
 * <p>
 * A {@code Pattern} is immutable and thread-safe, created typically via a {@link PatternBuilder}.
 * It encapsulates a sequence of {@link PatternBuilder.TreePatternElement}s that define
 * the structural requirements for a successful match within a derivation tree.
 * </p>
 * <p>
 * This class provides functionality to:
 * <ul>
 *     <li>Check if a given {@link DerivationTree} fully matches the pattern.</li>
 *     <li>Perform a match operation, returning a {@link MatchResult} that includes any
 *         labeled subtrees.</li>
 *     <li>Find all occurrences of the pattern within a larger {@link DerivationTree}.</li>
 * </ul>
 *
 * @see PatternBuilder
 * @see MatchResult
 * @see DerivationTree
 */
public class Pattern {

    /**
     * A pattern element that requires an empty subtree
     */
    public static final Pattern NOTHING = new Pattern(new PatternBuilder.CompositePatternElement(new TreePatternElement[0]));

    //The patter field used for matching logic
    private final TreePatternElement pattern;

    /**
     * Constructs a new {@code Pattern} from a composite pattern element.
     * This constructor is package-private and intended for use by {@link PatternBuilder}.
     *
     * @param compositePatternElement The root {@link TreePatternElement} representing the compiled pattern.
     */
    Pattern(TreePatternElement compositePatternElement) {
        this.pattern = compositePatternElement;
    }

    /**
     * Checks if the given {@link DerivationTree} matches this pattern.
     * This method performs a boolean check without capturing labeled subtrees.
     *
     * @param t The {@link DerivationTree} to check against the pattern.
     * @return {@code true} if the tree matches the pattern, {@code false} otherwise.
     */
    public boolean isMatch(DerivationTree t) {
        return pattern.isMatch(new PatternContext(t));
    }

    /**
     * Attempts to match the given {@link DerivationTree} against this pattern,
     * returning a {@link MatchResult} that indicates success and includes any
     * captured labeled subtrees.
     *
     * @param t The {@link DerivationTree} to match.
     * @return A {@link MatchResult} containing the success status and a map of labeled subtrees.
     */
    public MatchResult match(DerivationTree t) {
        PatternContext ctx = new PatternContext(t);
        boolean success = pattern.isMatch(ctx);
        return new MatchResult(success, t, ctx.getLabels());
    }

    /**
     * Finds all occurrences of this pattern within the given {@link DerivationTree} and its descendants.
     * This method traverses the tree and collects all subtrees that fully match the pattern.
     *
     * @param t The {@link DerivationTree} to search within.
     * @return A list of {@link DerivationTree} instances that match the pattern.
     * Returns an empty list if no matches are found.
     */
    public List<DerivationTree> findAll(DerivationTree t) {
        List<DerivationTree> descendants = Trees.getDescendants(true, t);
        List<DerivationTree> matches = new ArrayList<>();
        for (var child : descendants) {
            if (isMatch(child)) {
                matches.add(child);
            }
        }
        return matches;
    }

    /**
     * Returns the internal array of {@link TreePatternElement}s that constitute this pattern.
     * This method is intended for internal use, particularly by {@link PatternFormatter}.
     *
     * @return An array of {@link TreePatternElement}s.
     */
    TreePatternElement[] getElements() {
        if (pattern instanceof PatternBuilder.CompositePatternElement compositePatternElement) {
            return compositePatternElement.elements();
        } else {
            return new TreePatternElement[]{pattern};
        }
    }
}