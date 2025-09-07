package io.github.airbag.token.format;

import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.*;

/**
 * Builder for creating {@link TokenFormatter} instances.
 * <p>
 * This builder provides a flexible and powerful way to define custom formats for
 * converting {@link org.antlr.v4.runtime.Token} objects to and from strings.
 * It is the primary mechanism for constructing {@link TokenFormatter}s, which are
 * immutable and thread-safe once created.
 *
 * <h3>Overview</h3>
 * The builder uses a fluent API to assemble a sequence of "printer-parsers".
 * Each printer-parser is a component responsible for a specific part of the
 * format. For example, one component might handle the token's text, while another
 * handles its symbolic type name.
 * <p>
 * A {@link TokenFormatter} has two main functions:
 * <ul>
 *   <li><b>Formatting (Printing):</b> Converting a {@code Token} object into a string.</li>
 *   <li><b>Parsing:</b> Converting a string back into a {@code Token} object's constituent parts.</li>
 * </ul>
 * The sequence of appended components defines the exact format for both operations.
 *
 * <h3>Usage</h3>
 * To create a formatter, you instantiate a {@code TokenFormatterBuilder} and call
 * various {@code append...} methods to define the desired format. Once the
 * format is defined, you call {@link #toFormatter()} to create the
 * {@link TokenFormatter} instance.
 *
 * <p><b>Example: Simple Formatter</b></p>
 * <pre>{@code
 * // Creates a formatter that represents a token as "SYMBOLIC_NAME:'text'"
 * TokenFormatter formatter = new TokenFormatterBuilder()
 *     .appendSymbolicType()
 *     .appendLiteral(":'")
 *     .appendText()
 *     .appendLiteral("'")
 *     .toFormatter();
 *
 * // Formatting example:
 * // Assuming a token with symbolic name "ID" and text "user",
 * // the output of formatter.format(token, vocabulary) would be:
 * // "ID:'user'"
 *
 * // The same formatter can also parse this string back into its components.
 * }</pre>
 *
 * <h3>Pattern-Based Formatting</h3>
 * For more complex or dynamic formatting needs, the {@link #appendPattern(String)}
 * method provides a concise and powerful alternative. It allows you to define the
 * entire format using a single pattern string, similar to date and time formatting
 * patterns. This is often more convenient than chaining multiple {@code append...} calls.
 *
 * <p><b>Example: Pattern-Based Formatter</b></p>
 * <pre>{@code
 * // A pattern to replicate ANTLR's default Token.toString() format
 * String pattern = "\\[@N,B:E='X',<L>,R:P\\]";
 * TokenFormatter antlrStyleFormatter = new TokenFormatterBuilder()
 *     .appendPattern(pattern)
 *     .toFormatter();
 *
 * // Example output for a token:
 * // "[@-1,0:3='text',<1>,1:0]"
 * }</pre>
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used to create a formatter and then discarded. The resulting {@link TokenFormatter}
 * objects, however, are immutable and safe for use in multithreaded environments.
 *
 * @see TokenFormatter
 * @see TypeFormat
 * @see TextOption
 */
public class TokenFormatterBuilder {

    /**
     * The list of printer/parsers that make up the format of the token.
     */
    private final List<TokenPrinterParser> printerParsers = new ArrayList<>();

    /**
     * The list of token fields that are used by the printer/parsers.
     */
    private final Set<TokenField<?>> fields = new HashSet<>();

    private int optionalStart = -1;

    /**
     * Appends a printer/parser for an integer field to the formatter.
     *
     * @param field The integer field to append.
     * @return This builder.
     */
    public TokenFormatterBuilder appendInteger(TokenField<Integer> field) {
        return appendInteger(field, false);
    }

    /**
     * Appends a printer/parser for an integer field to the formatter, with optional strict formatting.
     * <p>
     * When strict formatting is enabled, the integer value will only be printed if it is not
     * equal to the default value of the provided {@link TokenField}. This is useful for omitting
     * optional fields that have not been explicitly set.
     * <p>
     * For example, if used with {@link TokenField#CHANNEL} and strict mode, the channel will not be
     * printed if it is the default channel (0).
     *
     * @param field    The integer field to append.
     * @param isStrict {@code true} to enable strict formatting, {@code false} otherwise.
     * @return This builder.
     */
    public TokenFormatterBuilder appendInteger(TokenField<Integer> field, boolean isStrict) {
        printerParsers.add(new IntegerPrinterParser(field, isStrict));
        fields.add(field);
        return this;
    }

    /**
     * Appends a printer/parser for a literal string to the formatter.
     *
     * @param literal The literal string to append.
     * @return This builder.
     */
    public TokenFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a printer/parser for the token's text.
     * <p>
     * This component is non-greedy; it consumes characters only up to the point
     * where the next component in the formatter is able to match. If this is the
     * last component in the sequence, it will consume the remainder of the input string.
     * <p>
     * It is useful for capturing variable text that is situated between more
     * well-defined components (like literals or symbolic types).
     * <p>
     * This method uses the {@link TextOption#NOTHING} option, which means no
     * escaping is performed.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendText() {
        printerParsers.add(new TextPrinterParser());
        fields.add(TokenField.TEXT);
        return this;
    }

    /**
     * Appends a printer/parser for the token's text, with custom escaping and default value behavior.
     * <p>
     * This component is non-greedy; it consumes characters only up to the point
     * where the next component in the formatter is able to match. If this is the
     * last component in the sequence, it will consume the remainder of the input string.
     *
     * @param option The {@link TextOption} to use for formatting and parsing the text.
     * @return This builder.
     * @see TextOption
     */
    public TokenFormatterBuilder appendText(TextOption option) {
        printerParsers.add(new TextPrinterParser(option));
        fields.add(TokenField.TEXT);
        return this;
    }

    /**
     * Appends a printer/parser for the token's symbolic type name (e.g., "ID", "INT").
     * <p>
     * This component provides a strict mapping between a token's type and its symbolic name
     * as defined in the ANTLR {@link Vocabulary}.
     * <p>
     * <b>Formatting:</b> It will throw a {@link TokenException} if the vocabulary is missing or
     * if the token's type does not have a symbolic name. This is often the case for tokens
     * representing literals (e.g., keywords, operators like {@code '='}), which have a literal
     * name but not a symbolic one.
     * <p>
     * <b>Parsing:</b> It reads a symbolic name from the input and resolves it back to the
     * corresponding token type.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendSymbolicType() {
        printerParsers.add(new SymbolicTypePrinterParser());
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the token's literal type name (e.g., {@code '='}, {@code '*'}).
     * <p>
     * This component maps a token's type to its literal name as defined in the ANTLR
     * {@link Vocabulary}. Literal names are the exact strings defined in the grammar,
     * typically enclosed in single quotes (e.g., {@code '='}).
     * <p>
     * <b>Formatting:</b> The literal name from the vocabulary (including the single quotes)
     * is appended to the output. It will throw a {@link TokenException} if the vocabulary
     * is missing or if the token's type does not have a literal name. This is often the
     * case for tokens with symbolic names like {@code ID} or {@code INT}.
     * <p>
     * <b>Parsing:</b> The parser expects the input to contain the literal name, including
     * the single quotes. It finds the longest possible literal in the vocabulary that
     * matches the input string and resolves it to the corresponding token type.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendLiteralType() {
        printerParsers.add(new LiteralTypePrinterParser());
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the token's type, with a configurable format.
     * <p>
     * This method provides a flexible way to format and parse a token's type by specifying
     * a {@link TypeFormat}. The format determines which representations of the type are
     * used and in what order. For example, a type can be represented by its symbolic name
     * (e.g., "ID"), its literal name (e.g., "'='"), or its raw integer value.
     * <p>
     * <b>Formatting:</b> The formatter will attempt to represent the token's type using the
     * strategies defined by the {@link TypeFormat}, in order. The first successful
     * representation will be appended to the output. If no representation is successful
     * (e.g., a symbolic name is requested but not available), the formatting for this
     * component fails.
     * <p>
     * <b>Parsing:</b> The parser will attempt to match the input text against the possible
     * representations defined by the {@link TypeFormat}, in order. The first representation
     * that successfully matches and parses the input will be used.
     * <p>
     * This is a more general version of {@link #appendSymbolicType()} and
     * {@link #appendLiteralType()}, which correspond to {@link TypeFormat#SYMBOLIC_ONLY}
     * and {@link TypeFormat#LITERAL_ONLY} respectively.
     *
     * @param format The format to use for the token's type.
     * @return This builder.
     * @see TypeFormat
     */
    public TokenFormatterBuilder appendType(TypeFormat format) {
        printerParsers.add(new TypePrinterParser(format));
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the end-of-file (EOF) token.
     * <p>
     * <b>Formatting:</b> If the token's type is {@link Token#EOF}, this component
     * appends the string "EOF". For any other token type, it fails.
     * <p>
     * <b>Parsing:</b> It matches the literal string "EOF" and resolves it to a
     * token of type {@link Token#EOF}.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendEOF() {
        printerParsers.add(new EOFPrinterParser());
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer and parser to the formatter using a flexible pattern string.
     * <p>
     * This method allows for the creation of a formatter by defining a pattern string,
     * which specifies the desired arrangement of token components. The pattern supports
     * various letters, each representing a specific field of a {@link Token}.
     * The case of the letter often determines its behavior during parsing, with lowercase
     * letters typically representing "strict" parsing and uppercase letters representing
     * "lenient" parsing.
     *
     * <h3>Pattern Letters</h3>
     * The following pattern letters are available:
     * <table border="1" cell-padding="5" summary="Pattern Letters">
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
     * <h3>Examples</h3>
     * <pre>{@code
     *   // Format a token as "SYMBOLIC_NAME:'text'"
     *   // For a token with symbolic name "ID" and text "user", the output would be "ID:'user'"
     *   String pattern1 = "s:'x'";
     *
     *   // Replicate ANTLR's default Token.toString() format
     *   // Example output: [@-1,0:3='text',<0>,1:0]
     *   // Using quoted blocks for all literal parts to avoid ambiguity.
     *   String antlrPattern = "\\[@N,B:E='X',<L>,[%channel%=c],R:P\\]";
     *
     *   // Format a token with an optional channel display
     *   // If the channel is not the default, it will be included (e.g., "ID[channel=1]")
     *   // Otherwise, it will be omitted (e.g., "ID")
     *   String optionalChannelPattern = "s[\\[%channel%=c\\]]";
     * }</pre>
     *
     * @param pattern the pattern string to define the formatter.
     * @return this builder.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public TokenFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        parsePattern(pattern);
        return this;
    }

    private static final Set<Character> PATTERN_LETTERS = Set.of(
            'I',
            'S',
            's',
            'L',
            'l',
            'x',
            'X',
            'N',
            'n',
            'B',
            'b',
            'E',
            'e',
            'C',
            'c',
            'P',
            'p',
            'R',
            'r'
    );

    private void parsePattern(String pattern) {
        StringBuilder literalBuf = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (PATTERN_LETTERS.contains(c)) {
                flushLiteralBuf(literalBuf);
                switch (c) {
                    case 'I' -> appendInteger(TokenField.TYPE);
                    case 'S' -> appendType(TypeFormat.SYMBOLIC_FIRST);
                    case 's' -> appendSymbolicType();
                    case 'L' -> appendType(TypeFormat.LITERAL_FIRST);
                    case 'l' -> appendLiteralType();
                    case 'x' -> appendText();
                    case 'X' -> appendText(TextOption.ESCAPED);
                    case 'N' -> appendInteger(TokenField.INDEX);
                    case 'n' -> appendInteger(TokenField.INDEX, true);
                    case 'B' -> appendInteger(TokenField.START);
                    case 'b' -> appendInteger(TokenField.START, true);
                    case 'E' -> appendInteger(TokenField.STOP);
                    case 'e' -> appendInteger(TokenField.STOP, true);
                    case 'C' -> appendInteger(TokenField.CHANNEL);
                    case 'c' -> appendInteger(TokenField.CHANNEL, true);
                    case 'P' -> appendInteger(TokenField.POSITION);
                    case 'p' -> appendInteger(TokenField.POSITION, true);
                    case 'R' -> appendInteger(TokenField.LINE);
                    case 'r' -> appendInteger(TokenField.LINE, true);
                }
            } else {
                switch (c) {
                    case '%' -> {
                        flushLiteralBuf(literalBuf);
                        i++; // Skip opening '%'
                        int contentStart = i;
                        while (i < pattern.length() && pattern.charAt(i) != '%') {
                            i++;
                        }
                        if (i >= pattern.length()) {
                            throw new TokenException("Unclosed quoted literal in pattern: " +
                                                     pattern);
                        }
                        String literal = pattern.substring(contentStart, i);
                        if (!literal.isEmpty()) {
                            appendLiteral(literal);
                        }
                    }
                    case '\\' -> {
                        i++;
                        if (i >= pattern.length()) {
                            throw new TokenException("Invalid escape sequence at end of pattern: " +
                                                     pattern);
                        }
                        literalBuf.append(pattern.charAt(i));
                    }
                    case '[' -> startOptional();
                    case ']' -> endOptional();
                    default -> literalBuf.append(c);
                }
            }
        }
        flushLiteralBuf(literalBuf);
    }

    private void flushLiteralBuf(StringBuilder buf) {
        if (!buf.isEmpty()) {
            appendLiteral(buf.toString());
            buf.setLength(0);
        }
    }

    /**
     * Marks the beginning of an optional section in the formatter.
     * <p>
     * All formatter components appended after this call and before a corresponding
     * call to {@link #endOptional()} will be part of an optional group.
     * <p>
     * During formatting, if any component within this optional group fails to print
     * (e.g., a strict field with a default value), the entire group will be skipped
     * without generating any output.
     * <p>
     * During parsing, the entire group will be attempted. If parsing fails at any
     * point within the group, the parser will backtrack to the state before the
     * optional section and continue, as if the optional section was not present.
     * <p>
     * Note: Optional sections cannot be nested.
     *
     * @return this builder.
     * @throws IllegalStateException if an optional section is already open.
     */
    public TokenFormatterBuilder startOptional() {
        if (optionalStart != -1) {
            throw new IllegalStateException("Optionals cannot be nested");
        }
        optionalStart = printerParsers.size();
        return this;
    }

    /**
     * Marks the end of an optional section.
     * <p>
     * This method must be called after a corresponding {@link #startOptional()}. It
     * takes all the printer-parsers that were added since {@code startOptional()}
     * was called and combines them into a single, optional unit.
     *
     * @return this builder.
     * @throws IllegalStateException if there is no open optional section to end.
     */
    public TokenFormatterBuilder endOptional() {
        if (optionalStart == -1) {
            throw new IllegalStateException("Cannot end optional without starting one");
        }
        if (optionalStart == printerParsers.size()) {
            optionalStart = -1;
            return this;
        }
        List<TokenPrinterParser> optionalList = printerParsers.subList(optionalStart,
                printerParsers.size());
        CompositePrinterParser optional = new CompositePrinterParser(new ArrayList<>(optionalList),
                true);
        optionalList.clear();
        printerParsers.add(optional);
        optionalStart = -1;
        return this;
    }

    /**
     * Builds the token formatter.
     *
     * @return The built token formatter.
     */
    public TokenFormatter toFormatter() {
        return new TokenFormatter(new CompositePrinterParser(printerParsers, false), fields, null);
    }

    /**
     * The internal interface for parsing and formatting.
     * This interface is the building block for the composite {@link TokenFormatter}.
     * It defines the dual functionality of formatting (printing) a token stream
     * and parsing an input character sequence.
     */
    interface TokenPrinterParser {

        /**
         * Formats a value from a context into a string buffer.
         *
         * @param context the context holding the values to be formatted.
         * @param buf     the buffer to append the formatted text to.
         * @return true if the formatting was successful, false otherwise.
         */
        boolean format(TokenFormatContext context, StringBuilder buf);

        /**
         * Parses a text string, consuming characters and updating the context.
         *
         * @param context  the context to store the parsed values.
         * @param text     the text to parse.
         * @param position the position to start parsing from.
         * @return the new position after a successful parse, or a negative value if parsing fails.
         * @throws NullPointerException      if the context or text is null
         * @throws IndexOutOfBoundsException if the position is invalid
         */
        int parse(TokenParseContext context, CharSequence text, int position);

        /**
         * Peeks ahead in the text to see if the next characters match this parser's rule,
         * but does not consume them. Implementations of this method must not alter the parse context.
         *
         * @param context  the parse context.
         * @param text     the text to peek into.
         * @param position the position to start peeking from.
         * @return the position of the potential match if successful, or a negative value if it does not match.
         */
        int peek(TokenParseContext context, CharSequence text, int position);

        default boolean isOptional() {
            return false;
        }

    }

    static final class CompositePrinterParser implements TokenPrinterParser {

        private final TokenPrinterParser[] printerParsers;

        private final boolean isOptional;

        private CompositePrinterParser(List<TokenPrinterParser> printerParsers,
                                       boolean isOptional) {
            this(printerParsers.toArray(new TokenPrinterParser[0]), isOptional);
        }

        private CompositePrinterParser(TokenPrinterParser[] printerParsers, boolean isOptional) {
            this.printerParsers = printerParsers;
            this.isOptional = isOptional;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            int initialLength = buf.length();
            for (TokenPrinterParser printer : printerParsers) {
                if (!printer.format(context, buf)) {
                    buf.setLength(initialLength);
                    return isOptional;
                }
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            if (isOptional) {
                int peekPosition = peek(context, text, position);
                if (peekPosition < 0) {
                    return position;
                }
            }
            for (TokenPrinterParser parser : printerParsers) {
                position = parser.parse(context, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                position = parser.peek(context, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }

        @Override
        public boolean isOptional() {
            return isOptional;
        }
    }

    static class IntegerPrinterParser implements TokenPrinterParser {

        private final TokenField<Integer> integerTokenField;
        private final boolean isStrict;

        IntegerPrinterParser(TokenField<Integer> integerTokenField, boolean isStrict) {
            this.isStrict = isStrict;
            this.integerTokenField = integerTokenField;
        }

        IntegerPrinterParser(TokenField<Integer> integerTokenField) {
            this(integerTokenField, false);
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            if (isStrict) {
                int value = integerTokenField.access(context.token());
                if (value == integerTokenField.getDefault()) {
                    return false;
                }
                buf.append(value);
            } else {
                buf.append(integerTokenField.access(context.token()));
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int numberEnd = peek(context, text, position);
            if (numberEnd < 0) {
                return numberEnd;
            }
            context.addField(integerTokenField,
                    Integer.valueOf(text.subSequence(position, numberEnd).toString()));
            return numberEnd;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            return text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
        }

    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
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

    static class LiteralPrinterParser implements TokenPrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            return peek(context, text, position);
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }

    }

    static class TextPrinterParser implements TokenPrinterParser {

        private final TextOption option;

        TextPrinterParser() {
            this(TextOption.NOTHING);
        }

        TextPrinterParser(TextOption option) {
            this.option = option;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            String text = context.token().getText();
            var escapeMap = option.getEscapeMap();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escapeMap.containsKey(c)) {
                    buf.append(option.getEscapeChar()).append(escapeMap.get(c));
                } else {
                    buf.append(c);
                }
            }
            if (text.isEmpty()) {
                buf.append(option.getDefaultValue());
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            StringBuilder buf = unescapeText(text, position, endPosition);
            String tokenText = buf.toString();
            if (tokenText.equals(option.getDefaultValue())) {
                tokenText = "";
            }
            context.addField(TokenField.TEXT, tokenText);
            return endPosition;
        }

        private StringBuilder unescapeText(CharSequence text, int position, int endPosition) {
            StringBuilder buf = new StringBuilder();
            //Since we peeked before we know that reescaping will work
            char escape = option.getEscapeChar();
            var unescapeMap = option.getUnescapeMap();
            for (int i = position; i < endPosition; i++) {
                char c = text.charAt(i);
                if (c == escape) {
                    buf.append(unescapeMap.get(text.charAt(i + 1)));
                    i++;
                } else {
                    buf.append(c);
                }
            }
            return buf;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            TokenPrinterParser[] parserChain = context.printerParser().printerParsers;
            TokenPrinterParser[] successors = getSuccessors(parserChain);

            if (successors.length == 0) {
                return text.length();
            }

            var unescapeMap = option.getUnescapeMap();
            var escapeChar = option.getEscapeChar();
            while (position < text.length()) {
                boolean match = false;
                for (var successor : successors) {
                    if (successor.peek(context, text, position) >= 0) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    break;
                }

                if (text.charAt(position) == escapeChar) {
                    if (text.length() == position + 1 ||
                        !unescapeMap.containsKey(text.charAt(position + 1))) {
                        return ~position; //Invalid escape
                    } else {
                        position++; //Escape characters cannot be delimiters.
                    }
                }
                position++;
            }
            return position;
        }

        private int findParserIndex(TokenPrinterParser[] parserChain) {
            for (int i = 0; i < parserChain.length; i++) {
                if (parserChain[i] == this) {
                    return i;
                }
            }
            throw new RuntimeException("Parser is not part of the chain");
        }

        private TokenPrinterParser[] getSuccessors(TokenPrinterParser[] parserChain) {
            int parserIndex = findParserIndex(parserChain);
            if (parserIndex < 0 || parserIndex == parserChain.length - 1) {
                return new TokenPrinterParser[0];
            }

            List<TokenPrinterParser> delimiters = new ArrayList<>();
            for (int i = parserIndex + 1; i < parserChain.length; i++) {
                TokenPrinterParser successor = parserChain[i];
                delimiters.add(successor);
                if (!successor.isOptional()) {
                    break;
                }
            }
            return delimiters.toArray(new TokenPrinterParser[0]);
        }
    }

    static class SymbolicTypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String symbolicName = vocabulary.getSymbolicName(context.token().getType());
            if (symbolicName == null) {
                return false;
            }
            buf.append(symbolicName);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            String symbolicName = text.subSequence(position, endPosition).toString();
            int type = Tokens.getTokenType(symbolicName, context.vocabulary());
            context.addField(TokenField.TYPE, type);
            return endPosition;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return ~position;
            }
            int longestMatch = 0;
            for (int i = -1; i < vocabulary.getMaxTokenType() + 1; i++) {
                String symbolicName = vocabulary.getSymbolicName(i);
                if (symbolicName == null) {
                    continue;
                }
                int length = symbolicName.length();
                if (position + length > text.length()) {
                    continue;
                }
                if (symbolicName.equals(text.subSequence(position, position + length).toString())) {
                    longestMatch = Math.max(longestMatch, length);
                }
            }
            return longestMatch == 0 ? ~position : position + longestMatch;
        }
    }

    static class LiteralTypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String literalName = vocabulary.getLiteralName(context.token().getType());
            if (literalName == null) {
                return false;
            }
            buf.append(literalName);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            String literalName = text.subSequence(position, endPosition).toString();
            int type = findLiteralType(literalName, context.vocabulary());
            context.addField(TokenField.TYPE, type);
            context.addField(TokenField.TEXT, literalName.substring(1, literalName.length() - 1));
            return endPosition;
        }

        private int findLiteralType(String literal, Vocabulary vocabulary) {
            for (int i = 1; i < vocabulary.getMaxTokenType() + 1; i++) {
                if (Objects.equals(literal, vocabulary.getLiteralName(i))) {
                    return i;
                }
            }
            throw new RuntimeException("No literal type found");
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return ~position;
            }
            int longestMatch = 0;
            for (int i = 1; i < vocabulary.getMaxTokenType() + 1; i++) {
                String literalName = vocabulary.getLiteralName(i);
                if (literalName == null) {
                    continue;
                }
                int length = literalName.length();
                if (position + length > text.length()) {
                    continue;
                }
                if (literalName.equals(text.subSequence(position, position + length).toString())) {
                    longestMatch = Math.max(longestMatch, length);
                }
            }
            return longestMatch == 0 ? ~position : position + longestMatch;
        }
    }

    static class TypePrinterParser implements TokenPrinterParser {

        private TokenPrinterParser[] printerParsers;

        TypePrinterParser(TypeFormat format) {
            switch (format) {
                case INTEGER_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new IntegerPrinterParser(
                                TokenField.TYPE)};
                case SYMBOLIC_FIRST ->
                        printerParsers = new TokenPrinterParser[]{new SymbolicTypePrinterParser(),
                                new LiteralTypePrinterParser(),
                                new IntegerPrinterParser(TokenField.TYPE)};
                case LITERAL_FIRST ->
                        printerParsers = new TokenPrinterParser[]{new LiteralTypePrinterParser(),
                                new SymbolicTypePrinterParser(),
                                new IntegerPrinterParser(TokenField.TYPE)};
                case SYMBOLIC_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new SymbolicTypePrinterParser()};
                case LITERAL_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new LiteralTypePrinterParser()};
            }
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            for (TokenPrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                if (parser.peek(context, text, position) > 0) {
                    return parser.parse(context, text, position);
                }
            }
            return ~position;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                int peeked = parser.peek(context, text, position);
                if (peeked > 0) {
                    return peeked;
                }
            }
            return ~position;
        }
    }

    static class EOFPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Token token = context.token();
            if (token.getType() == Token.EOF) {
                buf.append("EOF");
                return true;
            }
            return false;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int end = peek(context, text, position);
            if (end < 0) {
                return end;
            }
            context.addField(TokenField.TYPE, Token.EOF);
            context.addField(TokenField.TEXT, "<EOF>");
            return end;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            if (position + 3 > text.length()) {
                return ~position;
            }
            boolean matches = text.subSequence(position, position + 3).toString().equals("EOF");
            return matches ? position + 3 : ~position;
        }
    }
}