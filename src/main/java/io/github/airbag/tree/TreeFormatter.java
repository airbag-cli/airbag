package io.github.airbag.tree;

import io.github.airbag.symbol.*;
import io.github.airbag.tree.TreeFormatterBuilder.TreePrinterParser;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Vocabulary;

import java.text.ParsePosition;
import java.util.Objects;

/**
 * Formats a {@link DerivationTree} to and from a string representation.
 * <p>
 * This class is the main entry point for serializing a {@link DerivationTree} into a string
 * and deserializing a string back into a {@link DerivationTree}. It is immutable and
 * thread-safe, making it safe to be shared and reused.
 *
 * <h3>Instantiation</h3>
 * Instances of this class are created using a {@link TreeFormatterBuilder}. The builder
 * allows for defining a highly customized format by specifying how different types of
 * nodes (rule, terminal, error) should be represented.
 *
 * <h3>Predefined Formatters</h3>
 * For common use cases, several predefined formatters are provided as static fields:
 * <ul>
 *   <li>{@link #SIMPLE}: A simple LISP-style S-expression format.</li>
 *   <li>{@link #ANTLR}: A format that closely mimics the output of ANTLR's
 *       {@code Trees.toStringTree(tree, parser)}.</li>
 * </ul>
 *
 * <h3>Customization</h3>
 * Once a formatter is created, it can be further customized. The
 * {@link #withSymbolFormatter(SymbolFormatter)} method allows you to control how terminal
 * symbols are formatted, while {@link #withRecognizer(Recognizer)} provides the necessary
 * context (like rule and token names) from an ANTLR parser or lexer.
 *
 * @see TreeFormatterBuilder
 * @see DerivationTree
 * @see SymbolFormatter
 */
public class TreeFormatter {

    /**
     * A formatter that produces a LISP-style S-expression format, similar to the output of
     * {@code org.antlr.v4.runtime.tree.Trees#toStringTree(Tree, Parser)}.
     * <p>
     * <b>Example Output:</b>
     * <pre>{@code (expr (expr (atom 42)) + (expr (atom 1)))}
     * </pre>
     * This format is useful for creating compact, machine-readable representations of a tree.
     * It uses the rule names from the recognizer and the text of the terminal symbols.
     */
    public static final TreeFormatter ANTLR = new TreeFormatterBuilder().onRule(onRule -> onRule.appendLiteral(
                    "(").appendRule().appendLiteral(" ").appendChildren(" ").appendLiteral(")"))
            .onTerminal(
                    NodeFormatterBuilder::appendSymbol)
            .onError(NodeFormatterBuilder::appendSymbol)
            .toFormatter()
            .withSymbolFormatter(new SymbolFormatterBuilder().appendText(
                    TextOption.ESCAPED).toFormatter());

    /**
     * A simple formatter that produces a LISP-style S-expression format.
     * <p>
     * <b>Example Output:</b>
     * <pre>{@code (expr (atom 42) + (atom 1))}
     * </pre>
     * This format is very similar to {@link #ANTLR} but provides a more distinct representation
     * for error nodes, wrapping them in {@code (<error> ...)}.
     */
    public static final TreeFormatter SIMPLE = new TreeFormatterBuilder().onRule(onRule -> onRule.appendLiteral(
                            "(")
                    .appendWhitespace()
                    .appendRule()
                    .appendWhitespace(" ")
                    .appendChildren(sep -> sep.appendWhitespace(" "))
                    .appendWhitespace()
                    .appendLiteral(")"))
            .onTerminal(NodeFormatterBuilder::appendSymbol)
            .onPattern(onPattern -> onPattern.appendLiteral("(")
                    .appendWhitespace()
                    .appendLiteral("<")
                    .appendRule()
                    .appendLiteral(">")
                    .appendWhitespace(" ")
                    .appendLiteral("(")
                    .appendPattern()
                    .appendLiteral(")")
                    .appendWhitespace()
                    .appendLiteral(")"))
            .onError(onError -> onError.appendLiteral("(")
                    .appendWhitespace()
                    .appendLiteral("<error>")
                    .appendWhitespace(" ")
                    .appendSymbol()
                    .appendWhitespace()
                    .appendLiteral(")"))
            .toFormatter();

    private final TreePrinterParser treePrinterParser;
    private final SymbolFormatter symbolFormatter;
    private final Recognizer<?, ?> recognizer;
    private final PatternFormatter patternFormatter;

    TreeFormatter(TreePrinterParser treePrinterParser) {
        this.symbolFormatter = SymbolFormatter.SIMPLE;
        this.patternFormatter = PatternFormatter.SIMPLE;
        this.recognizer = null;
        this.treePrinterParser = treePrinterParser;
    }

    TreeFormatter(SymbolFormatter symbolFormatter,
                  PatternFormatter patternFormatter,
                  Recognizer<?, ?> recognizer,
                  TreePrinterParser treePrinterParser) {
        this.symbolFormatter = symbolFormatter;
        this.patternFormatter = patternFormatter;
        this.recognizer = recognizer;
        this.treePrinterParser = treePrinterParser;
    }

    /**
     * Formats the given {@link DerivationTree} into a string according to this formatter's rules.
     *
     * @param tree The derivation tree to format. Must not be null.
     * @return The formatted string representation of the tree.
     * @throws RuntimeException if formatting fails for any reason (e.g., a required format
     *                          for a node type was not defined).
     */
    public String format(DerivationTree tree) {
        NodeFormatContext ctx = new NodeFormatContext(symbolFormatter, patternFormatter, recognizer);
        ctx.setNode(tree);
        StringBuilder buf = new StringBuilder();
        if (!treePrinterParser.format(ctx, buf)) {
            throw new RuntimeException("Cannot format %s".formatted(tree));
        }
        return buf.toString();
    }

    /**
     * Parses a character sequence into a {@link DerivationTree}.
     * <p>
     * This method expects to consume the entire input string. If any part of the string
     * cannot be parsed or if there is trailing text, it will throw an exception.
     *
     * @param text The character sequence to parse.
     * @return The parsed {@link DerivationTree}.
     * @throws TreeParseException if parsing fails or the entire input is not consumed.
     */
    public DerivationTree parse(CharSequence text) {
        FormatterParsePosition parsePosition = new FormatterParsePosition(0);
        DerivationTree tree = parse(text, parsePosition);
        if (parsePosition.getErrorIndex() >= 0) {
            throw new TreeParseException(text.toString(),
                    parsePosition.getErrorIndex(),
                    parsePosition.getMessage());
        }
        if (parsePosition.getIndex() != text.length()) {
            throw new TreeParseException("Text has unparsed trailing text at %d".formatted(
                    parsePosition.getIndex()));
        }
        return tree;
    }

    /**
     * Parses a character sequence into a {@link DerivationTree}, starting at a given position.
     * <p>
     * This is the core parsing method. It attempts to deserialize a tree from the given
     * text, updating the {@link ParsePosition} to indicate success or failure.
     *
     * @param text     The character sequence to parse. Must not be null.
     * @param position The {@link FormatterParsePosition} object. On input, {@code getIndex()} is the
     *                 starting position. On success, {@code getIndex()} is updated to the
     *                 position after the parsed text. On failure, {@code getErrorIndex()}
     *                 is set to the position where the error occurred, and the method returns {@code null}.
     * @return The parsed {@link DerivationTree}, or {@code null} if parsing fails.
     */
    public DerivationTree parse(CharSequence text, FormatterParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        RootParseContext rootCtx = new RootParseContext(symbolFormatter, patternFormatter, recognizer);
        int result = treePrinterParser.parse(rootCtx, text, position.getIndex());
        if (result < 0) {
            position.setErrorIndex(rootCtx.getMaxError());
            position.setMessage(rootCtx.getErrorMessages());
            return null;
        }
        position.setIndex(result);
        return rootCtx.resolve();
    }

    /**
     * Creates a new {@link TreeFormatter} instance with the specified {@link SymbolFormatter}.
     * <p>
     * This allows for customizing how terminal symbols (tokens) within the tree are rendered
     * without changing the overall tree structure format. The new symbol formatter will automatically
     * inherit the vocabulary from this formatter's recognizer, if present.
     *
     * @param formatter The {@link SymbolFormatter} to use for formatting terminal nodes.
     * @return A new, configured {@link TreeFormatter} instance.
     */
    public TreeFormatter withSymbolFormatter(SymbolFormatter formatter) {
        return new TreeFormatter(formatter.withVocabulary(getVocabulary()),
                patternFormatter.withSymbolFormatter(symbolFormatter),
                recognizer,
                treePrinterParser);
    }

    private Vocabulary getVocabulary() {
        return recognizer == null ? null : recognizer.getVocabulary();
    }

    /**
     * Creates a new {@link TreeFormatter} instance with the specified ANTLR {@link Recognizer}.
     * <p>
     * The recognizer (typically a parser) provides the vocabulary needed to resolve rule indices
     * to rule names and token types to symbolic or literal names. Setting a recognizer is
     * essential for formats that use names instead of integer indices (e.g., {@code "expr"} instead of {@code 0}).
     *
     * @param recognizer The ANTLR recognizer (e.g., a {@code Parser} instance) to provide context.
     * @return A new, configured {@link TreeFormatter} instance.
     */
    public TreeFormatter withRecognizer(Recognizer<?, ?> recognizer) {
        if (recognizer == null) {
            return new TreeFormatter(symbolFormatter.withVocabulary(null), patternFormatter.withRecognizer(null), null, treePrinterParser);
        }
        return new TreeFormatter(symbolFormatter.withVocabulary(recognizer.getVocabulary()),
                patternFormatter.withRecognizer(recognizer),
                recognizer,
                treePrinterParser);
    }

    public SymbolFormatter getSymbolFormatter() {
        return symbolFormatter;
    }

}