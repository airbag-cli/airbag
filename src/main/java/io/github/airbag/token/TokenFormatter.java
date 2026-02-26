package io.github.airbag.token;

import io.github.airbag.token.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenFormatterBuilder.WhitespacePrinterParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.*;

import static java.util.Map.entry;

/**
 * A formatter for parsing and formatting ANTLR {@link Token} objects.
 * <p>
 * This class provides the primary application entry point for formatting and parsing {@link Token} instances.
 * It can transform a {@code Token} into a specific string representation and parse a string back into a {@code Token}.
 *
 * <h3>Overview</h3>
 * <p>
 * The formatting and parsing logic is defined by a composition of smaller components, each responsible for handling a
 * specific {@link TokenField} (e.g., {@link TokenField#TYPE TYPE}, {@link TokenField#TEXT TEXT}) or a literal string.
 * New formatters can be created using one of three main approaches:
 * <ol>
 *     <li><b>Predefined constants:</b> Common formats are available as static constants, such as
 *     {@link #SIMPLE} and {@link #ANTLR}.</li>
 *     <li><b>Pattern strings:</b> A format can be defined using a pattern string with {@link #ofPattern(String)}.
 *     This is a concise way to specify a custom format.</li>
 *     <li><b>Builder:</b> For more complex scenarios, a {@link TokenFormatterBuilder} can be used to construct a
 *     formatter piece by piece, allowing for detailed control over formatting and parsing logic, including
 *     optional sections. Multiple formatter can be chained as alternatives with the {@link #withAlternative(TokenFormatter)}
 *     method.
 *     </li>
 * </ol>
 *
 * <h3>Contextual Information</h3>
 * <p>
 * To correctly map between a symbol's integer getType and its symbolic or literal name (e.g., from getType {@code 5} to name {@code 'ID'} or {@code '='}),
 * the formatter relies on an ANTLR {@link org.antlr.v4.runtime.Vocabulary}. The vocabulary can be provided when creating a formatter
 * or by using the {@link #withVocabulary(Vocabulary)} method.
 *
 * <h3>Formatting</h3>
 * <p>
 * Formatting is performed by the {@link #format(Token)} method.
 * <pre>{@code
 * // Using a predefined formatter
 * Token symbol = ...;
 * Vocabulary vocabulary = ...;
 * String formatted = TokenFormatter.SIMPLE.withVocabulary(vocabulary).format(symbol);
 * }</pre>
 * Even a list of symbols can be formatted with the {@link #formatList(List, String)}
 *
 * <h3>Parsing</h3>
 * <p>
 * Parsing is performed by the {@link #parse(CharSequence)} and {@link #parse(CharSequence, FormatterParsePosition)} methods.
 * <pre>{@code
 * // Parsing a string that represents a single, complete symbol
 * Vocabulary vocabulary = ...;
 * Token symbol = TokenFormatter.ofPattern("s:'x'").withVocabulary(vocabulary).parse("ID:'myVar'");
 *
 * // Parsing a symbol from the beginning of a string
 * FormatterParsePosition getCharPositionInLine = new FormatterParsePosition(0);
 * Token firstSymbol = formatter.parse("(ID 'a') (OP '+')", getCharPositionInLine); // Parses first symbol
 * Token secondSymbol = formatter.parse("(ID 'a') (OP '+')", getCharPositionInLine); // Parses second symbol
 * }</pre>
 * Additionally it is possible to parse a full list of symbols with {@link #parseList(CharSequence)}
 *
 * <h3>Immutability</h3>
 * <p>
 * This class is immutable and thread-safe. Methods that appear to modify the formatter, such as
 * {@link #withVocabulary(Vocabulary)} and {@link #withAlternative(TokenFormatter)}, return a new
 * instance with the specified changes.
 *
 * @see TokenFormatterBuilder
 * @see #ofPattern(String)
 * @see org.antlr.v4.runtime.Vocabulary
 */
public class TokenFormatter {

    /**
     * A formatter that mimics the default ANTLR {@link Object#toString} of the {@link org.antlr.v4.runtime.CommonToken} format.
     * <p>
     * This formatter provides a detailed, parsable representation of a symbol, including all its core attributes.
     * The format is: {@code "[@{getTokenIndex},{getStartIndex}:{getStopIndex}='{getText}',<{getType}>(,getChannel={getChannel}),{getLine}:{pos}]"}
     * <ul>
     *     <li>{@code {getTokenIndex}}: The symbol's getTokenIndex within the stream. See {@link Token#getTokenIndex()}.</li>
     *     <li>{@code {getStartIndex}:{getStopIndex}}: The getStartIndex and getStopIndex character indices in the input stream. See {@link Token#getStartIndex()} and {@link Token#getStopIndex()}.</li>
     *     <li>{@code '{getText}'}: The matched getText of the symbol, with special characters escaped. See {@link Token#getText()}.</li>
     *     <li>{@code {getType}}: The symbol's getType, resolved first as a literal name (e.g., {@code '='}), then as a symbolic name (e.g., {@code ID}).</li>
     *     <li>{@code {getChannel}}: The getChannel number. See {@link Token#getChannel()}. The getChannel section is optional and everything
     *     in parentheses is only present, if a non default value is present.</li>
     *     <li>{@code {getLine}:{pos}}: The getLine number and character getCharPositionInLine within the getLine. See {@link Token#getLine()} and {@link Token#getCharPositionInLine()}.</li>
     * </ul>
     * <p><b>Example:</b>
     * <pre>{@code
     * // Given a symbol representing an identifier "user"
     *  Token symbol = Token.of().getType(MyLexer.ID).getText("user").getTokenIndex(10).getStartIndex(50).end(53).getLine(5).getCharPositionInLine(4).get();
     *
     * // Assuming getTokenIndex=10, getStartIndex=50, getStopIndex=53, getLine=5, pos=4
     *  String formatted = TokenFormatter.ANTLR.withVocabulary(MyLexer.VOCABULARY).format(symbol);
     * // formatted will be:
     * "[@10,50:53='user',<ID>,5:4]"
     * }</pre>
     * This format is particularly useful for debugging and logging, as it captures the full context of a symbol.
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
            .appendLiteral(",getChannel=")
            .appendInteger(TokenField.CHANNEL, true)
            .endOptional()
            .appendLiteral(",")
            .appendInteger(TokenField.LINE)
            .appendLiteral(":")
            .appendInteger(TokenField.POSITION)
            .appendLiteral("]")
            .toFormatter();

    // A formatter for the symbol's literal name (e.g., '=', '*').
    private static final TokenFormatter LITERAL = new TokenFormatterBuilder().appendLiteralType()
            .startOptional()
            .appendLiteral(":")
            .appendInteger(TokenField.CHANNEL, true)
            .endOptional()
            .toFormatter();

    // A formatter for the special end-of-file symbol.
    private static final TokenFormatter EOF = new TokenFormatterBuilder().appendEOF()
            .toFormatter();

    // A formatter for the symbol's symbolic name and getText (e.g., "(ID 'myVar')").
    private static final TokenFormatter SYMBOLIC = new TokenFormatterBuilder().appendLiteral("(")
            .appendWhitespace()
            .appendType(TypeFormat.SYMBOLIC_FIRST)
            .startOptional()
            .appendLiteral(":")
            .appendInteger(TokenField.CHANNEL, true)
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
     *     <li><b>EOF Token:</b> If the symbol is the end-of-file marker, it is formatted as the string {@code "EOF"}.</li>
     *     <li><b>Literal Name:</b> If the symbol has a literal name in the vocabulary (e.g., a keyword or operator),
     *     it is formatted as that name, including quotes (e.g., {@code "'='"}).</li>
     *     <li><b>Symbolic Name and Text:</b> If the symbol has no literal name (e.g., an identifier or number),
     *     it is formatted as {@code "(TYPE '<getText>')"} where TYPE is the actual symbolic name of the symbol.</li>
     * </ol>
     * For literal and symbolic formats, if the symbol is on a non-default getChannel, the getChannel
     * number will be appended (e.g., {@code "'+':3"} or {@code "(ID:3 'myVar')"}).
     * <p>
     * This is the most commonly used formatter for simple, readable output.
     * <p><b>Examples:</b>
     * <pre>{@code
     * Token eof = new CommonSymbol(Token.EOF);
     * Token plus = new CommonSymbol(MyLexer.PLUS, "+");
     * Token id = new CommonSymbol(MyLexer.ID, "myVar");
     * Token hidden = new CommonSymbol(MyLexer.COMMENT, "//comment");
     * hidden.setChannel(Token.HIDDEN_CHANNEL); // Assuming HIDDEN_CHANNEL = 1
     *
     * TokenFormatter formatter = TokenFormatter.SIMPLE.withVocabulary(MyLexer.VOCABULARY);
     *
     * formatter.format(eof);   // Returns "EOF"
     * formatter.format(plus);  // Returns "'+'"
     * formatter.format(id);    // Returns "(ID 'myVar')"
     * formatter.format(hidden); // Returns "(COMMENT:1 '//comment')"
     * }</pre>
     */
    public static final TokenFormatter SIMPLE = EOF.withAlternative(LITERAL)
            .withAlternative(SYMBOLIC);

/**
 * A formatter that formats and parses ANTLR {@link Token} objects into/from a JSON object structure.
 * <p>
 * This formatter outputs a token as a flat JSON object, with each token field mapped to a key-value pair.
 * The keys are quoted strings, and values correspond to the token's properties.
 * {@code "symbolicName"} and {@code "literalName"} are included as separate fields, derived from the token's type
 * using the provided {@link Vocabulary}.
 * <p>
 * The output JSON structure includes:
 * <ul>
 *     <li>{@code "type"}: The token's integer type ({@code "I"}).</li>
 *     <li>{@code "text"}: The token's text ({@code "X"}).</li>
 *     <li>{@code "symbolicName"}: The symbolic name of the token, if available ({@code "s"}). Optional.</li>
 *     <li>{@code "literalName"}: The literal name of the token, if available ({@code "l"}). Optional.</li>
 *     <li>{@code "channel"}: The token's channel ({@code "c"}). Optional.</li>
 *     <li>{@code "index"}: The token's index in the token stream ({@code "n"}). Optional.</li>
 *     <li>{@code "line"}: The line number where the token appears ({@code "r"}). Optional.</li>
 *     <li>{@code "charPositionInLine"}: The character position in the line ({@code "p"}). Optional.</li>
 *     <li>{@code "startIndex"}: The starting character index in the input stream ({@code "b"}). Optional.</li>
 *     <li>{@code "stopIndex"}: The stopping character index in the input stream ({@code "e"}). Optional.</li>
 * </ul>
 * <p>
 * Due to its implementation via {@link #ofPattern(String)}, this formatter's parsing behavior is **order-dependent**.
 * This means that for successful parsing, the fields in the input JSON string must appear in the exact order
 * defined in the formatter's internal pattern.
 * <p>
 * <b>Example Output:</b>
 * <pre>{@code
 * {
 *     "type": 5,
 *     "text": "54",
 *     "symbolicName": "INT",
 *     "channel": 0,
 *     "index": 12,
 *     "line": 3,
 *     "charPositionInLine": 8,
 *     "startIndex": 100,
 *     "stopIndex": 101
 * }
 * }</pre>
 */
    public static final TokenFormatter JSON = new TokenFormatterBuilder().appendPattern("""
            {
                '"type"' : "I",
                '"text"' : "X",
                ['"symbolicName"' : "s",][
                '"literalName"' : "l",][
                '"channel"' : "c",][
                '"index"' : "n",][
                '"line"' : "r",][
                '"charPositionInLine"' : "p",][
                '"startIndex"' : "b",][
                '"stopIndex"' : "e"]
            }""").toFormatter();

/**
 * A formatter that formats and parses ANTLR {@link Token} objects into/from an XML structure.
 * <p>
 * This formatter outputs a token as an XML element with a flat structure, where most token fields
 * are represented as child elements. The {@code symbolicName} and {@code literalName} are
 * included as attributes of the {@code <type>} element.
 * <p>
 * The output XML structure adheres to the "All Elements" best practice, providing a clear and
 * consistent representation:
 * <ul>
 *     <li>Root Element: {@code <token>}.</li>
 *     <li>{@code <type>}: Contains the token's integer type as content, with optional {@code symbolic}
 *         and {@code literal} attributes representing the token's symbolic and literal names ({@code "s"} and {@code "l"}).</li>
 *     <li>{@code <text>}: Contains the token's text ({@code "x"}) as content.</li>
 *     <li>Optional Child Elements:
 *         <ul>
 *             <li>{@code <channel>}: The token's channel ({@code "c"}).</li>
 *             <li>{@code <index>}: The token's index in the token stream ({@code "n"}).</li>
 *             <li>{@code <line>}: The line number where the token appears ({@code "r"}).</li>
 *             <li>{@code <charPositionInLine>}: The character position in the line ({@code "p"}).</li>
 *             <li>{@code <startIndex>}: The starting character index in the input stream ({@code "b"}).</li>
 *             <li>{@code <stopIndex>}: The stopping character index in the input stream ({@code "e"}).</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * <b>Example Output:</b>
 * <pre>{@code
 * <token>
 *     <type symbolic="INT">5</type>
 *     <text>54</text>
 *     <channel>0</channel>
 *     <index>12</index>
 *     <line>3</line>
 *     <charPositionInLine>8</charPositionInLine>
 *     <startIndex>100</startIndex>
 *     <stopIndex>101</stopIndex>
 * </token>
 * }</pre>
 */
    public static final TokenFormatter XML = new TokenFormatterBuilder().appendPattern("""
            '<token>'
                '<type'[ 'symbolic'="s"][ 'literal'="l"]>I'</type>'
                '<text>'x'</text>'[
                '<channel>'c'</channel>'][
                '<index>'n'</index>'][
                '<line>'r'</line>'][
                '<charPositionInLine>'p'</charPositionInLine>'][
                '<startIndex>'b'</startIndex>'][
                '<stopIndex>'e'</stopIndex>']
            '</token>'
            """).toFormatter();

    /**
     * The chain of parsers to attempt in order.
     */
    private final List<CompositePrinterParser> printerParsers;

    /**
     * The set of all symbol fields this formatter can process.
     */
    private final Set<TokenField<?>> fields;

    /**
     * The vocabulary for resolving symbol getType names.
     */
    private final Vocabulary vocabulary;

    // Package private constructor
    TokenFormatter(CompositePrinterParser printerParser,
                   Set<TokenField<?>> fields,
                   Vocabulary vocabulary) {
        this(List.of(printerParser), fields, vocabulary);
    }

    // Private constructor
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
     * This factory method provides a convenient way to define a symbol format using a
     * single pattern string, similar to date and time formatting patterns. It is a
     * concise alternative to programmatically chaining individual components with a
     * {@link TokenFormatterBuilder}.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Creates a formatter that represents a symbol as "SYMBOLIC_NAME:'getText'"
     * TokenFormatter formatter = TokenFormatter.ofPattern("s:\\'x\\'");
     *
     * // Example formatting:
     * // Given a symbol with symbolic name "ID" and getText "user",
     * // the output would be: "ID:'user'"
     * }</pre>
     *
     * <h3>PatternBuilder Syntax</h3>
     * The pattern allows you to specify which symbol fields to include, along with any
     * literal getText, in the desired order.
     *
     * <h3>PatternBuilder Letters</h3>
     * The following pattern letters are available:
     * <table border="1" cellpadding="5" summary="PatternBuilder Letters">
     *   <tr><th>Letter(s)</th><th>Component</th><th>Description</th></tr>
     *   <tr>
     *     <td><b>I</b></td>
     *     <td>Token Type (Integer)</td>
     *     <td>Always formats the symbol's integer getType. Parses an integer and sets it as the symbol getType.</td>
     *   </tr>
     *   <tr>
     *     <td><b>s / S</b></td>
     *     <td>Token Type (Symbolic)</td>
     *     <td>
     *         <b>s (Strict):</b> Formats the symbolic name of the symbol (e.g., "ID"). Fails if no symbolic name is available. Parses a symbolic name and resolves it to a symbol getType.<br>
     *         <b>S (Lenient):</b> Formats the symbolic name if available; otherwise, formats the literal name and lastly the integer getType if both fail. Parses either a symbolic or literal name or the integer getType.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>l / L</b></td>
     *     <td>Token Type (Literal)</td>
     *     <td>
     *         <b>l (Strict):</b> Formats the literal name of the symbol (e.g., "'='" ). Fails if no literal name is available. Parses a literal name and resolves it to a symbol getType.<br>
     *         <b>L (Lenient):</b> Formats the literal name if available; otherwise, formats the symbolic name and lastly the integer getType. Parses either a literal or symbolic name or the integer getType.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>x / X</b></td>
     *     <td>Token Text</td>
     *     <td>
     *         <b>x (Strict):</b> Formats the symbol's getText without any escaping. Parses getText until the next component.<br>
     *         <b>X (Lenient):</b> Formats the symbol's getText with escaping for special characters. Parses escaped getText.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>n / N</b></td>
     *     <td>Token Index</td>
     *     <td>
     *         <b>n (Strict):</b> Formats the symbol's getTokenIndex. Fails if the getTokenIndex is the default value (-1). Parses an integer for the symbol getTokenIndex.<br>
     *         <b>N (Lenient):</b> Always formats the symbol's getTokenIndex. Parses an integer for the symbol getTokenIndex.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>b / B</b></td>
     *     <td>Start Index</td>
     *     <td>
     *         <b>b (Strict):</b> Formats the getStartIndex getTokenIndex. Fails if the getTokenIndex is the default value (-1). Parses an integer for the getStartIndex getTokenIndex.<br>
     *         <b>B (Lenient):</b> Always formats the getStartIndex getTokenIndex. Parses an integer for the getStartIndex getTokenIndex.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>e / E</b></td>
     *     <td>Stop Index</td>
     *     <td>
     *         <b>e (Strict):</b> Formats the getStopIndex getTokenIndex. Fails if the getTokenIndex is the default value (-1). Parses an integer for the getStopIndex getTokenIndex.<br>
     *         <b>E (Lenient):</b> Always formats the getStopIndex getTokenIndex. Parses an integer for the getStopIndex getTokenIndex.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>c / C</b></td>
     *     <td>Channel</td>
     *     <td>
     *         <b>c (Strict):</b> Formats the getChannel number. Fails if the getChannel is the default getChannel (0). Parses a non-zero integer for the getChannel.<br>
     *         <b>C (Lenient):</b> Always formats the getChannel number, including the default getChannel. Parses any integer for the getChannel.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>p / P</b></td>
     *     <td>Char Position in Line</td>
     *     <td>
     *         <b>p (Strict):</b> Formats the character getCharPositionInLine in getLine. Fails if the getCharPositionInLine is the default value (-1). Parses an integer for the getCharPositionInLine.<br>
     *         <b>P (Lenient):</b> Always formats the character getCharPositionInLine in getLine. Parses an integer for the getCharPositionInLine.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td><b>r / R</b></td>
     *     <td>Line Number</td>
     *     <td>
     *         <b>r (Strict):</b> Formats the getLine number. Fails if the getLine number is the default value (-1). Parses an integer for the getLine number.<br>
     *         <b>R (Lenient):</b> Always formats the getLine number. Parses an integer for the getLine number.
     *     </td>
     *   </tr>
     * </table>
     *
     * <h3>Literals, Escaping, and Quoting</h3>
     * You can include literal getText in your pattern in three ways:
     * <ul>
     *   <li><b>Unquoted Text:</b> Any character that is not a recognized pattern letter (a-z, A-Z)
     *       or a special character ({@code []'\}) is treated as a literal. For example, in the pattern
     *       {@code s:x}, the colon is a literal.</li>
     *   <li><b>Escaping:</b> The backslash character ({@code \}) escapes the following character, forcing
     *       it to be treated as a literal. This is useful for treating a single pattern letter or special
     *       character as a literal. For example, {@code \s} will produce a literal 's', {@code \\} a literal
     *       '\' and {@code \'} a literal single quote.</li>
     *   <li><b>Quoting:</b> You can quote a sequence of characters with the percent sign ({@code '}).
     *       Everything between a pair of {@code '} characters is treated as a single literal block.
     *       This is useful for including sequences that contain pattern letters or special characters.
     *       For example, {@code 's'} produces the literal "s", and {@code 'section[]'} produces "section[]".
     *       To include a literal single quote, escape it with a backslash: {@code \'}.</li>
     * </ul>
     *
     * <h3>Optional Sections</h3>
     * Square brackets {@code []} can be used to create an optional section in the pattern.
     * During formatting, if all components within the optional section can be printed, they are.
     * Otherwise, the entire section is skipped. During parsing, the parser will attempt to
     * match the components in the optional section, but if it fails, it will skip the section
     * and continue with the rest of the pattern. A component can "fail" if either the getText option
     * set the option {@link TextOption#failOnDefault(boolean)} or if the component is strict with a default value.
     *
     * <h3>Alternatives</h3>
     * Alternatives patterns are separated by {@code |}. This character is non-escapable with this method
     * therefore it is advisable to directly use {@link TokenFormatterBuilder#appendPattern(String)}
     * if the desired pattern includes this character.
     *
     * @param pattern the pattern string that defines the format.
     * @return a new {@link TokenFormatter} instance based on the provided pattern.
     * @throws IllegalArgumentException if the pattern string is invalid.
     * @see TokenFormatterBuilder#appendPattern(String)
     */
    public static TokenFormatter ofPattern(String pattern) {
        String[] patterns = pattern.split("\\|");
        TokenFormatter formatter = null;
        try {
            for (String singlePattern : patterns) {
                if (formatter == null) {
                    formatter = new TokenFormatterBuilder().appendPattern(singlePattern)
                            .toFormatter();
                } else {
                    formatter = formatter.withAlternative(new TokenFormatterBuilder().appendPattern(
                            singlePattern).toFormatter());
                }
            }
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("The pattern %s is invalid".formatted(pattern), e);
        }

        return formatter;
    }

    /**
     * Creates a formatter that prints a symbol's fields, one per getLine.
     * <p>
     * This is useful for creating a detailed, human-readable representation of a symbol's data.
     * Each specified field is printed on a new getLine with its name and formatted value.
     * <p><b>Example Output:</b>
     * <pre>{@code
     * getType: ID
     * getText: 'myVariable'
     * getLine: 10
     * getCharPositionInLine: 5
     * }</pre>
     *
     * @param fields A collection of {@link TokenField}s to include in the formatter.
     * @return A new {@link TokenFormatter} that formats the specified fields.
     */
    public static TokenFormatter fromFields(Collection<TokenField<?>> fields) {
        TokenFormatterBuilder builder = new TokenFormatterBuilder();
        for (var field : fields) {
            if (field == TokenField.TEXT) {
                builder.appendLiteral("getText: ")
                        .appendText(TextOption.ESCAPED)
                        .appendLiteral("%n".formatted());
            } else if (field == TokenField.TYPE) {
                builder.appendLiteral("getType: ")
                        .appendType(TypeFormat.SYMBOLIC_FIRST)
                        .appendLiteral("%n".formatted());
            } else {
                //noinspection unchecked
                builder.appendLiteral("%s: ".formatted(field.name()))
                        .appendInteger((TokenField<Integer>) field)
                        .appendLiteral("%n".formatted());
            }
        }
        return builder.toFormatter();
    }

    /**
     * Formats a symbol into a string.
     *
     * @param symbol The symbol to format.
     * @return The formatted string.
     * @throws TokenFormatterException if the symbol cannot be formatted.
     */
    public String format(Token symbol) {
        TokenFormatContext ctx = new TokenFormatContext(symbol, vocabulary);
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
            throw new TokenFormatterException("Failed to format symbol %s".formatted(symbol));
        }
        return buf.toString();
    }

    /**
     * Formats a list of symbols into a single string, separated by a delimiter.
     *
     * @param symbols   The list of symbols to format.
     * @param delimiter The delimiter to place between formatted symbols.
     * @return A single string containing all formatted symbols.
     */
    public String formatList(List<? extends Token> symbols, String delimiter) {
        var joiner = new StringJoiner(delimiter);
        symbols.forEach(s -> joiner.add(format(s)));
        return joiner.toString();
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
     * @throws TokenParseException if the string cannot be parsed or is not fully consumed.
     * @see #parse(CharSequence, FormatterParsePosition)
     */
    public Token parse(CharSequence input) {
        Objects.requireNonNull(input);
        FormatterParsePosition position = new FormatterParsePosition(0);
        Token token = parse(input, position);
        if (token == null) {
            throw new TokenParseException(input.toString(),
                    position.getErrorIndex(),
                    position.getMessage());
        }
        if (position.getIndex() != input.length()) {
            int index = position.getIndex();
            String message = "Input '%s>>%s' has trailing unparsed getText at getCharPositionInLine %d".formatted(
                    input.subSequence(0, index),
                    input.subSequence(index, input.length()),
                    index);
            throw new TokenParseException(input.toString(), position.getIndex(), message);
        }
        return token;
    }

    /**
     * Parses a symbol from a string in a lenient, non-exception-throwing manner.
     * <p>
     * This method attempts to parse a symbol starting at the getTokenIndex specified by the
     * {@link FormatterParsePosition}. It does <b>not</b> require the entire string to be consumed.
     * <p>
     * On success, the parsed {@link Token} is returned, and the getTokenIndex of the {@code FormatterParsePosition}
     * is updated to point to the character immediately after the parsed getText. The error getTokenIndex
     * is set to -1.
     * <p>
     * On failure, this method returns {@code null} instead of throwing an exception. The
     * getTokenIndex of the {@code FormatterParsePosition} is left unchanged, and the error getTokenIndex is updated
     * to the getCharPositionInLine where the parse failed. The method {@link FormatterParsePosition#getMessage()} gives an
     * indication about why the parse might have failed.
     * <p>
     * This method is particularly useful for parsing multiple symbols sequentially from a
     * single input string.
     *
     * @param input    The char sequence from which to parse a symbol. Must not be null.
     * @param position The {@link FormatterParsePosition} object that tracks the current parsing
     *                 getCharPositionInLine and error location. Must not be null.
     * @return The parsed {@link Token}, or {@code null} if parsing fails.
     * @see #parse(CharSequence)
     */
    public Token parse(CharSequence input, FormatterParsePosition position) {
        Objects.requireNonNull(position, "getCharPositionInLine");
        Objects.requireNonNull(input, "input");
        int initial = position.getIndex();
        int maxError = position.getErrorIndex();

        for (var parser : printerParsers) {
            TokenParseContext ctx = new TokenParseContext(parser, vocabulary);
            int current = parser.parse(ctx, input, initial);
            if (current >= 0) {
                position.setIndex(current);
                position.setErrorIndex(-1); // Clear error getTokenIndex on success
                if (position.isSymbolIndex()) {
                    ctx.addField(TokenField.INDEX, position.getSymbolIndex());
                }
                return ctx.resolveFields();
            } else {
                int errorPosition = ~current;
                if (errorPosition > maxError) {
                    position.setMessage(ctx.getErrorMessage());
                    position.setErrorIndex(errorPosition);
                } else if (errorPosition == maxError) {
                    position.appendMessage(ctx.getErrorMessage());
                }
                maxError = Math.max(maxError, errorPosition);
            }
        }

        // All parsers failed return null
        return null;
    }

    /**
     * Parses a sequence of symbols from a string.
     * <p>
     * This method reads symbols sequentially until the end of the input is reached.
     * By default, it skips any whitespace between symbols.
     *
     * @param input The character sequence to parse.
     * @return An unmodifiable list of parsed symbols.
     * @throws TokenParseException if any part of the input (other than trailing whitespace) cannot be parsed.
     */
    public List<Token> parseList(CharSequence input) {
        return parseList(input, true);
    }

    /**
     * Parses a sequence of symbols from a string, with optional whitespace handling.
     * <p>
     * This method reads symbols sequentially until the end of the input is reached.
     *
     * @param input            The character sequence to parse.
     * @param ignoreWhitespace If true, any whitespace between symbols is ignored. If false,
     *                         the parser expects symbols to be contiguous, and any intervening
     *                         characters will cause a {@link TokenParseException}.
     * @return An unmodifiable list of parsed symbols.
     * @throws TokenParseException if any part of the input cannot be parsed.
     */
    public List<Token> parseList(CharSequence input, boolean ignoreWhitespace) {
        WhitespacePrinterParser whitespaceConsumer = new WhitespacePrinterParser("");
        FormatterParsePosition position = new FormatterParsePosition(0);
        if (!fields.contains(TokenField.INDEX)) {
            position.setSymbolIndex(0);
        }
        List<Token> list = new ArrayList<>();
        while (input.length() > position.getIndex() && position.getErrorIndex() < 0) {
            Token symbol = parse(input, position);
            if (position.getErrorIndex() >= 0) {
                if (ignoreWhitespace) {
                    int index = whitespaceConsumer.parse(null, input, position.getIndex());
                    if (index > position.getIndex()) {
                        position.setIndex(index);
                        position.setErrorIndex(-1);
                    } else {
                        throw new TokenParseException(input.toString(),
                                position.getErrorIndex(),
                                position.getMessage());
                    }
                } else {
                    throw new TokenParseException(input.toString(),
                            position.getErrorIndex(),
                            position.getMessage());
                }
            } else {
                list.add(symbol);
                if (position.isSymbolIndex()) {
                    position.setSymbolIndex(position.getSymbolIndex() + 1);
                }
            }
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns a copy of this formatter with a new ANTLR vocabulary.
     * <p>
     * The vocabulary is essential for resolving token getType integers to their literal
     * and symbolic names (e.g., mapping getType {@code 4} to symbolic name {@code 'ID'}).
     *
     * @param vocabulary The ANTLR vocabulary to use for name resolution.
     * @return A new {@link TokenFormatter} instance with the specified vocabulary.
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
     * @return An unmodifiable set of symbol fields.
     */
    public Set<TokenField<?>> getFields() {
        return fields;
    }

    /**
     * Returns a string representation of this formatter's pattern.
     * <p>
     * This is useful for debugging and understanding the structure of the formatter.
     * If the formatter was built with alternatives, each alternative pattern is
     * separated by {@code  | }.
     *
     * @return A string representing the formatter's pattern.
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