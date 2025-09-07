package io.github.airbag.token.format;

import io.github.airbag.token.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.text.ParsePosition;
import java.util.*;

import static java.util.Map.entry;

/**
 * A formatter for ANTLR {@link Token} objects.
 * <p>
 * This class can be used to format a token into a string representation, or to parse a string into a token.
 * The format of the string is defined by a {@link TokenFormatterBuilder}.
 */
public class TokenFormatter {

    /**
     * A formatter that mimics the default ANTLR {@link Token#toString} format.
     * <p>
     * This formatter provides a detailed, parsable representation of a token, including all its core attributes.
     * The format is: {@code "[@<index>,<start>:<stop>='<text>',<<type>>,<line>:<pos>]"}
     * <ul>
     *     <li>{@code <index>}: The token's index within the stream. See {@link Token#getTokenIndex()}.</li>
     *     <li>{@code <start>:<stop>}: The start and stop character indices in the input stream. See {@link Token#getStartIndex()} and {@link Token#getStopIndex()}.</li>
     *     <li>{@code '<text>'}: The matched text of the token, with special characters escaped. See {@link Token#getText()}.</li>
     *     <li>{@code <<type>>}: The token's type, resolved first as a literal name (e.g., {@code '='}), then as a symbolic name (e.g., {@code ID}).</li>
     *     <li>{@code <line>:<pos>}: The line number and character position within the line. See {@link Token#getLine()} and {@link Token#getCharPositionInLine()}.</li>
     * </ul>
     * <p><b>Example:</b>
     * <pre>{@code
     * // Given a token representing an identifier "user"
     *  Token token = new CommonToken(MyLexer.ID, "user");
     * // Assuming index=10, start=50, stop=53, line=5, pos=4
     *
     *  String formatted = TokenFormatter.ANTLR.withVocabulary(MyLexer.VOCABULARY).format(token);
     * // formatted will be: "[@10,50:53='user',<ID>,5:4]"
     * }</pre>
     * This format is particularly useful for debugging and logging, as it captures the full context of a token.
     */
    public static final TokenFormatter ANTLR = new TokenFormatterBuilder().appendLiteral("[@")
            .appendInteger(TokenField.INDEX)
            .appendLiteral(",")
            .appendInteger(TokenField.START)
            .appendLiteral(":")
            .appendInteger(TokenField.STOP)
            .appendLiteral("='")
            .appendText(TextOption.ESCAPED)
            .appendLiteral("',<")
            .appendType(TypeFormat.LITERAL_FIRST)
            .appendLiteral(">")
            .startOptional()
            .appendLiteral(",channel=")
            .appendInteger(TokenField.CHANNEL, true)
            .endOptional()
            .appendLiteral(",")
            .appendInteger(TokenField.LINE)
            .appendLiteral(":")
            .appendInteger(TokenField.POSITION)
            .appendLiteral("]")
            .toFormatter();

    /**
     * A formatter for the token's literal name (e.g., '=', '*').
     */
    private static final TokenFormatter LITERAL = new TokenFormatterBuilder().appendLiteralType()
            .toFormatter();

    /**
     * A formatter for the special end-of-file token.
     */
    private static final TokenFormatter EOF = new TokenFormatterBuilder().appendEOF().toFormatter();

    /**
     * A formatter for the token's symbolic name and text (e.g., "(ID 'myVar')").
     */
    private static final TokenFormatter SYMBOLIC = new TokenFormatterBuilder().appendLiteral("(")
            .appendSymbolicType()
            .appendLiteral(" '")
            .appendText(new TextOption().withDefaultValue("")
                    .withEscapeChar('\\')
                    .withEscapeMap(Map.ofEntries(entry('\n', 'n'),
                            entry('\r', 'r'),
                            entry('\t', 't'),
                            entry('\\', '\\'),
                            entry('\'', '\''))))
            .appendLiteral("')")
            .toFormatter();

    /**
     * A simple, human-readable formatter with intelligent alternatives.
     * <p>
     * This formatter tries different representations in a specific order, making it
     * flexible for a variety of token types. The order of preference is:
     * <ol>
     *     <li><b>EOF Token:</b> If the token is the end-of-file marker, it is formatted as the string {@code "EOF"}.</li>
     *     <li><b>Literal Name:</b> If the token has a literal name in the vocabulary (e.g., a keyword or operator),
     *     it is formatted as that name, including quotes (e.g., {@code "'='"}).</li>
     *     <li><b>Symbolic Name and Text:</b> If the token has no literal name (e.g., an identifier or number),
     *     it is formatted as {@code "(<SymbolicName> '<text>')"}.</li>
     * </ol>
     * This is the most commonly used formatter for simple, readable output.
     * <p><b>Examples:</b>
     * <pre>{@code
     * Token eof = new CommonToken(Token.EOF);
     * Token plus = new CommonToken(MyLexer.PLUS, "+");
     * Token id = new CommonToken(MyLexer.ID, "myVar");
     *
     * TokenFormatter formatter = TokenFormatter.SIMPLE.withVocabulary(MyLexer.VOCABULARY);
     *
     * formatter.format(eof);   // Returns "EOF"
     * formatter.format(plus);  // Returns "'='"
     * formatter.format(id);    // Returns "(ID 'myVar')"
     * }</pre>
     */
    public static final TokenFormatter SIMPLE = EOF.withAlternative(LITERAL)
            .withAlternative(SYMBOLIC);

    /**
     * The chain of parsers to attempt in order.
     */
    private final List<CompositePrinterParser> printerParsers;

    /**
     * The set of all token fields this formatter can process.
     */
    private final Set<TokenField<?>> fields;

    /**
     * The vocabulary for resolving token type names.
     */
    private final Vocabulary vocabulary;

    /**
     * Constructs a new TokenFormatter.
     *
     * @param printerParser The printer/parser to use for formatting and parsing.
     * @param fields        The fields that are used by this formatter.
     */
    TokenFormatter(CompositePrinterParser printerParser,
                   Set<TokenField<?>> fields,
                   Vocabulary vocabulary) {
        this(List.of(printerParser), fields, vocabulary);
    }

    private TokenFormatter(List<CompositePrinterParser> printerParsers,
                           Set<TokenField<?>> fields,
                           Vocabulary vocabulary) {
        this.printerParsers = printerParsers;
        this.fields = Set.copyOf(fields);
        this.vocabulary = vocabulary;
    }

    /**
     * Creates a {@link TokenFormatter} from a pattern string.
     * <p>
     * This factory method provides a convenient way to define a token format using a
     * single pattern string, similar to date and time formatting patterns. It is a
     * concise alternative to programmatically chaining individual components with a
     * {@link TokenFormatterBuilder}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Creates a formatter that represents a token as "SYMBOLIC_NAME:'text'"
     * TokenFormatter formatter = TokenFormatter.ofPattern("s:'x'");
     *
     * // Example formatting:
     * // Given a token with symbolic name "ID" and text "user",
     * // the output would be: "ID:'user'"
     * }</pre>
     *
     * <h3>Pattern Syntax</h3>
     * The pattern allows you to specify which token fields to include, along with any
     * literal text, in the desired order.
     *
     * <h3>Pattern Letters</h3>
     * The following pattern letters are available:
     * <table border="1" cellpadding="5" summary="Pattern Letters">
     *   <tr><th>Letter(s)</th><th>Component</th><th>Description</th></tr>
     *   <tr>
     *     <td><b>I</b></td>
     *     <td>Token Type (Integer)</td>
     *     <td>Always formats the token's integer type. Parses an integer and sets it as the token type.</td>
     *   </tr>
     *   <tr>
     *     <td><b>s / S</b></td>
     *     <td>Token Type (Symbolic)</td>
     *     <td>
     *         <b>s (Strict):</b> Formats the symbolic name of the token (e.g., "ID"). Fails if no symbolic name is available. Parses a symbolic name and resolves it to a token type.<br>
     *         <b>S (Lenient):</b> Formats the symbolic name if available; otherwise, formats the literal name. Parses either a symbolic or literal name.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>l / L</b></td>
     *     <td>Token Type (Literal)</td>
     *     <td>
     *         <b>l (Strict):</b> Formats the literal name of the token (e.g., "'='" ). Fails if no literal name is available. Parses a literal name and resolves it to a token type.<br>
     *         <b>L (Lenient):</b> Formats the literal name if available; otherwise, formats the symbolic name. Parses either a literal or symbolic name.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>x / X</b></td>
     *     <td>Token Text</td>
     *     <td>
     *         <b>x (Strict):</b> Formats the token's text without any escaping. Parses text until the next component.<br>
     *         <b>X (Lenient):</b> Formats the token's text with escaping for special characters. Parses escaped text.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>n / N</b></td>
     *     <td>Token Index</td>
     *     <td>
     *         <b>n (Strict):</b> Formats the token's index. Fails if the index is the default value (-1). Parses an integer for the token index.<br>
     *         <b>N (Lenient):</b> Always formats the token's index. Parses an integer for the token index.
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
     * @param pattern the pattern string that defines the format.
     * @return a new {@link TokenFormatter} instance based on the provided pattern.
     * @throws IllegalArgumentException if the pattern string is invalid.
     * @see TokenFormatterBuilder#appendPattern(String)
     */
    public static TokenFormatter ofPattern(String pattern) {
        return new TokenFormatterBuilder().appendPattern(pattern).toFormatter();
    }

    /**
     * Formats a token into a string.
     *
     * @param token The token to format.
     * @return The formatted string.
     * @throws TokenException if the token cannot be formatted.
     */
    public String format(Token token) {
        TokenFormatContext ctx = new TokenFormatContext(token, vocabulary);
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
            throw new TokenException("Failed to format token %s".formatted(Tokens.format(token,
                    vocabulary)));
        }
        return buf.toString();
    }

    /**
     * Parses a string into a single token using a strict parsing strategy.
     * <p>
     * This method requires that the <b>entire</b> input string be consumed during the
     * parsing process. It is best used when the input string is expected to be a
     * complete and standalone representation of a single token.
     * <p>
     * For more lenient parsing of a token from the beginning of a string, or for parsing
     * multiple tokens from a single string, use {@link #parse(String, ParsePosition)}.
     *
     * @param input The string to parse. Must not be null.
     * @return The parsed token.
     * @throws TokenParseException if the string cannot be parsed or is not fully consumed.
     * @see #parse(String, ParsePosition)
     */
    public Token parse(String input) {
        Objects.requireNonNull(input);
        ParsePosition position = new ParsePosition(0);
        Token token = parse(input, position);
        if (token == null) {
            throw new TokenParseException(input, position.getErrorIndex());
        }
        if (position.getIndex() != input.length()) {
            String message = "Input '%s' has trailing unparsed text at position %d".formatted(input, position.getIndex());
            throw new TokenParseException(input, position.getIndex(), message);
        }
        return token;
    }

    /**
     * Parses a token from a string in a lenient, non-exception-throwing manner.
     * <p>
     * This method attempts to parse a token starting at the index specified by the
     * {@link ParsePosition}. It does <b>not</b> require the entire string to be consumed.
     * <p>
     * On success, the parsed {@link Token} is returned, and the index of the {@code ParsePosition}
     * is updated to point to the character immediately after the parsed text. The error index
     * is set to -1.
     * <p>
     * On failure, this method returns {@code null} instead of throwing an exception. The
     * index of the {@code ParsePosition} is left unchanged, and the error index is updated
     * to the position where the parse failed.
     * <p>
     * This method is particularly useful for parsing multiple tokens sequentially from a
     * single input string.
     *
     * @param input    The string from which to parse a token. Must not be null.
     * @param position The {@link ParsePosition} object that tracks the current parsing
     *                 position and error location. Must not be null.
     * @return The parsed {@link Token}, or {@code null} if parsing fails.
     * @see #parse(String)
     */
     public Token parse(String input, ParsePosition position) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(input, "input");
        TokenParseContext ctx;
        int initial = position.getIndex();
        int lastError = -1;

        for (var parser : printerParsers) {
            ctx = new TokenParseContext(new HashMap<>(), parser, vocabulary);
            int current = parser.parse(ctx, input, initial);
            if (current > 0) {
                position.setIndex(current);
                position.setErrorIndex(-1); // Clear error index on success
                return ctx.resolveFields();
            } else {
                lastError = current; // Record the failure position
            }
        }

        // All parsers failed, set the error index and return null
        position.setErrorIndex(~lastError);
        return null;
    }



    /**
     * Returns a new {@link TokenFormatter} instance with the specified ANTLR vocabulary.
     * <p>
     * This method allows for configuring the vocabulary after the formatter has been built.
     * Since {@link TokenFormatter} is immutable, this method returns a new instance
     * with the new vocabulary, or the current instance if the vocabulary is unchanged.
     * The vocabulary is required for formatters that use {@link TokenFormatterBuilder#appendSymbolicType()}
     * or {@link TokenFormatterBuilder#appendLiteralType()}.
     *
     * @param vocabulary The ANTLR vocabulary to use for formatting and parsing.
     * @return A new formatter instance with the given vocabulary.
     */
    public TokenFormatter withVocabulary(Vocabulary vocabulary) {
        if (Objects.equals(vocabulary, this.vocabulary)) {
            return this;
        }
        return new TokenFormatter(printerParsers, fields, vocabulary);
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
     * @return A new {@link TokenFormatter} with the combined formatting and parsing logic.
     */
    public TokenFormatter withAlternative(TokenFormatter alternative) {
        Vocabulary vocabulary = this.vocabulary == null ? alternative.vocabulary : this.vocabulary;
        Set<TokenField<?>> fields = new HashSet<>(getFields());
        fields.addAll(alternative.getFields());
        List<CompositePrinterParser> printerParsers = new ArrayList<>(this.printerParsers);
        printerParsers.addAll(alternative.printerParsers);
        return new TokenFormatter(printerParsers, fields, vocabulary);
    }

    /**
     * Gets the set of {@link TokenField}s that this formatter uses.
     *
     * @return An unmodifiable set of token fields.
     */
    public Set<TokenField<?>> getFields() {
        return fields;
    }

}
