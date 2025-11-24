package io.github.airbag.tree;

import io.github.airbag.tree.NodeFormatterBuilder.NodePrinterParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for creating {@link TreeFormatter} instances.
 * <p>
 * This builder provides a flexible way to define custom formats for converting
 * {@link DerivationTree} objects to and from strings. It works by allowing you to
 * specify a distinct format for each type of node in the tree:
 * <ul>
 *   <li>{@link DerivationTree.Rule} nodes</li>
 *   <li>{@link DerivationTree.Terminal} nodes</li>
 *   <li>{@link DerivationTree.Error} nodes</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * To create a formatter, you instantiate a {@code TreeFormatterBuilder} and use the
 * {@link #onRule(Consumer)}, {@link #onTerminal(Consumer)}, and {@link #onError(Consumer)}
 * methods to define the format for each node type. These methods provide a
 * {@link NodeFormatterBuilder} instance within a lambda, which is used to define the
 * actual format for that node type.
 * <p>
 * Once all node formats are defined, you call {@link #toFormatter()} to create the
 * immutable and thread-safe {@link TreeFormatter} instance.
 *
 * <p><b>Example: LISP-style S-expression Format</b></p>
 * <pre>{@code
 * // This example creates a formatter that represents a tree in a LISP-like format,
 * // such as "(rule (child1) (child2))".
 *
 * TreeFormatter lispFormatter = new TreeFormatterBuilder()
 *     .onRule(ruleNode -> ruleNode
 *         .appendLiteral("(")
 *         .appendRule()
 *         .appendLiteral(" ")
 *         .appendChildren(" ") // Recursively format children, separated by a space
 *         .appendLiteral(")")
 *     )
 *     .onTerminal(terminalNode -> terminalNode
 *         .appendLiteral("(")
 *         .appendSymbol() // Uses the SymbolFormatter; for terminals, often just the text
 *         .appendLiteral(")")
 *     )
 *     .onError(errorNode -> errorNode
 *         .appendLiteral("(<error> ")
 *         .appendSymbol()
 *         .appendLiteral(")")
 *     )
 *     .toFormatter();
 *
 * // The resulting formatter can then be customized with a Recognizer or SymbolFormatter
 * TreeFormatter finalFormatter = lispFormatter.withRecognizer(myRecognizer);
 *
 * // Format a tree to a string
 * String output = finalFormatter.format(myTree);
 * }</pre>
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used to create a formatter and then discarded. The resulting {@link TreeFormatter}
 * objects, however, are immutable and safe for use in multithreaded environments.
 *
 * @see TreeFormatter
 * @see NodeFormatterBuilder
 * @see DerivationTree
 */
public class TreeFormatterBuilder {

    private NodePrinterParser[] rulePrinterParsers;
    private NodePrinterParser[] terminalPrinterParsers;
    private NodePrinterParser[] errorPrinterParsers;
    private NodePrinterParser[] patternPrinterParser;

    /**
     * Defines the format for {@link DerivationTree.Rule} nodes.
     * <p>
     * The provided consumer is passed a {@link NodeFormatterBuilder} instance that can be
     * used to construct the specific format for rule nodes.
     *
     * @param onRule A consumer that accepts a {@link NodeFormatterBuilder} to define the format.
     * @return This builder, for chaining.
     */
    public TreeFormatterBuilder onRule(Consumer<NodeFormatterBuilder> onRule) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onRule.accept(builder);
        rulePrinterParsers = builder.printerParsers();
        return this;
    }

    /**
     * Defines the format for {@link DerivationTree.Terminal} nodes.
     * <p>
     * The provided consumer is passed a {@link NodeFormatterBuilder} instance that can be
     * used to construct the specific format for terminal nodes.
     *
     * @param onTerminal A consumer that accepts a {@link NodeFormatterBuilder} to define the format.
     * @return This builder, for chaining.
     */
    public TreeFormatterBuilder onTerminal(Consumer<NodeFormatterBuilder> onTerminal) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onTerminal.accept(builder);
        terminalPrinterParsers = builder.printerParsers();
        return this;
    }

    /**
     * Defines the format for {@link DerivationTree.Error} nodes.
     * <p>
     * The provided consumer is passed a {@link NodeFormatterBuilder} instance that can be
     * used to construct the specific format for error nodes.
     *
     * @param onError A consumer that accepts a {@link NodeFormatterBuilder} to define the format.
     * @return This builder, for chaining.
     */
    public TreeFormatterBuilder onError(Consumer<NodeFormatterBuilder> onError) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onError.accept(builder);
        errorPrinterParsers = builder.printerParsers();
        return this;
    }

    public TreeFormatterBuilder onPattern(Consumer<NodeFormatterBuilder> onPattern) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onPattern.accept(builder);
        patternPrinterParser = builder.printerParsers();
        return this;
    }

    /**
     * Builds the immutable {@link TreeFormatter} from the configured node formats.
     * <p>
     * If a format for a specific node type (e.g., rule, terminal) has not been defined,
     * it will default to an empty format. This may lead to unexpected behavior or errors
     * during formatting or parsing if that node type is encountered.
     *
     * @return The newly created, immutable {@link TreeFormatter} instance.
     */
    public TreeFormatter toFormatter() {
        return new TreeFormatter(new TreePrinterParser(rulePrinterParsers,
                terminalPrinterParsers,
                errorPrinterParsers,
                patternPrinterParser));
    }

    static class TreePrinterParser implements NodePrinterParser {

        private final CompositePrinterParser rulePrinterParser;
        private final CompositePrinterParser terminalPrinterParser;
        private final CompositePrinterParser errorPrinterParser;
        private final CompositePrinterParser patternPrinterParser;

        public TreePrinterParser(NodePrinterParser[] rulePrinterParsers,
                                 NodePrinterParser[] terminalPrinterParsers,
                                 NodePrinterParser[] errorPrinterParsers,
                                 NodePrinterParser[] patternPrinterParsers) {

            for (int i = 0; i < rulePrinterParsers.length; i++) {
                if (rulePrinterParsers[i] instanceof NodeFormatterBuilder.ChildrenPrinterParser(
                        NodePrinterParser separator
                )) {
                    rulePrinterParsers[i] = new ChildrenPrinterParser(this, separator);
                }
            }
            this.rulePrinterParser = new CompositePrinterParser(rulePrinterParsers);
            this.terminalPrinterParser = new CompositePrinterParser(terminalPrinterParsers);
            this.errorPrinterParser = new CompositePrinterParser(errorPrinterParsers);
            this.patternPrinterParser = new CompositePrinterParser(patternPrinterParsers);
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            return switch (ctx.node()) {
                case DerivationTree.Rule ignored -> rulePrinterParser.format(ctx, buf);
                case DerivationTree.Terminal ignored -> terminalPrinterParser.format(ctx, buf);
                case DerivationTree.Error ignored -> errorPrinterParser.format(ctx, buf);
                case DerivationTree.Pattern ignored -> patternPrinterParser.format(ctx, buf);
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int result;
            RootParseContext.Terminal terminalCtx = ctx.root().new Terminal(ctx);
            result = terminalPrinterParser.parse(terminalCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(terminalCtx);
                return result;
            }
            RootParseContext.Rule ruleCtx = ctx.root().new Rule(ctx);
            result = rulePrinterParser.parse(ruleCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(ruleCtx);
                return result;
            }
            RootParseContext.Pattern patternCtx = ctx.root().new Pattern(ctx);
            result = patternPrinterParser.parse(patternCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(patternCtx);
                return result;
            }
            RootParseContext.Error errorCtx = ctx.root().new Error(ctx);
            result = errorPrinterParser.parse(errorCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(errorCtx);
            }
            return result;
        }

    }

    static class ChildrenPrinterParser implements NodePrinterParser {

        private final TreePrinterParser treePrinterParser;
        private final NodePrinterParser separator;

        ChildrenPrinterParser(TreePrinterParser treePrinterParser, NodePrinterParser separator) {
            this.treePrinterParser = treePrinterParser;
            this.separator = separator;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            if (ctx.doNotRecurse()) {
                // Trim trailing whitespace
                int i = buf.length() - 1;
                while (i >= 0 && Character.isWhitespace(buf.charAt(i))) {
                    i--;
                }
                buf.setLength(i + 1);
                return true;
            }
            var current = ctx.node();
            for (int i = 0; i < current.size(); i++) {
                var child = current.getChild(i);
                ctx.setNode(child);
                if (!treePrinterParser.format(ctx, buf)) {
                    ctx.setNode(current);
                    return false;
                }
                if (i + 1 != current.size()) {
                    separator.format(ctx, buf);
                }
            }
            ctx.setNode(current);
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int result = position;
            do {
                position = treePrinterParser.parse(ctx, text, position);
                if (position < 0) {
                    return result;
                }
                result = position;
                position = separator.parse(ctx, text, position);
            } while (position < text.length() && position > 0);
            return result;
        }
    }

    static class CompositePrinterParser implements NodePrinterParser {

        private final NodePrinterParser[] printerParsers;

        CompositePrinterParser(NodePrinterParser[] printerParsers) {
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            int initialLength = buf.length();
            for (NodePrinterParser printer : printerParsers) {
                if (!printer.format(ctx, buf)) {
                    buf.setLength(initialLength);
                    return false;
                }
            }
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            for (NodePrinterParser parser : printerParsers) {
                position = parser.parse(ctx, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }
    }
}