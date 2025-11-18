package io.github.airbag.symbol;

import io.github.airbag.symbol.SymbolFormatterBuilder.CompositePrinterParser;
import org.antlr.v4.runtime.Vocabulary;

import java.text.ParsePosition;
import java.util.*;

import static java.util.Map.entry;

/**
 * A formatter for ANTLR {@link Symbol} objects.
 * <p>
 * This class can be used to format a symbol into a string representation, or to parse a string into a symbol.
 * The format of the string is defined by a {@link SymbolFormatterBuilder}.
 */
public class SymbolFormatter {

    /**
     * A formatter that mimics the default ANTLR {@link Object#toString} of the {@link org.antlr.v4.runtime.CommonToken} format.
     * <p>
     * This formatter provides a detailed, parsable representation of a symbol, including all its core attributes.
     * The format is: {@code "[@<index>,<start>:<stop>='<text>',<<type>>,<line>:<pos>]"}
     * <ul>
     *     <li>{@code <index>}: The symbol's index within the stream. See {@link Symbol#index()}.</li>
     *     <li>{@code <start>:<stop>}: The start and stop character indices in the input stream. See {@link Symbol#start()} and {@link Symbol#stop()}.</li>
     *     <li>{@code '<text>'}: The matched text of the symbol, with special characters escaped. See {@link Symbol#text()}.</li>
     *     <li>{@code <<type>>}: The symbol's type, resolved first as a literal name (e.g., {@code '='}), then as a symbolic name (e.g., {@code ID}).</li>
     *     <li>{@code <line>:<pos>}: The line number and character position within the line. See {@link Symbol#line()} and {@link Symbol#position()}.</li>
     * </ul>
     * <p><b>Example:</b>
     * <pre>{@code
     * // Given a symbol representing an identifier "user"
     *  Symbol symbol = Symbol.of().type(MyLexer.ID).text("user");
     * // Assuming index=10, start=50, stop=53, line=5, pos=4
     *
     *  String formatted = SymbolFormatter.ANTLR.withVocabulary(MyLexer.VOCABULARY).format(symbol);
     * // formatted will be: "[@10,50:53='user',<ID>,5:4]"
     * }</pre>
     * This format is particularly useful for debugging and logging, as it captures the full context of a symbol.
     */
    public static final SymbolFormatter ANTLR = new SymbolFormatterBuilder().appendLiteral("[@")
            .appendInteger(SymbolField.INDEX)
            .appendLiteral(",")
            .appendInteger(SymbolField.START)
            .appendLiteral(":")
            .appendInteger(SymbolField.STOP)
            .appendLiteral("='")
            .appendText(TextOption.ESCAPED)
            .appendLiteral("',<")
            .appendType(TypeFormat.LITERAL_FIRST)
            .appendLiteral(">")
            .startOptional()
            .appendLiteral(",channel=")
            .appendInteger(SymbolField.CHANNEL, true)
            .endOptional()
            .appendLiteral(",")
            .appendInteger(SymbolField.LINE)
            .appendLiteral(":")
            .appendInteger(SymbolField.POSITION)
            .appendLiteral("]")
            .toFormatter();

    /**
     * A formatter for the symbol's literal name (e.g., '=', '*').
     */
    private static final SymbolFormatter LITERAL = new SymbolFormatterBuilder().appendLiteralType()
            .startOptional()
            .appendLiteral(":")
            .appendInteger(SymbolField.CHANNEL, true)
            .endOptional()
            .toFormatter();

    /**
     * A formatter for the special end-of-file symbol.
     */
    private static final SymbolFormatter EOF = new SymbolFormatterBuilder().appendEOF()
            .toFormatter();

    /**
     * A formatter for the symbol's symbolic name and text (e.g., "(ID 'myVar')").
     */
    private static final SymbolFormatter SYMBOLIC = new SymbolFormatterBuilder().appendLiteral("(")
            .appendWhitespace()
            .appendType(TypeFormat.SYMBOLIC_FIRST)
            .startOptional()
            .appendLiteral(":")
            .appendInteger(SymbolField.CHANNEL, true)
            .endOptional()
            .appendWhitespace(" ")
            .appendLiteral("'")
            .appendText(new TextOption().withDefaultValue("")
                    .withEscapeChar('\\')
                    .withEscapeMap(Map.ofEntries(entry('\n', 'n'),
                            entry('\r', 'r'),
                            entry('\t', 't'),
                            entry('\\', '\\'),
                            entry('\'', '\''))))
            .appendLiteral("'")
            .appendWhitespace()
            .appendLiteral(")")
            .toFormatter();

    /**
     * A simple, human-readable formatter with intelligent alternatives.
     * <p>
     * This formatter tries different representations in a specific order, making it
     * flexible for a variety of symbol types. The order of preference is:
     * <ol>
     *     <li><b>EOF Symbol:</b> If the symbol is the end-of-file marker, it is formatted as the string {@code "EOF"}.</li>
     *     <li><b>Literal Name:</b> If the symbol has a literal name in the vocabulary (e.g., a keyword or operator),
     *     it is formatted as that name, including quotes (e.g., {@code "'='"}).</li>
     *     <li><b>Symbolic Name and Text:</b> If the symbol has no literal name (e.g., an identifier or number),
     *     it is formatted as {@code "(<SymbolicName> '<text>')"}.</li>
     * </ol>
     * For literal and symbolic formats, if the symbol is on a non-default channel, the channel
     * number will be appended (e.g., {@code "'+':3"} or {@code "(ID:3 'myVar')"}).
     * <p>
     * This is the most commonly used formatter for simple, readable output.
     * <p><b>Examples:</b>
     * <pre>{@code
     * Symbol eof = new CommonSymbol(Symbol.EOF);
     * Symbol plus = new CommonSymbol(MyLexer.PLUS, "+");
     * Symbol id = new CommonSymbol(MyLexer.ID, "myVar");
     * Symbol hidden = new CommonSymbol(MyLexer.COMMENT, "//comment");
     * hidden.setChannel(Symbol.HIDDEN_CHANNEL); // Assuming HIDDEN_CHANNEL = 1
     *
     * SymbolFormatter formatter = SymbolFormatter.SIMPLE.withVocabulary(MyLexer.VOCABULARY);
     *
     * formatter.format(eof);   // Returns "EOF"
     * formatter.format(plus);  // Returns "'+'"
     * formatter.format(id);    // Returns "(ID 'myVar')"
     * formatter.format(hidden); // Returns "(COMMENT:1 '//comment')"
     * }</pre>
     */
    public static final SymbolFormatter SIMPLE = EOF.withAlternative(LITERAL)
            .withAlternative(SYMBOLIC);

    /**
     * The chain of parsers to attempt in order.
     */
    private final List<CompositePrinterParser> printerParsers;

    /**
     * The set of all symbol fields this formatter can process.
     */
    private final Set<SymbolField<?>> fields;

    /**
     * The vocabulary for resolving symbol type names.
     */
    private final Vocabulary vocabulary;

    /**
     * Constructs a new SymbolFormatter.
     *
     * @param printerParser The printer/parser to use for formatting and parsing.
     * @param fields        The fields that are used by this formatter.
     */
    SymbolFormatter(CompositePrinterParser printerParser,
                    Set<SymbolField<?>> fields,
                    Vocabulary vocabulary) {
        this(List.of(printerParser), fields, vocabulary);
    }

    private SymbolFormatter(List<CompositePrinterParser> printerParsers,
                            Set<SymbolField<?>> fields,
                            Vocabulary vocabulary) {
        this.printerParsers = printerParsers;
        this.fields = Set.copyOf(fields);
        this.vocabulary = vocabulary;
    }

    /**
     * Creates a {@link SymbolFormatter} from a pattern string.
     * <p>
     * This factory method provides a convenient way to define a symbol format using a
     * single pattern string, similar to date and time formatting patterns. It is a
     * concise alternative to programmatically chaining individual components with a
     * {@link SymbolFormatterBuilder}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Creates a formatter that represents a symbol as "SYMBOLIC_NAME:'text'"
     * SymbolFormatter formatter = SymbolFormatter.ofPattern("s:'x'");
     *
     * // Example formatting:
     * // Given a symbol with symbolic name "ID" and text "user",
     * // the output would be: "ID:'user'"
     * }</pre>
     *
     * <h3>TreePatternBuilder Syntax</h3>
     * The pattern allows you to specify which symbol fields to include, along with any
     * literal text, in the desired order.
     *
     * <h3>TreePatternBuilder Letters</h3>
     * The following pattern letters are available:
     * <table border="1" cellpadding="5" summary="TreePatternBuilder Letters">
     *   <tr><th>Letter(s)</th><th>Component</th><th>Description</th></tr>
     *   <tr>
     *     <td><b>I</b></td>
     *     <td>Symbol Type (Integer)</td>
     *     <td>Always formats the symbol's integer type. Parses an integer and sets it as the symbol type.</td>
     *   </tr>
     *   <tr>
     *     <td><b>s / S</b></td>
     *     <td>Symbol Type (Symbolic)</td>
     *     <td>
     *         <b>s (Strict):</b> Formats the symbolic name of the symbol (e.g., "ID"). Fails if no symbolic name is available. Parses a symbolic name and resolves it to a symbol type.<br>
     *         <b>S (Lenient):</b> Formats the symbolic name if available; otherwise, formats the literal name. Parses either a symbolic or literal name.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>l / L</b></td>
     *     <td>Symbol Type (Literal)</td>
     *     <td>
     *         <b>l (Strict):</b> Formats the literal name of the symbol (e.g., "'='" ). Fails if no literal name is available. Parses a literal name and resolves it to a symbol type.<br>
     *         <b>L (Lenient):</b> Formats the literal name if available; otherwise, formats the symbolic name. Parses either a literal or symbolic name.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>x / X</b></td>
     *     <td>Symbol Text</td>
     *     <td>
     *         <b>x (Strict):</b> Formats the symbol's text without any escaping. Parses text until the next component.<br>
     *         <b>X (Lenient):</b> Formats the symbol's text with escaping for special characters. Parses escaped text.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>n / N</b></td>
     *     <td>Symbol Index</td>
     *     <td>
     *         <b>n (Strict):</b> Formats the symbol's index. Fails if the index is the default value (-1). Parses an integer for the symbol index.<br>
     *         <b>N (Lenient):</b> Always formats the symbol's index. Parses an integer for the symbol index.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>b / B</b></td>
     *     <td>Start Index</td>
     *     <td>
     *         <b>b (Strict):</b> Formats the start index. Fails if the index is the default value (-1). Parses an integer for the start index.<br>
     *         <b>B (Lenient):</b> Always formats the start index. Parses an integer for the start index.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>e / E</b></td>
     *     <td>Stop Index</td>
     *     <td>
     *         <b>e (Strict):</b> Formats the stop index. Fails if the index is the default value (-1). Parses an integer for the stop index.<br>
     *         <b>E (Lenient):</b> Always formats the stop index. Parses an integer for the stop index.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>c / C</b></td>
     *     <td>Channel</td>
     *     <td>
     *         <b>c (Strict):</b> Formats the channel number. Fails if the channel is the default channel (0). Parses a non-zero integer for the channel.<br>
     *         <b>C (Lenient):</b> Always formats the channel number, including the default channel. Parses any integer for the channel.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>p / P</b></td>
     *     <td>Char Position in Line</td>
     *     <td>
     *         <b>p (Strict):</b> Formats the character position in line. Fails if the position is the default value (-1). Parses an integer for the position.<br>
     *         <b>P (Lenient):</b> Always formats the character position in line. Parses an integer for the position.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>r / R</b></td>
     *     <td>Line Number</td>
     *     <td>
     *         <b>r (Strict):</b> Formats the line number. Fails if the line number is the default value (-1). Parses an integer for the line number.<br>
     *         <b>R (Lenient):</b> Always formats the line number. Parses an integer for the line number.
     *     </td>
     *   </tr>
     * </table>
     *
     * <h3>Literals and Quoted Text</h3>
     * Any character in the pattern that is not a recognized pattern letter (and not part of an optional
     * section marker {@code []} or an escape sequence {@code \}) is treated as a literal.
     * For example, in the pattern {@code s:x}, the colon is a literal.
     * <p>
     * To treat a sequence of characters as a single literal, especially if it contains characters
     * that could be interpreted as pattern modifiers, you can enclose the sequence in {@code %} characters.
     * Everything between the opening and closing {@code %} is treated as one literal block.
     * For example, {@code %s%} would result in the literal "s" being printed, not the symbolic name.
     * This is useful for ensuring that text is treated as a literal, regardless of its content.
     *
     * <h3>Optional Sections</h3>
     * Square brackets {@code []} can be used to create an optional section in the pattern.
     * During formatting, if all components within the optional section can be printed, they are.
     * Otherwise, the entire section is skipped. During parsing, the parser will attempt to
     * match the components in the optional section, but if it fails, it will skip the section
     * and continue with the rest of the pattern.
     *
     * <h3>Escaping</h3>
     * The backslash character {@code \} is used as an escape character. It allows individual pattern
     * letters to be treated as literals outside a quoted block. For example, a pattern of {@code \s}
     * will format or parse the literal character 's'. To include a literal backslash, use a double
     * backslash {@code \\}. To include a literal percent sign, use {@code \%}.
     *
     * <h3>Alternatives</h3>
     * Alternatives patterns are separated by {@code |}. This character is non-escapable with this method
     * therefore it is advisable to directly use {@link SymbolFormatterBuilder#appendPattern(String)}
     * if the desired pattern includes this character.
     *
     * @param pattern the pattern string that defines the format.
     * @return a new {@link SymbolFormatter} instance based on the provided pattern.
     * @throws IllegalArgumentException if the pattern string is invalid.
     * @see SymbolFormatterBuilder#appendPattern(String)
     */
    public static SymbolFormatter ofPattern(String pattern) {
        String[] patterns = pattern.split("\\|");
        SymbolFormatter formatter = null;
        for (String singlePattern : patterns) {
            if (formatter == null) {
                formatter = new SymbolFormatterBuilder().appendPattern(singlePattern).toFormatter();
            } else {
                formatter = formatter.withAlternative(new SymbolFormatterBuilder().appendPattern(
                        singlePattern).toFormatter());
            }
        }
        return formatter;
    }

    /**
     * Formats a symbol into a string.
     *
     * @param symbol The symbol to format.
     * @return The formatted string.
     * @throws SymbolFormatterException if the symbol cannot be formatted.
     */
    public String format(Symbol symbol) {
        SymbolFormatContext ctx = new SymbolFormatContext(symbol, vocabulary);
        StringBuilder buf = new StringBuilder();
        boolean success = false;
        for (var printer : printerParsers) {
            if (printer.format(ctx, buf)) {
                success = true;
                break;
            }
            buf.setLength(0);
        }
        if (!success) {
            throw new SymbolFormatterException("Failed to format symbol %s".formatted(symbol));
        }
        return buf.toString();
    }

    /**
     * Parses a string into a single symbol using a strict parsing strategy.
     * <p>
     * This method requires that the <b>entire</b> input string be consumed during the
     * parsing process. It is best used when the input string is expected to be a
     * complete and standalone representation of a single symbol.
     * <p>
     * For more lenient parsing of a symbol from the beginning of a string, or for parsing
     * multiple tokens from a single string, use {@link #parse(CharSequence, FormatterParsePosition)}.
     *
     * @param input The char sequence to parse. Must not be null.
     * @return The parsed symbol.
     * @throws SymbolParseException if the string cannot be parsed or is not fully consumed.
     * @see #parse(CharSequence, FormatterParsePosition)
     */
    public Symbol parse(CharSequence input) {
        Objects.requireNonNull(input);
        FormatterParsePosition position = new FormatterParsePosition(0);
        Symbol token = parse(input, position);
        if (token == null) {
            throw new SymbolParseException(input.toString(),
                    position.getErrorIndex(),
                    position.getMessage());
        }
        if (position.getIndex() != input.length()) {
            int index = position.getIndex();
            String message = "Input '%s>>%s' has trailing unparsed text at position %d".formatted(
                    input.subSequence(0, index),
                    input.subSequence(index, input.length()),
                    index);
            throw new SymbolParseException(input.toString(), position.getIndex(), message);
        }
        return token;
    }

    /**
     * Parses a symbol from a string in a lenient, non-exception-throwing manner.
     * <p>
     * This method attempts to parse a symbol starting at the index specified by the
     * {@link ParsePosition}. It does <b>not</b> require the entire string to be consumed.
     * <p>
     * On success, the parsed {@link Symbol} is returned, and the index of the {@code ParsePosition}
     * is updated to point to the character immediately after the parsed text. The error index
     * is set to -1.
     * <p>
     * On failure, this method returns {@code null} instead of throwing an exception. The
     * index of the {@code ParsePosition} is left unchanged, and the error index is updated
     * to the position where the parse failed.
     * <p>
     * This method is particularly useful for parsing multiple symbols sequentially from a
     * single input string.
     *
     * @param input    The char sequence from which to parse a symbol. Must not be null.
     * @param position The {@link FormatterParsePosition} object that tracks the current parsing
     *                 position and error location. Must not be null.
     * @return The parsed {@link Symbol}, or {@code null} if parsing fails.
     * @see #parse(CharSequence)
     */
    public Symbol parse(CharSequence input, FormatterParsePosition position) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(input, "input");
        int initial = position.getIndex();
        int maxError = -1;
        boolean errorSet = false;

        for (var parser : printerParsers) {
            SymbolParseContext ctx = new SymbolParseContext(parser, vocabulary);
            int current = parser.parse(ctx, input, initial);
            if (current >= 0) {
                position.setIndex(current);
                position.setErrorIndex(-1); // Clear error index on success
                return ctx.resolveFields();
            } else {
                if (!errorSet) {
                    maxError = current;
                    position.setMessage(ctx.getErrorMessage());
                    position.setErrorIndex(~current);
                    errorSet = true;
                } else if (current < maxError) {
                    maxError = current;
                    position.setMessage(ctx.getErrorMessage());
                    position.setErrorIndex(~current);
                } else if (current == maxError) {
                    position.setMessage(position.getMessage()
                            .concat("%n%s".formatted(ctx.getErrorMessage())));
                }
            }
        }

        // All parsers failed return null
        return null;
    }

    public SymbolFormatter withVocabulary(Vocabulary vocabulary) {
        if (Objects.equals(vocabulary, this.vocabulary)) {
            return this;
        }
        return new SymbolFormatter(printerParsers, fields, vocabulary);
    }

    /**
     * Creates a new formatter by adding an alternative format.
     * <p>
     * The resulting formatter will first attempt to format or parse using the
     * formatters from the current instance, in order. If all of those fail,
     * it will then attempt to use the formatters from the {@code alternative} instance.
     * <p>
     * The fields from both formatters are merged. The vocabulary of the current
     * formatter is preferred. If it is null, the alternative's vocabulary is used.
     *
     * @param alternative The formatter to use as a fallback.
     * @return A new {@link SymbolFormatter} with the combined formatting and parsing logic.
     */
    public SymbolFormatter withAlternative(SymbolFormatter alternative) {
        Vocabulary vocabulary = this.vocabulary == null ? alternative.vocabulary : this.vocabulary;
        Set<SymbolField<?>> fields = new HashSet<>(getFields());
        fields.addAll(alternative.getFields());
        List<CompositePrinterParser> printerParsers = new ArrayList<>(this.printerParsers);
        printerParsers.addAll(alternative.printerParsers);
        return new SymbolFormatter(printerParsers, fields, vocabulary);
    }

    /**
     * Gets the set of {@link SymbolField}s that this formatter uses.
     *
     * @return An unmodifiable set of symbol fields.
     */
    public Set<SymbolField<?>> getFields() {
        return fields;
    }

    /**
     * Prints this formatter pattern as string.
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" | ");
        for (CompositePrinterParser parser : printerParsers) {
            joiner.add(parser.toString());
        }
        return joiner.toString();
    }

}