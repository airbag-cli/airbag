package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.FormatterParsePosition;
import io.github.airbag.tree.pattern.TreePattern;
import io.github.airbag.tree.pattern.TreePatternFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for defining the format of a single {@link Node} within a {@link DerivationTree}.
 * <p>
 * This builder provides a flexible way to define custom formats for converting
 * individual nodes in a tree to and from strings. It is the primary mechanism for
 * specifying the node-level format within a {@link TreeFormatterBuilder} when creating
 * a {@link TreeFormatter}.
 *
 * <h3>Overview</h3>
 * The builder uses a fluent API to assemble a sequence of "printer-parsers".
 * Each printer-parser is a component responsible for a specific part of a node's
 * string representation. For example, one component might handle a rule node's name,
 * while another handles a terminal node's underlying symbol.
 * <p>
 * The sequence of appended components defines the format for both formatting and parsing
 * a single node in the tree. The {@link TreeFormatter} uses this format as it traverses
 * the tree structure.
 *
 * <h3>Usage</h3>
 * This builder is not instantiated directly. Instead, it is provided to a consumer
 * via the {@link TreeFormatterBuilder#onRule(Consumer)},
 * {@link TreeFormatterBuilder#onTerminal(Consumer)}, and
 * {@link TreeFormatterBuilder#onError(Consumer)} methods. This allows for defining
 * distinct formats for each type of node within a tree.
 *
 * <p><b>Example: Defining Node Formats</b></p>
 * <pre>{@code
 * // This example defines a different format for rule nodes and terminal nodes.
 * // Rule nodes will be rendered as "(<rule_name> ...children... )"
 * // Terminal nodes will be rendered as "TOKEN_NAME:'text'"
 *
 * TreeFormatter treeFormatter = new TreeFormatterBuilder()
 *     .onRule(ruleNode -> ruleNode
 *         .appendLiteral("(")
 *         .appendRule()
 *         .appendLiteral(" ")
 *         .appendChildren(" ") // Recursively format children, separated by a space
 *         .appendLiteral(")")
 *     )
 *     .onTerminal(terminalNode -> terminalNode
 *         .appendSymbol() // Uses the configured SymbolFormatter
 *     )
 *     .toFormatter()
 *     .withSymbolFormatter(
 *         new SymbolFormatterBuilder().appendPattern("S:\\'x\\'").toFormatter()
 *     );
 *
 * // Example output for a simple tree:
 * // (expr (atom INT:'42'))
 * String result = treeFormatter.format(myTree);
 * }</pre>
 *
 * <h3>Traversal and Recursion</h3>
 * The {@link #appendChildren(String)} component is required to enable tree traversal
 * during formatting. It acts as a placeholder that instructs the parent {@link TreeFormatter}
 * where to recursively render the child nodes relative to the parent. Without it, only the
 * root node will be formatted.
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used within the context of a {@link TreeFormatterBuilder} to create a formatter
 * and then discarded. The resulting {@link TreeFormatter} is safe for use in
 * multithreaded environments.
 *
 * @see TreeFormatter
 * @see TreeFormatterBuilder
 * @see Node
 * @see DerivationTree
 */
public class NodeFormatterBuilder {

    private final List<NodePrinterParser> printerParsers = new ArrayList<>();

    /**
     * Constructs a new NodeFormatterBuilder.
     * <p>
     * This constructor is package-private and is intended to be used by
     * {@link TreeFormatterBuilder}.
     */
    NodeFormatterBuilder() {
    }

    /**
     * Appends a printer/parser for a literal string to the formatter.
     * <p>
     * <b>Formatting:</b> This component always appends the specified string to the output.
     * <p>
     * <b>Parsing:</b> This component expects to find the exact same literal string in the
     * input. If the literal is not found at the current position, parsing of this
     * component fails.
     *
     * @param literal The literal string to append. Must not be null.
     * @return This builder.
     */
    public NodeFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a printer/parser for a node's underlying {@link Symbol}.
     * <p>
     * This component is polymorphic and only applies to terminal nodes of the tree
     * (i.e., {@link DerivationTree.Terminal} and {@link DerivationTree.Error}).
     * <p>
     * <b>Formatting:</b> If the current node is a terminal or error node, this component
     * formats the associated {@link Symbol} using the {@link SymbolFormatter} configured
     * in the parent {@link TreeFormatter}. If the node is a {@link DerivationTree.Rule},
     * this component does nothing and formatting succeeds.
     * <p>
     * <b>Parsing:</b> This component attempts to parse a {@link Symbol} from the input
     * string using the configured {@link SymbolFormatter}. The parsed symbol is then
     * used to construct a {@link DerivationTree.Terminal} or {@link DerivationTree.Error} node.
     * This component must be present in a node format if the tree contains terminal nodes.
     *
     * @return This builder.
     * @see SymbolFormatter
     * @see TreeFormatter#withSymbolFormatter(SymbolFormatter)
     */
    public NodeFormatterBuilder appendSymbol() {
        printerParsers.add(new SymbolPrinterParser());
        return this;
    }

    /**
     * Appends a printer/parser for a {@link DerivationTree.Rule} node's identity.
     * <p>
     * This component is polymorphic and only applies to {@link DerivationTree.Rule} nodes.
     * <p>
     * <b>Formatting:</b> If the current node is a rule node, this component attempts to
     * format its identity. It first tries to get the rule's name from the ANTLR
     * {@link org.antlr.v4.runtime.Recognizer}. If successful, it appends the name. If the
     * recognizer is not available or the rule index is out of bounds, it falls back to
     * appending the integer rule index. If the node is not a rule node, this component
     * does nothing and formatting succeeds.
     * <p>
     * <b>Parsing:</b> It attempts to parse a rule identity from the input. It first tries
     * to match a known rule name from the recognizer. If that fails or no recognizer is
     * available, it attempts to parse an integer rule index. The result is used to set
     * the index of the parsed rule node.
     *
     * @return This builder.
     */
    public NodeFormatterBuilder appendRule() {
        printerParsers.add(new RulePrinterParser());
        return this;
    }

    /**
     * Appends a placeholder that instructs the {@link TreeFormatter} on how to format child nodes.
     * <p>
     * This component is a special instruction for the {@link TreeFormatter} and does not
     * directly format or parse content itself. It is a required component for any format
     * that needs to represent a tree structure, as it dictates where child nodes are rendered.
     * <p>
     * <b>Formatting:</b> When the formatter encounters this placeholder, it recursively formats
     * the children of the current node. The formatted strings of the children are then joined
     * together using the provided {@code separator}.
     * <p>
     * <b>Parsing:</b> This component is <b>not supported</b> during parsing and will throw an
     * {@link UnsupportedOperationException} if a format containing it is used for parsing.
     *
     * <p><b>Example: LISP-style tree format</b></p>
     * <pre>{@code
     * // Produces a format like: (rule child1 child2 child3)
     * new TreeFormatterBuilder()
     *     .onRule(nodeBuilder -> nodeBuilder
     *         .appendLiteral("(")
     *         .appendRule()
     *         .appendLiteral(" ")
     *         .appendChildren(" ") // Children separated by spaces
     *         .appendLiteral(")")
     *     )
     *     .toFormatter();
     * }</pre>
     *
     * @param separator The string to insert between the formatted output of each child node.
     * @return This builder.
     * @throws UnsupportedOperationException during parsing.
     */
    public NodeFormatterBuilder appendChildren(String separator) {
        printerParsers.add(new ChildrenPrinterParser(new LiteralPrinterParser(separator)));
        return this;
    }

    /**
     * Appends a placeholder for child nodes with a separator defined by a nested formatter.
     * <p>
     * This is an advanced version of {@link #appendChildren(String)} that allows the separator
     * between child nodes to be defined by its own {@link NodeFormatterBuilder}. This enables
     * dynamic or complex separators, such as separators that include padding or other structured
     * elements.
     * <p>
     * <b>Formatting:</b> When formatting, the {@link TreeFormatter} will recursively format the
     * children of the current node. The provided nested formatter is used to generate the
     * separator string that is inserted between each child's output.
     * <p>
     * <b>Parsing:</b> This component is <b>not supported</b> during parsing and will throw an
     * {@link UnsupportedOperationException}.
     *
     * <p><b>Example: Children on new lines with indentation</b></p>
     * <pre>{@code
     * // Produces a format where each child is on a new line and indented.
     * new TreeFormatterBuilder()
     *     .onRule(nodeBuilder -> nodeBuilder
     *         .appendRule()
     *         .appendChildren(separator -> separator
     *             .appendLiteral("
")
     *             .appendPadding("  ") // Indent each child
     *         )
     *     )
     *     .toFormatter();
     * }</pre>
     *
     * @param childSeparator A consumer that configures a {@link NodeFormatterBuilder} to define the separator.
     * @return This builder.
     * @throws UnsupportedOperationException during parsing.
     */
    public NodeFormatterBuilder appendChildren(Consumer<NodeFormatterBuilder> childSeparator) {
        var builder = new NodeFormatterBuilder();
        childSeparator.accept(builder);
        printerParsers.add(new ChildrenPrinterParser(new TreeFormatterBuilder.CompositePrinterParser(
                builder.printerParsers())));
        return this;
    }

    /**
     * Appends a flexible whitespace parser and a fixed (empty) whitespace formatter.
     * <p>
     * <b>Formatting:</b> This component appends nothing to the output. It is a no-op.
     * <p>
     * <b>Parsing:</b> This component consumes any contiguous sequence of whitespace characters
     * (spaces, tabs, newlines) from the input string. It is useful for allowing for
     * optional whitespace in a format without enforcing a specific structure.
     *
     * @return This builder.
     */
    public NodeFormatterBuilder appendWhitespace() {
        return appendWhitespace("");
    }

    /**
     * Appends a printer for a fixed whitespace string and a parser for flexible whitespace.
     * <p>
     * <b>Formatting:</b> Appends the specified whitespace string to the output. The provided
     * string must only contain whitespace characters.
     * <p>
     * <b>Parsing:</b> This component consumes any contiguous sequence of whitespace characters
     * from the input. The content of the {@code whitespace} parameter is ignored during
     * parsing; this component acts as a flexible whitespace consumer.
     *
     * @param whitespace The string of whitespace characters to append during formatting.
     * @return This builder.
     * @throws IllegalArgumentException if the provided string contains non-whitespace characters.
     */
    public NodeFormatterBuilder appendWhitespace(String whitespace) {
        printerParsers.add(new WhitespacePrinterParser(whitespace, false));
        return this;
    }

    /**
     * Appends a printer for depth-based indentation and a parser for flexible whitespace.
     * <p>
     * This component is used to create indented, human-readable output. The indentation
     * level is determined by the node's depth in the tree.
     * <p>
     * <b>Formatting:</b> Appends the {@code indent} string repeated by the node's depth.
     * For example, if {@code indent} is {@code "  "}, a node at depth 0 gets no indent,
     * a node at depth 1 gets "  ", a node at depth 2 gets "    ", and so on.
     * The provided string must only contain whitespace characters.
     * <p>
     * <b>Parsing:</b> This component consumes any contiguous sequence of whitespace characters
     * from the input. The indentation structure is <b>not</b> enforced during parsing;
     * this component acts as a flexible whitespace consumer, identical to {@link #appendWhitespace()}.
     *
     * @param indent The string to use for each level of indentation.
     * @return This builder.
     * @throws IllegalArgumentException if the provided string contains non-whitespace characters.
     */
    public NodeFormatterBuilder appendIndent(String indent) {
        printerParsers.add(new WhitespacePrinterParser(indent, true));
        return this;
    }

    /**
     * Appends a padding string that is dynamically calculated based on the node's depth in the tree.
     * <p>
     * This component is typically used to create indented, human-readable representations of a
     * {@link DerivationTree}.
     * <p>
     * <b>Formatting:</b> The provided function is called with the current node's depth (where the
     * root is at depth 0), and the returned string is appended to the output.
     * <p>
     * <b>Parsing:</b> The function is called with the current parsing depth. The parser then
     * expects to find the returned string at the current position in the input.
     *
     * @param padFunction A function that takes an integer depth and returns a padding string.
     * @return This builder.
     */
    public NodeFormatterBuilder appendPadding(Function<Integer, String> padFunction) {
        printerParsers.add(new PaddingPrinterParser(padFunction));
        return this;
    }

    /**
     * Appends padding by repeating a given string for each level of depth.
     * <p>
     * This is a convenience method for {@link #appendPadding(Function)}.
     * For example, if {@code padString} is {@code "  "}, a node at depth 0 gets no padding,
     * a node at depth 1 gets "  ", a node at depth 2 gets "    ", and so on.
     * <p>
     * <b>Formatting:</b> Appends {@code padString.repeat(node.depth())}.
     * <p>
     * <b>Parsing:</b> Expects to find {@code padString.repeat(currentDepth)} in the input.
     *
     * @param padString The string to repeat for each level of indentation.
     * @return This builder.
     */
    public NodeFormatterBuilder appendPadding(String padString) {
        return appendPadding(padString::repeat);
    }

    /**
     * Appends padding by repeating a space character a fixed number of times for each depth level.
     * <p>
     * This is a convenience method for {@code appendPadding(" ".repeat(padSize))}.
     * <p>
     * <b>Formatting:</b> Appends a number of spaces equal to {@code padSize * node.depth()}.
     * <p>
     * <b>Parsing:</b> Expects to find a number of spaces equal to {@code padSize * currentDepth}.
     *
     * @param padSize The number of spaces to use for each level of indentation.
     * @return This builder.
     */
    public NodeFormatterBuilder appendPadding(int padSize) {
        return appendPadding(" ".repeat(padSize));
    }

    public NodeFormatterBuilder appendPattern() {
        printerParsers.add(new PatternPrinterParser());
        return this;
    }

    NodePrinterParser[] printerParsers() {
        return printerParsers.toArray(new NodePrinterParser[0]);
    }

    /**
     * The internal interface for parsing and formatting a single {@link DerivationTree} node.
     * <p>
     * This interface is the building block for the composite {@link TreeFormatter}.
     * It defines the dual functionality of formatting (printing) a node's components
     * into a string and parsing an input character sequence to extract node information.
     */
    interface NodePrinterParser {

        /**
         * Formats a value from a {@link NodeFormatContext} into a string buffer.
         *
         * @param ctx the context holding the node and other values to be formatted.
         * @param buf the buffer to append the formatted text to.
         * @return {@code true} if the formatting was successful, {@code false} otherwise.
         */
        boolean format(NodeFormatContext ctx, StringBuilder buf);

        /**
         * Parses a text string, consuming characters and updating the {@link NodeParseContext}.
         *
         * @param ctx      the context to store the parsed values.
         * @param text     the text to parse.
         * @param position the position to start parsing from.
         * @return the new position after a successful parse, or a negative value if parsing fails.
         */
        int parse(NodeParseContext ctx, CharSequence text, int position);
    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    private static String textLookahead(CharSequence text, int position, int lookahead) {
        if (position == text.length()) {
            return "<text end>";
        }
        return text.subSequence(position, Math.min(text.length(), position + lookahead)).toString();
    }

    static class LiteralPrinterParser implements NodePrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.contentEquals(text.subSequence(position, positionEnd))) {
                ctx.root()
                        .recordError(position,
                                escapeText("Expected literal '%s' but found '%s'".formatted(literal,
                                        textLookahead(text, position, literal.length()))));
                return ~position;
            }
            return positionEnd;
        }
    }

    private static String escapeText(String text) {
        text = text.replace("\n", "\\n");
        text = text.replace("\t", "\\t");
        return text.replace("\r", "\\r");
    }

    static class SymbolPrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            return switch (ctx.node()) {
                case DerivationTree.Terminal terminalNode -> {
                    SymbolFormatter formatter = ctx.symbolFormatter();
                    buf.append(formatter.format(terminalNode.symbol()));
                    yield true;
                }
                case DerivationTree.Error errorNode -> {
                    SymbolFormatter formatter = ctx.symbolFormatter();
                    buf.append(formatter.format(errorNode.symbol()));
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            FormatterParsePosition parsePosition = new FormatterParsePosition(position);
            SymbolFormatter symbolFormatter = ctx.symbolFormatter();
            Symbol symbol = symbolFormatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() > 0) {
                String[] messages = parsePosition.getMessage().split("\n");
                int index = parsePosition.getIndex();
                for (var message : messages) {
                    ctx.root().recordError(index, message);
                }
                return ~parsePosition.getErrorIndex();
            }
            if (ctx instanceof RootParseContext.Terminal terminalCtx) {
                terminalCtx.setSymbol(symbol);
            } else if (ctx instanceof RootParseContext.Error errorCtx) {
                errorCtx.setSymbol(symbol);
            } else {
                throw new RuntimeException("Wrong context type");
            }
            return parsePosition.getIndex();
        }
    }

    static class IntegerRulePrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            DerivationTree node = ctx.node();
            return switch (node) {
                case DerivationTree.Rule ruleNode -> {
                    buf.append(ruleNode.index());
                    yield true;
                }
                case DerivationTree.Pattern patterNode -> {
                    buf.append(patterNode.index());
                    yield true;
                }
                case null, default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int numberEnd = peek(text, position);
            if (numberEnd < 0) {
                ctx.root()
                        .recordError(~numberEnd,
                                "Expected an integer for a rule index but found '%s'".formatted(
                                        textLookahead(text, position, 3)));
                return numberEnd;
            }
            int index = Integer.parseInt(text.subSequence(position, numberEnd).toString());
            if (ctx instanceof RootParseContext.Rule ruleCtx) {
                ruleCtx.setIndex(index);
            } else if (ctx instanceof  RootParseContext.Pattern patternCtx) {
                patternCtx.setIndex(index);
            }
            else {
                throw new RuntimeException("No rule or pattern context");
            }
            return numberEnd;
        }

        public int peek(CharSequence text, int position) {
            validatePosition(text, position);
            return text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
        }
    }

    static int findNumberEnd(CharSequence text, int position) {
        if (position >= text.length() || !Character.isDigit(text.charAt(position))) {
            return ~position;
        }
        while (text.length() != position && Character.isDigit(text.charAt(position))) {
            position++;
        }
        return position;
    }

    static class StringRuleNamePrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            DerivationTree node = ctx.node();
            return switch (node) {
                case DerivationTree.Rule ruleNode -> {
                    String[] ruleNames = ctx.recognizer() == null ?
                            new String[0] :
                            ctx.recognizer().getRuleNames();
                    if (ruleNode.index() >= ruleNames.length) {
                        yield false;
                    }
                    buf.append(ruleNames[ruleNode.index()]);
                    yield true;
                }
                case DerivationTree.Pattern patternNode -> {
                    String[] ruleNames = ctx.recognizer() == null ?
                            new String[0] :
                            ctx.recognizer().getRuleNames();
                    if (patternNode.index() >= ruleNames.length) {
                        yield false;
                    }
                    buf.append(ruleNames[patternNode.index()]);
                    yield true;
                }
                case null, default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            String[] ruleNames = ctx.recognizer() == null ?
                    new String[0] :
                    ctx.recognizer().getRuleNames();
            int index = findRuleIndex(text, ruleNames, position);
            if (index < 0) {
                ctx.root()
                        .recordError(position,
                                "Unrecognized rule name starting with '%s'".formatted(textLookahead(
                                        text,
                                        position, 5)));
                return ~position;
            }
            if (ctx instanceof RootParseContext.Rule ruleContext) {
                ruleContext.setIndex(index);
            }
            if (ctx instanceof RootParseContext.Pattern patternContext) {
                patternContext.setIndex(index);
            }
            return position + ruleNames[index].length();
        }

        private int findRuleIndex(CharSequence text, String[] ruleNames, int position) {
            int index = -1;
            int maxLength = 0;
            for (int i = 0; i < ruleNames.length; i++) {
                String ruleName = ruleNames[i];
                if (ruleName.length() > maxLength &&
                    text.length() - position >= ruleName.length() &&
                    text.subSequence(position, position + ruleName.length())
                            .toString()
                            .equals(ruleName)) {
                    maxLength = ruleName.length();
                    index = i;
                }
            }
            return index;
        }
    }

    static class RulePrinterParser implements NodePrinterParser {

        private final NodePrinterParser[] printerParsers;

        RulePrinterParser() {
            printerParsers = new NodePrinterParser[]{new StringRuleNamePrinterParser(),
                    new IntegerRulePrinterParser()};
        }

        @Override
        public boolean format(NodeFormatContext context, StringBuilder buf) {
            for (NodePrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(NodeParseContext context, CharSequence text, int position) {
            for (NodePrinterParser parser : printerParsers) {
                int end = parser.parse(context, text, position);
                if (end > 0) {
                    return end;
                }
            }
            return ~position;
        }
    }

    record ChildrenPrinterParser(NodePrinterParser separator) implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            throw new UnsupportedOperationException();
        }
    }

    static class PaddingPrinterParser implements NodePrinterParser {

        private final Function<Integer, String> padFunction;

        public PaddingPrinterParser(Function<Integer, String> padFunction) {
            this.padFunction = padFunction;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            buf.append(padFunction.apply(ctx.node().depth()));
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            String literal = padFunction.apply(ctx.depth());
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.contentEquals(text.subSequence(position, positionEnd))) {
                ctx.root()
                        .recordError(position,
                                escapeText("Expected padding literal '%s' but found '%s'".formatted(
                                        literal,
                                        textLookahead(text,
                                                position,
                                                Math.max(10, literal.length() + 4)))));
                return ~position;
            }
            return positionEnd;
        }
    }

    static class WhitespacePrinterParser implements NodePrinterParser {

        private final String whitespace;
        private final boolean isIndented;

        public WhitespacePrinterParser(String whitespace, boolean indented) {
            if (!whitespace.chars().allMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Can only append whitespace");
            }
            this.whitespace = whitespace;
            this.isIndented = indented;
        }

        @Override
        public boolean format(NodeFormatContext context, StringBuilder buf) {
            buf.append(isIndented ? whitespace.repeat(context.node().depth()) : whitespace);
            return true;
        }

        @Override
        public int parse(NodeParseContext context, CharSequence text, int position) {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
            return position;
        }
    }

    static class PatternPrinterParser implements NodePrinterParser {

       @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            return switch (ctx.node()) {
                case DerivationTree.Pattern patternNode -> {
                    TreePatternFormatter formatter = ctx.patternFormatter();
                    buf.append(formatter.format(patternNode.getPattern()));
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            FormatterParsePosition parsePosition = new FormatterParsePosition(position);
            TreePatternFormatter patternFormatter = ctx.patternFormatter();
            TreePattern pattern = patternFormatter.parse(text, parsePosition);
            if (ctx instanceof RootParseContext.Pattern patternCtx) {
                patternCtx.setPattern(pattern);
            } else {
                throw new RuntimeException("Wrong context type");
            }
            return parsePosition.getIndex();
        }
    }
}