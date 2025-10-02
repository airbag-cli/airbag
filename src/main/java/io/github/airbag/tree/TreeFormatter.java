package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolException;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolFormatterBuilder;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static java.util.Map.entry;

/**
 * A formatter for converting tree structures, specifically {@link ConcreteSyntaxTree},
 * to and from their string representations.
 * <p>
 * This class is the engine that drives the formatting (printing) and parsing of
 * tree nodes. It is designed to be immutable and thread-safe, making it safe to
 * share and reuse instances.
 * <p>
 * {@code TreeFormatter} instances are created using a {@link TreeFormatterBuilder},
 * which provides a flexible fluent API for defining custom tree formats. A tree
 * formatter relies on several components:
 * <ul>
 *   <li>A main tree structure format, defined by the sequence of printers and
 *       parsers in the {@link TreeFormatterBuilder}.</li>
 *   <li>A {@link SymbolFormatter} for handling the representation of terminal
 *       (leaf) nodes.</li>
 *   <li>A {@link SymbolFormatter} for handling the representation of error nodes.</li>
 *   <li>An optional ANTLR {@link Recognizer} to resolve rule indices to names and
 *       provide a {@link Vocabulary} to the symbol formatters.</li>
 * </ul>
 *
 * @see TreeFormatterBuilder
 * @see ConcreteSyntaxTree
 * @see SymbolFormatter
 */
public class TreeFormatter {

    /**
     * A simple, human-readable formatter that represents the tree in a LISP-style,
     * parenthesized format.
     * <p>
     * The format is defined as {@code "(<ruleName> <child1> <child2> ...)"}. Each node
     * is recursively formatted using this structure. Terminal nodes are formatted
     * using {@link SymbolFormatter#SIMPLE}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Assuming a tree for an expression "1 + 2" with root rule "expr"
     * // and children for "1", "+", and "2".
     *
     * String formatted = TreeFormatter.SIMPLE.format(tree);
     *
     * // formatted will be: "(expr (INT '1') '+' (INT '2'))"
     * }</pre>
     * This is a common default for visualizing tree structures.
     */
    public static final TreeFormatter SIMPLE = new TreeFormatterBuilder().appendLiteral("(")
            .appendRule()
            .appendLiteral(" ")
            .appendChildren(" ")
            .appendLiteral(")")
            .toFormatter();

    /**
     * The main parser/formatter for the tree structure.
     */
    private final TreeFormatterBuilder.NodePrinterParser printerParser;
    /**
     * The ANTLR recognizer for resolving rule and token names.
     */
    private final Recognizer<?, ?> recognizer;
    /**
     * The formatter for terminal (leaf) nodes.
     */
    private final SymbolFormatter terminalFormatter;
    /**
     * The formatter for error nodes.
     */
    private final SymbolFormatter errorFormatter;

    /**
     * Constructs a new formatter with default settings.
     *
     * @param printerParser The main printer/parser for the tree structure.
     */
    TreeFormatter(TreeFormatterBuilder.NodePrinterParser printerParser) {
        this.printerParser = printerParser;
        this.recognizer = null;
        this.terminalFormatter = SymbolFormatter.SIMPLE;
        this.errorFormatter = SymbolFormatter.ofPattern("(<error> l)|(<error> (S 'X'))");
    }

    /**
     * Constructs a new formatter with all components specified.
     *
     * @param printerParser     The main printer/parser for the tree structure.
     * @param recognizer        The ANTLR recognizer.
     * @param terminalFormatter The formatter for terminal nodes.
     * @param errorFormatter    The formatter for error nodes.
     */
    TreeFormatter(TreeFormatterBuilder.NodePrinterParser printerParser,
                  Recognizer<?, ?> recognizer,
                  SymbolFormatter terminalFormatter,
                  SymbolFormatter errorFormatter) {
        this.printerParser = printerParser;
        this.recognizer = recognizer;
        this.terminalFormatter = terminalFormatter;
        this.errorFormatter = errorFormatter;
    }

    /**
     * Formats a {@link ConcreteSyntaxTree} into a string according to the rules
     * defined in this formatter.
     *
     * @param tree The concrete syntax tree to format.
     * @return The formatted string representation of the tree.
     * @throws SymbolException if the tree cannot be formatted, for example, if a
     *         component of the format cannot be applied to a given node.
     */
    public String format(ConcreteSyntaxTree tree) {
        StringBuilder buf = new StringBuilder();
        TreeFormatContext ctx = new TreeFormatContext(new AtomicReference<>(tree),
                recognizer,
                terminalFormatter,
                errorFormatter);
        if (!printerParser.format(ctx, buf)) {
            buf.setLength(0);
            throw new SymbolException("Failed to format tree %s".formatted(tree));
        }
        return buf.toString();
    }

    /**
     * Parses a string into a {@link ConcreteSyntaxTree} according to the rules
     * defined in this formatter.
     * <p>
     * This method expects the <b>entire</b> input string to be consumed by the
     * parsing process. If any trailing text is left unparsed, the behavior is
     * currently undefined and may result in an error or an incompletely parsed tree.
     *
     * @param text The character sequence to parse.
     * @return The resulting {@link ConcreteSyntaxTree}.
     * @throws RuntimeException if the input text cannot be parsed into a valid tree
     *         structure according to the formatter's rules.
     */
    public ConcreteSyntaxTree parseCST(CharSequence text) {
        Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors = Map.ofEntries(entry("rule",
                        (parent, index) -> ConcreteSyntaxTree.Rule.attachTo((ConcreteSyntaxTree) parent,
                                (int) index)),
                entry("terminal",
                        (parent, symbol) -> ConcreteSyntaxTree.Terminal.attachTo((ConcreteSyntaxTree) parent,
                                (Symbol) symbol)),
                entry("error",
                        (parent, symbol) -> ConcreteSyntaxTree.Error.attachTo((ConcreteSyntaxTree) parent,
                                (Symbol) symbol)));
        TreeParseContext ctx = new TreeParseContext(new AtomicReference<>(),
                recognizer,
                connectors,
                terminalFormatter,
                errorFormatter);
        int result = printerParser.parse(ctx, text, 0);
        if (result < 0) {
            throw new RuntimeException("Cannot parse");
        }
        return (ConcreteSyntaxTree) ctx.getNode();
    }

    /**
     * Returns a new {@link TreeFormatter} instance configured with the specified
     * ANTLR {@link Recognizer}.
     * <p>
     * The recognizer is essential for formats that involve rule names, as it provides
     * the mapping from rule indices to names. It also supplies the {@link Vocabulary}
     * to the underlying terminal and error {@link SymbolFormatter}s, allowing them
     * to resolve token types to their symbolic or literal names.
     * <p>
     * Since {@link TreeFormatter} is immutable, this method returns a new instance
     * with the updated configuration.
     *
     * @param recognizer The ANTLR recognizer (e.g., a subclass of {@code Parser} or
     *                   {@code Lexer}) to use for resolving names.
     * @return A new formatter instance with the given recognizer.
     */
    public TreeFormatter withRecognizer(Recognizer<?, ?> recognizer) {
        return new TreeFormatter(printerParser,
                recognizer,
                terminalFormatter.withVocabulary(recognizer.getVocabulary()),
                errorFormatter.withVocabulary(recognizer.getVocabulary()));
    }

    /**
     * Returns a new {@link TreeFormatter} instance with a custom formatter for
     * terminal (leaf) nodes.
     * <p>
     * This allows for fine-grained control over how tokens in the tree are
     * represented in the formatted string. For example, you could choose to format
     * terminals using {@link SymbolFormatter#ANTLR} for detailed debug output.
     * <p>
     * The vocabulary from the current formatter's recognizer (if any) will be
     * passed to the new terminal formatter.
     * <p>
     * Since {@link TreeFormatter} is immutable, this method returns a new instance.
     *
     * @param terminalFormatter The {@link SymbolFormatter} to use for terminal nodes.
     * @return A new formatter instance with the specified terminal formatter.
     */
    public TreeFormatter withTerminalFormatter(SymbolFormatter terminalFormatter) {
        return new TreeFormatter(printerParser,
                recognizer,
                terminalFormatter.withVocabulary(getVocabulary()),
                errorFormatter.withVocabulary(getVocabulary()));
    }

    /**
     * Returns a new {@link TreeFormatter} instance with a custom formatter for
     * error nodes.
     * <p>
     * This allows for customizing the string representation of error nodes within
     * the tree, which can be useful for logging and debugging parsing errors.
     * <p>
     * The vocabulary from the current formatter's recognizer (if any) will be
     * passed to the new error formatter.
     * <p>
     * Since {@link TreeFormatter} is immutable, this method returns a new instance.
     *
     * @param errorFormatter The {@link SymbolFormatter} to use for error nodes.
     * @return A new formatter instance with the specified error formatter.
     */
    public TreeFormatter withErrorFormatter(SymbolFormatter errorFormatter) {
        return new TreeFormatter(printerParser,
                recognizer,
                terminalFormatter.withVocabulary(getVocabulary()),
                errorFormatter.withVocabulary(getVocabulary()));
    }

    private Vocabulary getVocabulary() {
        return recognizer == null ? null : recognizer.getVocabulary();
    }



}