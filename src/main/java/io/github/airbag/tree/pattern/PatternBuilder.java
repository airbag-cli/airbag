package io.github.airbag.tree.pattern;

import io.github.airbag.token.TokenBuilder;
import io.github.airbag.token.TokenField;
import io.github.airbag.tree.DerivationTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Builder for creating {@link Pattern} instances.
 * <p>
 * This builder provides a fluent API to define structural patterns for matching against
 * tree objects, particularly {@link DerivationTree} instances.
 * It is the primary mechanism for constructing {@link Pattern} objects, which are immutable
 * and thread-safe once created.
 *
 * <h3>Overview</h3>
 * The builder assembles a sequence of {@link TreePatternElement} components. Each component
 * specifies a constraint on a node in the tree, such as matching a specific parser rule, a symbol
 * (token), or a wildcard. The sequence of appended components defines the pattern that will be
 * used to match against the children of a given tree node.
 * <p>
 * A {@link Pattern} checks if a given {@link org.antlr.v4.runtime.tree.Tree} node's children
 * match the sequence of elements defined in the pattern.
 *
 * <h3>Usage</h3>
 * To create a pattern, instantiate a {@code PatternBuilder} and call its {@code append...}
 * methods to define the structure you want to match. Once the pattern is defined, call
 * {@link #toPattern()} to create the immutable {@link Pattern} instance.
 *
 * <p><b>Example: Matching a Simple Expression</b></p>
 * <pre>{@code
 * // Suppose we have an ANTLR grammar for expressions like "a + b" and want to match it.
 * // Rule and token indices are typically defined as constants in the generated parser/lexer classes.
 * // For instance, MyParser.RULE_expr for a rule and MyLexer.PLUS for a token.
 * // Replace these with actual constants from your generated grammar.
 *
 * Pattern pattern = new PatternBuilder()
 *     .appendRuleTag(MyParser.RULE_expr, "left") // Matches an 'expr' rule node and labels it 'left'
 *     .appendSymbolTag(MyLexer.PLUS)              // Matches a '+' token node
 *     .appendRuleTag(MyParser.RULE_expr, "right")// Matches another 'expr' rule node and labels it 'right'
 *     .toPattern();
 *
 * // This pattern can now be used to find all addition operations in a parse tree.
 * // Assuming 'someTree' is an ANTLR parse tree root (e.g., a DerivationTree.Rule instance).
 * MatchResult result = pattern.match(someTree);
 * if (result.isSuccess()) {
 *     Tree leftOperand = result.get("left");     // Retrieves the subtree matched by "left"
 *     Tree rightOperand = result.get("right");   // Retrieves the subtree matched by "right"
 *     // ... operate on the captured subtrees (e.g., for refactoring or analysis)
 * }
 * }</pre>
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used to create a pattern and then discarded. The resulting {@link Pattern}
 * objects, however, are immutable and safe for use in multithreaded environments.
 *
 * @see Pattern
 * @see MatchResult
 * @see DerivationTree
 */
public class PatternBuilder {

    private final List<TreePatternElement> treePatternList = new ArrayList<>();

    /**
     * Builds the {@link Pattern} from the sequence of appended components.
     * <p>
     * Once this method is called, the builder's configuration is used to create an
     * immutable {@link Pattern} object. This builder instance is not affected and can
     * be further modified to create other patterns, but it is generally recommended
     * to discard the builder after use.
     *
     * @return The immutable {@link Pattern} instance.
     */
    public Pattern toPattern() {
        return new Pattern(new CompositePatternElement(treePatternList.toArray(new TreePatternElement[0])));
    }

    /**
     * Appends a {@link Token} element to the pattern.
     * <p>
     * This element will match a terminal node (a leaf in the parse tree) if its underlying
     * symbol is considered equal to the provided {@code symbol}.
     * <p>
     * By default, equality is determined by comparing a "simple" set of fields:
     * {@link TokenField#TEXT}, {@link TokenField#TYPE}, {@link TokenField#LINE},
     * and {@link TokenField#POSITION}.
     *
     * @param symbol The {@link Token} to match.
     * @return This builder.
     * @see #appendSymbol(Token, BiPredicate)
     * @see TokenField#equalizer(Set)
     */
    public PatternBuilder appendSymbol(Token symbol) {
        return appendSymbol(symbol, TokenField.equalizer(TokenField.simple()));
    }

    /**
     * Appends a {@link Token} element to the pattern with custom matching logic.
     * <p>
     * This element will match a terminal node if the provided {@code equalizer}
     * predicate returns {@code true} when comparing the node's symbol with the
     * given {@code symbol}.
     * <p>
     * This allows for flexible matching based on specific fields of a {@link Token}.
     * For example, to match any symbol of a certain getType regardless of its getText:
     * <pre>{@code
     * BiPredicate<Token, Token> typeMatcher = TokenField.equalizer(Set.of(TokenField.TYPE));
     * builder.appendSymbol(new TokenBuilder().getType(MyLexer.ID).get(), typeMatcher);
     * }</pre>
     *
     * @param symbol    The {@link Token} to use for the comparison.
     * @param equalizer A {@link BiPredicate} that defines the equality condition between
     *                  the symbol in the tree and the provided {@code symbol}.
     * @return This builder.
     */
    public PatternBuilder appendSymbol(Token symbol, BiPredicate<Token, Token> equalizer) {
        treePatternList.add(new SymbolPatternElement(symbol, equalizer));
        return this;
    }

    /**
     * Appends a rule tag element to the pattern.
     * <p>
     * This element will match a non-terminal node (an internal node in the parse tree)
     * if it corresponds to the specified parser rule getTokenIndex. This is used to match
     * specific grammar constructs.
     *
     * @param ruleIndex The getTokenIndex of the parser rule to match (e.g., {@code MyParser.RULE_expression}).
     * @return This builder.
     */
    public PatternBuilder appendRuleTag(int ruleIndex) {
        treePatternList.add(new RuleTagPatternElement(ruleIndex));
        return this;
    }

    /**
     * Appends a labeled rule tag element to the pattern.
     * <p>
     * This element matches a non-terminal node corresponding to the specified parser rule getTokenIndex.
     * If the match is successful, the matched {@link Tree} node is captured and associated
     * with the given {@code label}. The captured tree can be retrieved from the
     * {@link MatchResult} object.
     *
     * @param ruleIndex The getTokenIndex of the parser rule to match.
     * @param label     The label to associate with the matched subtree. If null or empty,
     *                  the node is not labeled.
     * @return This builder.
     */
    public PatternBuilder appendRuleTag(int ruleIndex, String label) {
        treePatternList.add(new RuleTagPatternElement(ruleIndex, label));
        return this;
    }

    /**
     * Appends a symbol tag element to the pattern.
     * <p>
     * This is a convenience method to match a terminal node based on its token getType.
     * It is equivalent to creating a symbol with only the getType field and using a
     * getType-only equalizer.
     *
     * @param symbolIndex The token getType getTokenIndex to match (e.g., {@code MyLexer.ID}).
     * @return This builder.
     */
    public PatternBuilder appendSymbolTag(int symbolIndex) {
        treePatternList.add(new SymbolTagPatternElement(symbolIndex));
        return this;
    }

    /**
     * Appends a labeled symbol tag element to the pattern.
     * <p>
     * This convenience method matches a terminal node by its token getType. If the match is successful,
     * the matched terminal node is captured and associated with the given {@code label}.
     * The captured tree can be retrieved from the {@link MatchResult}.
     *
     * @param symbolIndex The token getType getTokenIndex to match.
     * @param label       The label to associate with the matched terminal node. If null or empty,
     *                    the node is not labeled.
     * @return This builder.
     */
    public PatternBuilder appendSymbolTag(int symbolIndex, String label) {
        treePatternList.add(new SymbolTagPatternElement(symbolIndex, label));
        return this;
    }

    /**
     * Appends a wildcard element to the pattern.
     * <p>
     * This element acts as a placeholder and will successfully match any single node in the
     * tree, whether it is a terminal or non-terminal node. It is useful for creating
     * patterns where some parts of the structure are irrelevant.
     *
     * @return This builder.
     */
    public PatternBuilder appendWildcard() {
        treePatternList.add(new WildcardPatternElement());
        return this;
    }

    /**
     * Represents a single element within a {@link Pattern}.
     * <p>
     * Implementations of this interface define how a specific part of the tree pattern
     * attempts to match against a node in the target tree.
     */
    interface TreePatternElement {
        /**
         * Attempts to match this pattern element against the current tree in the context.
         *
         * @param ctx The {@link PatternContext} providing the current tree node and other matching state.
         * @return {@code true} if the element successfully matches the tree node, {@code false} otherwise.
         */
        boolean isMatch(PatternContext ctx);
    }

    /**
     * A {@link TreePatternElement} that represents a composite of other elements.
     * <p>
     * This element is used internally by {@link PatternBuilder} to group a sequence
     * of pattern elements, effectively matching the children of a tree node against
     * the defined sub-elements.
     */
    static class CompositePatternElement implements TreePatternElement {

        private final TreePatternElement[] patternElements;

        /**
         * Constructs a new {@code CompositePatternElement}.
         * @param patternElements An array of {@link TreePatternElement}s that form this composite.
         */
        public CompositePatternElement(TreePatternElement[] patternElements) {
            this.patternElements = patternElements;
        }

        @Override
        public boolean isMatch(PatternContext ctx) {
            var t = ctx.getTree();
            for (int i = 0; i < Math.max(patternElements.length, t.size()); i++) {
                if (i == patternElements.length || i == t.size()) {
                    return false;
                }
                ctx.setTree(t.getChild(i));
                if (!patternElements[i].isMatch(ctx)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Returns the array of pattern elements contained within this composite.
         * @return The internal array of {@link TreePatternElement}s.
         */
        public TreePatternElement[] elements() {
            return patternElements;
        }
    }

    /**
     * A {@link TreePatternElement} that matches any single tree node.
     * <p>
     * This wildcard element always successfully matches, regardless of the getType or content
     * of the tree node it is applied to.
     */
    static class WildcardPatternElement implements TreePatternElement {

        @Override
        public boolean isMatch(PatternContext ctx) {
            return true;
        }
    }

    /**
     * A {@link TreePatternElement} that matches a specific {@link Token} in a terminal node.
     * <p>
     * This element compares the symbol of a terminal node in the target tree with a
     * predefined symbol using a specified equality predicate.
     */
    static class SymbolPatternElement implements TreePatternElement {

        private final Token symbol;
        private final BiPredicate<Token, Token> equalizer;

        /**
         * Constructs a new {@code SymbolPatternElement}.
         * @param symbol The {@link Token} to compare against.
         * @param equalizer The {@link BiPredicate} to determine equality between symbols.
         */
        public SymbolPatternElement(Token symbol, BiPredicate<Token, Token> equalizer) {
            this.symbol = symbol;
            this.equalizer = equalizer;
        }

        @Override
        public boolean isMatch(PatternContext ctx) {
            if (ctx.getTree() instanceof DerivationTree.Terminal terminal) {
                return equalizer.test(terminal.symbol(), symbol);
            } else if (ctx.getTree().size() == 1) {
                ctx.setTree(ctx.getTree().getChild(0));
                return isMatch(ctx);
            } else {
                return false;
            }
        }

        /**
         * Returns the {@link Token} used by this element for comparison.
         * @return The reference {@link Token}.
         */
        public Token getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol.toString();
        }
    }

    /**
     * A {@link TreePatternElement} that matches a non-terminal node based on its rule getTokenIndex.
     * <p>
     * Optionally, if a label is provided, the matched rule node is captured and associated
     * with that label in the {@link PatternContext}.
     */
    static class RuleTagPatternElement implements TreePatternElement {

        private final int ruleIndex;
        private final String tag;

        /**
         * Constructs a new {@code RuleTagPatternElement} without a label.
         * @param ruleIndex The ANTLR rule getTokenIndex to match.
         */
        public RuleTagPatternElement(int ruleIndex) {
            this.ruleIndex = ruleIndex;
            this.tag = null;
        }

        /**
         * Constructs a new {@code RuleTagPatternElement} with an optional label.
         * @param ruleIndex The ANTLR rule getTokenIndex to match.
         * @param tag An optional label to capture the matched node.
         */
        public RuleTagPatternElement(int ruleIndex, String tag) {
            this.ruleIndex = ruleIndex;
            this.tag = tag == null || tag.isEmpty() ? null : tag;
        }

        @Override
        public boolean isMatch(PatternContext ctx) {
            boolean result = switch (ctx.getTree()) {
                case DerivationTree.Rule ruleNode -> ruleNode.index() == ruleIndex;
                default -> false;
            };
            if (result && tag != null) {
                ctx.addLabel(tag, ctx.getTree());
            }
            return result;
        }

        /**
         * Returns the rule getTokenIndex this element is configured to match.
         * @return The rule getTokenIndex.
         */
        public int type() {
            return ruleIndex;
        }

        /**
         * Returns the label associated with this element, or {@code null} if no label is set.
         * @return The label string.
         */
        public String label() {
            return tag;
        }
    }

    /**
     * A {@link TreePatternElement} that matches a terminal node based on its symbol getType.
     * <p>
     * This is a specialized form of {@link SymbolPatternElement} that focuses solely on
     * matching the {@link TokenField#TYPE} of a terminal node's symbol.
     * Optionally, if a label is provided, the matched terminal node is captured.
     */
    static class SymbolTagPatternElement implements TreePatternElement {

        private final SymbolPatternElement symbolPattern;
        private final String tag;

        /**
         * Constructs a new {@code SymbolTagPatternElement} without a label.
         * @param symbolIndex The ANTLR token getType getTokenIndex to match.
         */
        public SymbolTagPatternElement(int symbolIndex) {
            this(symbolIndex, null);
        }

        /**
         * Constructs a new {@code SymbolTagPatternElement} with an optional label.
         * @param symbolIndex The ANTLR token getType getTokenIndex to match.
         * @param tag An optional label to capture the matched node.
         */
        public SymbolTagPatternElement(int symbolIndex, String tag) {
            symbolPattern = new SymbolPatternElement(new TokenBuilder().type(symbolIndex).get(),
                    TokenField.equalizer(Set.of(TokenField.TYPE)));
            this.tag = tag == null || tag.isEmpty() ? null : tag;
        }

        @Override
        public boolean isMatch(PatternContext ctx) {
            boolean result = symbolPattern.isMatch(ctx);
            if (result && tag != null) {
                ctx.addLabel(tag, ctx.getTree());
            }
            return result;
        }

        /**
         * Returns the symbol getType getTokenIndex this element is configured to match.
         * @return The symbol getType getTokenIndex.
         */
        public int type() {
            return symbolPattern.getSymbol().getType();
        }

        /**
         * Returns the label associated with this element, or {@code null} if no label is set.
         * @return The label string.
         */
        public String label() {
            return tag;
        }
    }
}