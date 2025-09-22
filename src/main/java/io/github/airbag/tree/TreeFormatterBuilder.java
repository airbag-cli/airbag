package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating {@link TreeFormatter} instances.
 * <p>
 * This builder provides a flexible and powerful way to define custom formats for
 * converting {@link Node} objects to and from strings.
 * It is the primary mechanism for constructing {@link TreeFormatter}s, which are
 * immutable and thread-safe once created.
 *
 * <h3>Overview</h3>
 * The builder uses a fluent API to assemble a sequence of "printer-parsers".
 * Each printer-parser is a component responsible for a specific part of the
 * format. For example, one component might handle a rule's name, while another
 * handles its children.
 * <p>
 * A {@link TreeFormatter} has two main functions:
 * <ul>
 *   <li><b>Formatting (Printing):</b> Converting a {@code Node} object into a string.</li>
 *   <li><b>Parsing:</b> Converting a string back into a {@code Node} object.</li>
 * </ul>
 * The sequence of appended components defines the exact format for both operations.
 * When formatting, terminal and error nodes are formatted using their own respective
 * {@link SymbolFormatter} instances provided via the {@link TreeFormatContext}.
 *
 * <h3>Usage</h3>
 * To create a formatter, you instantiate a {@code TreeFormatterBuilder} and call
 * various {@code append...} methods to define the desired format. Once the
 * format is defined, you call {@link #toFormatter()} to create the
 * {@link TreeFormatter} instance.
 *
 * <p><b>Example: LISP-style Formatter</b></p>
 * <pre>{@code
 * // Creates a formatter that represents a tree in a LISP-like format,
 * // e.g., "(ruleName child1 child2)"
 * TreeFormatter formatter = new TreeFormatterBuilder()
 *     .appendLiteral("(")
 *     .appendRule()
 *     .appendLiteral(" ")
 *     .appendChildren(" ") // Recursively formats children, separated by a space
 *     .appendLiteral(")")
 *     .toFormatter();
 *
 * // Formatting example:
 * // Assuming a rule node for 'expression' with two children representing '1' and '+',
 * // the output of formatter.format(node, context) might be:
 * // "(expression 1 +)"
 *
 * // The same formatter can also parse this string back into a tree structure.
 * }</pre>
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used to create a formatter and then discarded. The resulting {@link TreeFormatter}
 * objects, however, are immutable and safe for use in multithreaded environments.
 *
 * @see TreeFormatter
 * @see Node
 */
public class TreeFormatterBuilder {

    private final List<TreePrinterParser> printerParsers = new ArrayList<>();

    /**
     * Appends a printer/parser for a literal string to the formatter.
     * <p>
     * This component always appends the specified literal to the output during
     * formatting and expects to consume the exact same literal from the input
     * during parsing.
     *
     * @param literal The literal string to append.
     * @return This builder.
     */
    public TreeFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a printer/parser for the children of a rule node.
     * <p>
     * <b>Formatting:</b> This component iterates through the children of the current
     * {@link Node.Rule}. For each child, it recursively applies the complete tree
     * format, and it appends the specified separator between each child's output.
     * <p>
     * <b>Parsing:</b> It repeatedly attempts to parse child nodes, separated by the
     * given separator. It continues as long as it can successfully parse a
     * child-separator sequence. This process is inherently recursive, as parsing a
     * child involves applying the complete tree format.
     *
     * @param separator The string to use as a separator between child nodes.
     * @return This builder.
     */
    public TreeFormatterBuilder appendChildren(String separator) {
        printerParsers.add(new ChildrenPrinterParser(separator));
        return this;
    }

    /**
     * Appends a printer/parser for a rule's identity.
     * <p>
     * This component handles the representation of a {@link Node.Rule} itself,
     * distinct from its children or any surrounding literals.
     * <p>
     * <b>Formatting:</b> It attempts to format the rule's name (e.g., "expression")
     * if a {@code Recognizer} with rule names is available in the
     * {@link TreeFormatContext}. If not, it falls back to formatting the rule's
     * integer index.
     * <p>
     * <b>Parsing:</b> It first attempts to parse a rule name by checking against the
     * list of known rule names. If that fails, it attempts to parse an integer,
     * which it uses as the rule index. The parsed rule is then attached to the
     * tree being built.
     *
     * @return This builder.
     */
    public TreeFormatterBuilder appendRule() {
        printerParsers.add(new RulePrinterParser());
        return this;
    }

    /**
     * Builds the tree formatter.
     *
     * @return The built tree formatter.
     */
    public TreeFormatter toFormatter() {
        return new TreeFormatter(new NodePrinterParser(printerParsers));
    }

    /**
     * The internal interface for parsing and formatting tree nodes.
     * This interface is the building block for the composite {@link TreeFormatter}.
     * It defines the dual functionality of formatting (printing) a node and
     * parsing an input character sequence to construct a node.
     */
    interface TreePrinterParser {

        /**
         * Formats a value from a context into a string buffer.
         *
         * @param ctx the context holding the node to be formatted.
         * @param buf the buffer to append the formatted text to.
         * @return true if the formatting was successful, false otherwise.
         */
        boolean format(TreeFormatContext ctx, StringBuilder buf);

        /**
         * Parses a text string, consuming characters and updating the context.
         *
         * @param ctx      the context to store the parsed node.
         * @param text     the text to parse.
         * @param position the position to start parsing from.
         * @return the new position after a successful parse, or a negative value if parsing fails.
         */
        int parse(TreeParseContext ctx, CharSequence text, int position);
    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    static class CompositePrinterParser implements TreePrinterParser {

        private final TreePrinterParser[] printerParsers;

        private CompositePrinterParser(List<TreePrinterParser> printerParsers) {
            this(printerParsers.toArray(new TreePrinterParser[0]));
        }

        private CompositePrinterParser(TreePrinterParser[] printerParsers) {
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            int initialLength = buf.length();
            for (TreePrinterParser printer : printerParsers) {
                if (!printer.format(ctx, buf)) {
                    buf.setLength(initialLength);
                    return false;
                }
            }
            return true;
        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            for (TreePrinterParser parser : printerParsers) {
                position = parser.parse(ctx, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }
    }

    static class NodePrinterParser implements TreePrinterParser {

        private final TreePrinterParser[] printerParsers;

        public NodePrinterParser(List<TreePrinterParser> list) {
            this(list.toArray(new TreePrinterParser[0]));
        }

        private NodePrinterParser(TreePrinterParser[] printerParsers) {
            for (int i = 0; i < printerParsers.length; i++) {
                if (printerParsers[i] instanceof ChildrenPrinterParser childrenPrinterParser) {
                    printerParsers[i] = new ChildrenPrinterParser(this, childrenPrinterParser);
                }
            }
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            return switch (ctx.getNode()) {
                case Node.Rule<?> ignored -> {
                    for (var printer : printerParsers) {
                        if (!printer.format(ctx, buf)) {
                            yield false;
                        }
                    }
                    yield true;
                }
                case Node.Terminal<?> terminal -> {
                    SymbolFormatter formatter = ctx.terminalFormatter();
                    buf.append(formatter.format(terminal.getSymbol()));
                    yield true;
                }
                case Node.Error<?> error -> {
                    SymbolFormatter formatter = ctx.errorFormatter();
                    buf.append(formatter.format(error.getSymbol()));
                    yield true;
                }
                default -> throw new RuntimeException();
            };
        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            ParsePosition parsePosition = new ParsePosition(position);
            SymbolFormatter symbolFormatter = ctx.terminalFormatter();
            Symbol symbol = symbolFormatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() < 0) {
                var connector = ctx.connectors().get("terminal");
                Node<?> node = connector.apply(ctx.getNode(), symbol);
                ctx.setNode(node);
                return parsePosition.getIndex();
            }
            symbolFormatter = ctx.errorFormatter();
            symbol = symbolFormatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() < 0) {
                var connector = ctx.connectors().get("error");
                Node<?> node = connector.apply(ctx.getNode(), symbol);
                ctx.setNode(node);
                return parsePosition.getIndex();
            }

            // Save the original parent node before starting the sequence.
            Node<?> parent = ctx.getNode();
            for (TreePrinterParser parser : printerParsers) {
                position = parser.parse(ctx, text, position);
                if (position < 0) {
                    // Restore context on failure before returning.
                    ctx.setNode(parent);
                    return position;
                }
            }
            return position;
        }
    }

    static class ChildrenPrinterParser implements TreePrinterParser {

        private final CompositePrinterParser prefix;
        private final CompositePrinterParser postfix;
        private final NodePrinterParser nodePrinterParser;
        private final LiteralPrinterParser separator;

        ChildrenPrinterParser(String separator) {
            this.nodePrinterParser = null;
            this.separator = new LiteralPrinterParser(separator);
            this.prefix = null;
            this.postfix = null;
        }

        ChildrenPrinterParser(CompositePrinterParser prefix,
                              CompositePrinterParser postfix,
                              LiteralPrinterParser separator) {
            this.prefix = prefix;
            this.postfix = postfix;
            this.separator = separator;
            this.nodePrinterParser = null;
        }

        ChildrenPrinterParser(NodePrinterParser printerParser,
                              ChildrenPrinterParser childrenPrinterParser) {
            this.separator = childrenPrinterParser.separator;
            this.nodePrinterParser = printerParser;
            this.prefix = childrenPrinterParser.prefix;
            this.postfix = childrenPrinterParser.postfix;
        }

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            if (prefix != null) {
                if (!prefix.format(ctx, buf)) {
                    return false;
                }
            }
            Node<?> parent = ctx.getNode();
            for (int i = 0; i < parent.size(); i++) {
                var child = parent.getChild(i);
                ctx.setNode(child);
                if (!nodePrinterParser.format(ctx, buf)) {
                    ctx.setNode(parent);
                    return false;
                }
                if (i < parent.size() - 1) {
                    separator.format(null, buf);
                }
            }
            ctx.setNode(parent);
            if (postfix != null) {
                if (!postfix.format(ctx, buf)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            Node<?> parent = ctx.getNode();

            //Check the prefix
            if (prefix != null) {
                position = prefix.parse(ctx, text, position);
                if (position < 0) {
                    return position;
                }
            }

            int result;
            do {
                ctx.setNode(parent);
                result = nodePrinterParser.parse(ctx, text, position);
                if (result < 0) {
                    break;
                }
                position = result;
                result = separator.parse(ctx, text, position);
                if (result > 0) {
                    position = result;
                }
                //No separator after a child means we are done.
            } while (result >= 0);
            ctx.setNode(parent);

            //Check postfix
            if (postfix != null) {
                position = postfix.parse(ctx, text, position);
                if (position < 0) {
                    return position;
                }
            }

            return position;
        }
    }

    static class LiteralPrinterParser implements TreePrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(TreeFormatContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(TreeParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }
    }

    static class IntegerRulePrinterParser implements TreePrinterParser {

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            Node<?> node = ctx.getNode();
            return switch (node) {
                case Node.Rule<?> ruleNode -> {
                    buf.append(ruleNode.index());
                    yield true;
                }
                case null, default -> false;
            };

        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            int numberEnd = peek(text, position);
            if (numberEnd < 0) {
                return numberEnd;
            }
            Node<?> parent = ctx.getNode();
            int ruleIndex = Integer.parseInt(text.subSequence(position, numberEnd).toString());
            var connector = ctx.connectors().get("rule");
            Node<?> node = connector.apply(parent, ruleIndex);
            ctx.setNode(node);
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

    static class StringRuleNamePrinterParser implements TreePrinterParser {

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            Node<?> node = ctx.getNode();
            return switch (node) {
                case Node.Rule<?> ruleNode -> {
                    String[] ruleNames = ctx.recognizer() == null ?
                            new String[0] :
                            ctx.recognizer().getRuleNames();
                    if (ruleNode.index() >= ruleNames.length) {
                        yield false;
                    }
                    buf.append(ruleNames[ruleNode.index()]);
                    yield true;
                }
                case null, default -> false;
            };
        }


        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            String[] ruleNames = ctx.recognizer() == null ?
                    new String[0] :
                    ctx.recognizer().getRuleNames();
            int index = findRuleIndex(text, ruleNames, position);
            if (index < 0) {
                return ~position;
            }
            var connector = ctx.connectors().get("rule");
            Node<?> node = connector.apply(ctx.getNode(), index);
            ctx.setNode(node);
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

    static class RulePrinterParser implements TreePrinterParser {

        private final TreePrinterParser[] printerParsers;

        RulePrinterParser() {
            printerParsers = new TreePrinterParser[]{new StringRuleNamePrinterParser(),
                    new IntegerRulePrinterParser()};
        }

        @Override
        public boolean format(TreeFormatContext context, StringBuilder buf) {
            for (TreePrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(TreeParseContext context, CharSequence text, int position) {
            for (TreePrinterParser parser : printerParsers) {
                int end = parser.parse(context, text, position);
                if (end > 0) {
                    return end;
                }
            }
            return ~position;
        }

    }
}