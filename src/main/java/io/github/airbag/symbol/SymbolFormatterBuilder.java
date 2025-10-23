package io.github.airbag.symbol;

import org.antlr.v4.runtime.Vocabulary;

import java.util.*;

/**
 * Builder for creating {@link SymbolFormatter} instances.
 * <p>
 * This builder provides a flexible and powerful way to define custom formats for
 * converting {@link Symbol} objects to and from strings.
 * It is the primary mechanism for constructing {@link SymbolFormatter}s, which are
 * immutable and thread-safe once created.
 *
 * <h3>Overview</h3>
 * The builder uses a fluent API to assemble a sequence of "printer-parsers".
 * Each printer-parser is a component responsible for a specific part of the
 * format. For example, one component might handle the symbol's text, while another
 * handles its symbolic type name.
 * <p>
 * A {@link SymbolFormatter} has two main functions:
 * <ul>
 *   <li><b>Formatting (Printing):</b> Converting a {@code Symbol} object into a string.</li>
 *   <li><b>Parsing:</b> Converting a string back into a {@code Symbol} object's constituent parts.</li>
 * </ul>
 * The sequence of appended components defines the exact format for both operations.
 *
 * <h3>Usage</h3>
 * To create a formatter, you instantiate a {@code SymbolFormatterBuilder} and call
 * various {@code append...} methods to define the desired format. Once the
 * format is defined, you call {@link #toFormatter()} to create the
 * {@link SymbolFormatter} instance.
 *
 * <p><b>Example: Simple Formatter</b></p>
 * <pre>{@code
 * // Creates a formatter that represents a symbol as "SYMBOLIC_NAME:'text'"
 * SymbolFormatter formatter = new SymbolFormatterBuilder()
 *     .appendSymbolicType()
 *     .appendLiteral(":'")
 *     .appendText()
 *     .appendLiteral("'")
 *     .toFormatter();
 *
 * // Formatting example:
 * // Assuming a symbol with symbolic name "ID" and text "user",
 * // the output of formatter.format(symbol, vocabulary) would be:
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
 * SymbolFormatter antlrStyleFormatter = new SymbolFormatterBuilder()
 *     .appendPattern(pattern)
 *     .toFormatter();
 *
 * // Example output for a symbol:
 * // "[@-1,0:3='text',<1>,1:0]"
 * }</pre>
 *
 * <h3>State and Thread-Safety</h3>
 * This builder is a stateful object and is <b>not</b> thread-safe. It should be
 * used to create a formatter and then discarded. The resulting {@link SymbolFormatter}
 * objects, however, are immutable and safe for use in multithreaded environments.
 *
 * @see SymbolFormatter
 * @see TypeFormat
 * @see TextOption
 */
public class SymbolFormatterBuilder {

    /**
     * The list of printer/parsers that make up the format of the symbol.
     */
    private final List<SymbolPrinterParser> printerParsers = new ArrayList<>();

    /**
     * The list of symbol fields that are used by the printer/parsers.
     */
    private final Set<SymbolField<?>> fields = new HashSet<>();

    /**
     * The start index of the optional section
     */
    private int optionalStart = -1;

    /**
     * Appends a printer/parser for an integer field to the formatter.
     *
     * @param field The integer field to append.
     * @return This builder.
     */
    public SymbolFormatterBuilder appendInteger(SymbolField<Integer> field) {
        return appendInteger(field, false);
    }

    /**
     * Appends a printer/parser for an integer field to the formatter, with optional strict formatting.
     * <p>
     * When strict formatting is enabled, the integer value will only be printed if it is not
     * equal to the default value of the provided {@link SymbolField}. This is useful for omitting
     * optional fields that have not been explicitly set.
     * <p>
     * For example, if used with {@link SymbolField#CHANNEL} and strict mode, the channel will not be
     * printed if it is the default channel (0).
     *
     * @param field    The integer field to append.
     * @param isStrict {@code true} to enable strict formatting, {@code false} otherwise.
     * @return This builder.
     */
    public SymbolFormatterBuilder appendInteger(SymbolField<Integer> field, boolean isStrict) {
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
    public SymbolFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a printer/parser for the symbol's text.
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
    public SymbolFormatterBuilder appendText() {
        printerParsers.add(new TextPrinterParser());
        fields.add(SymbolField.TEXT);
        return this;
    }

    /**
     * Appends a printer/parser for the symbol's text, with custom escaping and default value behavior.
     * <p>
     * This component is non-greedy; it consumes characters only up to the point
     * where the next component in the formatter is able to match. If this is the
     * last component in the sequence, it will consume the remainder of the input string.
     *
     * @param option The {@link TextOption} to use for formatting and parsing the text.
     * @return This builder.
     * @see TextOption
     */
    public SymbolFormatterBuilder appendText(TextOption option) {
        printerParsers.add(new TextPrinterParser(option));
        fields.add(SymbolField.TEXT);
        return this;
    }

    /**
     * Appends a printer/parser for the symbol's symbolic type name (e.g., "ID", "INT").
     * <p>
     * This component provides a strict mapping between a symbol's type and its symbolic name
     * as defined in the ANTLR {@link Vocabulary}.
     * <p>
     * <b>Formatting:</b> It will throw a {@link SymbolFormatterException} if the vocabulary is missing or
     * if the symbol's type does not have a symbolic name. This is often the case for tokens
     * representing literals (e.g., keywords, operators like {@code '='}), which have a literal
     * name but not a symbolic one.
     * <p>
     * <b>Parsing:</b> It reads a symbolic name from the input and resolves it back to the
     * corresponding symbol type.
     *
     * @return This builder.
     */
    public SymbolFormatterBuilder appendSymbolicType() {
        printerParsers.add(new SymbolicTypePrinterParser());
        fields.add(SymbolField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the symbol's literal type name (e.g., {@code '='}, {@code '*'}).
     * <p>
     * This component maps a symbol's type to its literal name as defined in the ANTLR
     * {@link Vocabulary}. Literal names are the exact strings defined in the grammar,
     * typically enclosed in single quotes (e.g., {@code '='}).
     * <p>
     * <b>Formatting:</b> The literal name from the vocabulary (including the single quotes)
     * is appended to the output. It will throw a {@link SymbolFormatterException} if the vocabulary
     * is missing or if the symbol's type does not have a literal name. This is often the
     * case for tokens with symbolic names like {@code ID} or {@code INT}.
     * <p>
     * <b>Parsing:</b> The parser expects the input to contain the literal name, including
     * the single quotes. It finds the longest possible literal in the vocabulary that
     * matches the input string and resolves it to the corresponding symbol type.
     *
     * @return This builder.
     */
    public SymbolFormatterBuilder appendLiteralType() {
        printerParsers.add(new LiteralTypePrinterParser());
        fields.add(SymbolField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the symbol's type, with a configurable format.
     * <p>
     * This method provides a flexible way to format and parse a symbol's type by specifying
     * a {@link TypeFormat}. The format determines which representations of the type are
     * used and in what order. For example, a type can be represented by its symbolic name
     * (e.g., "ID"), its literal name (e.g., "'='"), or its raw integer value.
     * <p>
     * <b>Formatting:</b> The formatter will attempt to represent the symbol's type using the
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
     * @param format The format to use for the symbol's type.
     * @return This builder.
     * @see TypeFormat
     */
    public SymbolFormatterBuilder appendType(TypeFormat format) {
        printerParsers.add(new TypePrinterParser(format));
        fields.add(SymbolField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the end-of-file (EOF) symbol.
     * <p>
     * <b>Formatting:</b> If the symbol's type is {@link Symbol#EOF}, this component
     * appends the string "EOF". For any other symbol type, it fails.
     * <p>
     * <b>Parsing:</b> It matches the literal string "EOF" and resolves it to a
     * symbol of type {@link Symbol#EOF}.
     *
     * @return This builder.
     */
    public SymbolFormatterBuilder appendEOF() {
        printerParsers.add(new EOFPrinterParser());
        fields.add(SymbolField.TYPE);
        return this;
    }

    /**
     * Appends a whitespace component that formats with a single space and parses any amount of whitespace.
     * <p>
     * This is a convenience method for {@code appendWhitespace(" ")}.
     *
     * <p><b>Formatting:</b></p>
     * No whitespace is added while formatting
     *
     * <p><b>Parsing:</b></p>
     * The parser will greedily consume any number of consecutive whitespace characters (zero or more)
     * from the input. This component will always succeed. Therefore, this component must not be
     * followed by a literal beginning a whitespace character.
     *
     * @return This builder.
     * @see #appendWhitespace(String)
     */
    public SymbolFormatterBuilder appendWhitespace() {
        return appendWhitespace("");
    }

    /**
     * Appends a flexible whitespace component to the formatter.
     * <p>
     * This component provides a powerful way to handle spacing between other elements in the format.
     * It allows the parser to be insensitive to variations in whitespace while enabling the formatter
     * to produce a clean, consistent output with a preferred spacing.
     *
     * <p><b>Formatting:</b></p>
     * The specified {@code whitespace} string is appended to the output. This allows you to define
     * the canonical spacing for your format (e.g., a single space, a tab, or even an empty string).
     *
     * <p><b>Parsing:</b></p>
     * The parser will greedily consume any number of consecutive whitespace characters (e.g., spaces, tabs, newlines)
     * from the input string. The actual whitespace found in the input does not need to match the
     * preferred {@code whitespace} string provided. This operation always succeeds, even if no
     * whitespace is present. Therefore, this component must not be
     * followed by a literal beginning a whitespace character.
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * // Creates a formatter for a symbolic name in parentheses with one space of padding.
     * SymbolFormatter formatter = new SymbolFormatterBuilder()
     *     .appendLiteral("(")
     *     .appendWhitespace(" ")
     *     .appendSymbolicType()
     *     .appendWhitespace(" ")
     *     .appendLiteral(")")
     *     .toFormatter();
     *
     * // This will correctly parse all of the following:
     * formatter.parse("(ID)");
     * formatter.parse("( ID)");
     * formatter.parse("(ID   )");
     *
     * // And will always format the symbol back to a consistent string:
     * // Returns "( ID )"
     * formatter.format(someSymbol);
     * }</pre>
     *
     * @param whitespace The preferred whitespace string to use during formatting. This string must
     *                   only contain whitespace characters as defined by {@link Character#isWhitespace(char)}.
     * @return This builder.
     * @throws IllegalArgumentException if the {@code whitespace} string contains any non-whitespace characters.
     */
    public SymbolFormatterBuilder appendWhitespace(String whitespace) {
        printerParsers.add(new WhitespacePrinterParser(whitespace));
        return this;
    }

    /**
     * Appends a printer and parser to the formatter using a flexible pattern string.
     * <p>
     * This method allows for the creation of a formatter by defining a pattern string,
     * which specifies the desired arrangement of symbol components. The pattern supports
     * various letters, each representing a specific field of a {@link Symbol}.
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
     * that could be interpreted as pattern modifiers, you can enclose the sequence in {@code '} characters.
     * Everything between the opening and closing {@code '} is treated as one literal block.
     * For example, {@code 's'} would result in the literal "s" being printed, not the symbolic name.
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
     * backslash {@code \\}. To include a literal percent sign, use {@code \'}.
     *
     * <h3>Examples</h3>
     * <pre>{@code
     *   // Format a symbol as "SYMBOLIC_NAME:'text'"
     *   // For a symbol with symbolic name "ID" and text "user", the output would be "ID:'user'"
     *   String pattern1 = "s:\\'x\\'";
     *
     *   // Replicate ANTLR's default Symbol.toString() format
     *   // Example output: [@-1,0:3='text',<0>,1:0]
     *   // Using quoted blocks for all literal parts to avoid ambiguity.
     *   String antlrPattern = "\\[@N,B:E=\\'X\\',<L>,['channel'=c],R:P\\]";
     *
     *   // Format a symbol with an optional channel display
     *   // If the channel is not the default, it will be included (e.g., "ID[channel=1]")
     *   // Otherwise, it will be omitted (e.g., "ID")
     *   String optionalChannelPattern = "s[\\['channel'=c\\]]";
     * }</pre>
     *
     * @param pattern the pattern string to define the formatter.
     * @return this builder.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    public SymbolFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        parsePattern(pattern);
        return this;
    }

    private static final Set<Character> PATTERN_LETTERS = Set.of('I',
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
            'r');

    private static final Set<Character> SPECIAL_CHARACTERS = Set.of('[', ']', '\'', '\\');

    private void parsePattern(String pattern) {
        StringBuilder literalBuf = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (PATTERN_LETTERS.contains(c)) {
                flushLiteralBuf(literalBuf);
                switch (c) {
                    case 'I' -> appendInteger(SymbolField.TYPE);
                    case 'S' -> appendType(TypeFormat.SYMBOLIC_FIRST);
                    case 's' -> appendSymbolicType();
                    case 'L' -> appendType(TypeFormat.LITERAL_FIRST);
                    case 'l' -> appendLiteralType();
                    case 'x' -> appendText();
                    case 'X' -> appendText(TextOption.ESCAPED);
                    case 'N' -> appendInteger(SymbolField.INDEX);
                    case 'n' -> appendInteger(SymbolField.INDEX, true);
                    case 'B' -> appendInteger(SymbolField.START);
                    case 'b' -> appendInteger(SymbolField.START, true);
                    case 'E' -> appendInteger(SymbolField.STOP);
                    case 'e' -> appendInteger(SymbolField.STOP, true);
                    case 'C' -> appendInteger(SymbolField.CHANNEL);
                    case 'c' -> appendInteger(SymbolField.CHANNEL, true);
                    case 'P' -> appendInteger(SymbolField.POSITION);
                    case 'p' -> appendInteger(SymbolField.POSITION, true);
                    case 'R' -> appendInteger(SymbolField.LINE);
                    case 'r' -> appendInteger(SymbolField.LINE, true);
                }
            } else {
                switch (c) {
                    case '\'' -> {
                        flushLiteralBuf(literalBuf);
                        i++; // Skip opening '
                        int contentStart = i;
                        while (i < pattern.length() && pattern.charAt(i) != '\'') {
                            i++;
                        }
                        if (i >= pattern.length()) {
                            throw new SymbolFormatterException("Unclosed quoted literal in pattern: " +
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
                            throw new SymbolFormatterException("Invalid escape sequence at end of pattern: " +
                                                               pattern);
                        }
                        literalBuf.append(pattern.charAt(i));
                    }
                    case '[' -> {
                        flushLiteralBuf(literalBuf);
                        startOptional();
                    }
                    case ']' -> {
                        flushLiteralBuf(literalBuf);
                        endOptional();
                    }
                    default -> {
                        if (Character.isWhitespace(c)) {
                            flushLiteralBuf(literalBuf);
                            int j = i;
                            while (j < pattern.length() && Character.isWhitespace(pattern.charAt(j))) {
                                j++;
                            }
                            appendWhitespace(pattern.substring(i, j));
                            i = j - 1;
                        } else {
                            literalBuf.append(c);
                        }
                    }
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
    public SymbolFormatterBuilder startOptional() {
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
    public SymbolFormatterBuilder endOptional() {
        if (optionalStart == -1) {
            throw new IllegalStateException("Cannot end optional without starting one");
        }
        if (optionalStart == printerParsers.size()) {
            optionalStart = -1;
            return this;
        }
        List<SymbolPrinterParser> optionalList = printerParsers.subList(optionalStart,
                printerParsers.size());
        CompositePrinterParser optional = new CompositePrinterParser(new ArrayList<>(optionalList),
                true);
        optionalList.clear();
        printerParsers.add(optional);
        optionalStart = -1;
        return this;
    }

    /**
     * Builds the symbol formatter.
     *
     * @return The built symbol formatter.
     */
    public SymbolFormatter toFormatter() {
        return new SymbolFormatter(new CompositePrinterParser(printerParsers, false), fields, null);
    }

    /**
     * The internal interface for parsing and formatting.
     * This interface is the building block for the composite {@link SymbolFormatter}.
     * It defines the dual functionality of formatting (printing) a symbol stream
     * and parsing an input character sequence.
     */
    interface SymbolPrinterParser {

        /**
         * Formats a value from a context into a string buffer.
         *
         * @param context the context holding the values to be formatted.
         * @param buf     the buffer to append the formatted text to.
         * @return true if the formatting was successful, false otherwise.
         */
        boolean format(SymbolFormatContext context, StringBuilder buf);

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
        int parse(SymbolParseContext context, CharSequence text, int position);

        /**
         * Peeks ahead in the text to see if the next characters match this parser's rule,
         * but does not consume them. Implementations of this method must not alter the parse context.
         *
         * @param context  the parse context.
         * @param text     the text to peek into.
         * @param position the position to start peeking from.
         * @return the position of the potential match if successful, or a negative value if it does not match.
         */
        int peek(SymbolParseContext context, CharSequence text, int position);

        /**
         * Returns {@code true} if the printer parser is optional.
         *
         * @return {@code true} if the printer parser is optional.
         */
        default boolean isOptional() {
            return false;
        }

    }

    static final class CompositePrinterParser implements SymbolPrinterParser {

        private final SymbolPrinterParser[] printerParsers;

        private final boolean isOptional;

        private CompositePrinterParser(List<SymbolPrinterParser> printerParsers,
                                       boolean isOptional) {
            this(printerParsers.toArray(new SymbolPrinterParser[0]), isOptional);
        }

        private CompositePrinterParser(SymbolPrinterParser[] printerParsers, boolean isOptional) {
            this.printerParsers = printerParsers;
            this.isOptional = isOptional;
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            int initialLength = buf.length();
            for (SymbolPrinterParser printer : printerParsers) {
                if (!printer.format(context, buf)) {
                    buf.setLength(initialLength);
                    return isOptional;
                }
            }
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            if (isOptional) {
                int peekPosition = peek(context, text, position);
                if (peekPosition < 0) {
                    return position;
                }
            }
            for (SymbolPrinterParser parser : printerParsers) {
                position = parser.parse(context, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            for (SymbolPrinterParser parser : printerParsers) {
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

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (isOptional) {
                buf.append("[");
            }
            Arrays.stream(printerParsers).forEach(buf::append);
            if (isOptional) {
                buf.append("]");
            }
            return buf.toString();
        }
    }

    static class IntegerPrinterParser implements SymbolPrinterParser {

        private final SymbolField<Integer> integerSymbolField;
        private final boolean isStrict;

        IntegerPrinterParser(SymbolField<Integer> integerSymbolField, boolean isStrict) {
            this.isStrict = isStrict;
            this.integerSymbolField = integerSymbolField;
        }

        IntegerPrinterParser(SymbolField<Integer> integerSymbolField) {
            this(integerSymbolField, false);
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            if (isStrict) {
                int value = integerSymbolField.access(context.symbol());
                if (value == integerSymbolField.getDefault()) {
                    return false;
                }
                buf.append(value);
            } else {
                buf.append(integerSymbolField.access(context.symbol()));
            }
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int numberEnd = peek(context, text, position);
            if (numberEnd < 0) {
                context.setErrorMessage(
                        "Expected an integer for field '%s' but found '%s'".formatted(
                                integerSymbolField.name(),
                                textLookahead(text, position, 3)));
                return numberEnd;
            }
            context.addField(integerSymbolField,
                    Integer.valueOf(text.subSequence(position, numberEnd).toString()));
            return numberEnd;
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            if (text.charAt(position) == '-') {
                int result = findNumberEnd(text, position + 1);
                if (result < 0) {
                    return -~result;
                }
                return result;
            }
            return findNumberEnd(text, position);
        }

        @Override
        public String toString() {
            switch (integerSymbolField.name()) {
                case "type" -> {
                    return "I";
                }
                case "index" -> {
                    return isStrict ? "n" : "N";
                }
                case "line" -> {
                    return isStrict ? "r" : "R";
                }
                case "position" -> {
                    return isStrict ? "p" : "P";
                }
                case "channel" -> {
                    return isStrict ? "c" : "C";
                }
                case "start" -> {
                    return isStrict ? "b" : "B";
                }
                case "stop" -> {
                    return isStrict ? "e" : "E";
                }
                default -> throw new RuntimeException();
            }
        }
    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    private static String textLookahead(CharSequence text, int position, int length) {
        if (position == text.length()) {
            return "<text end>";
        }
        return text.subSequence(position, Math.min(text.length(), position + length)).toString();
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

    static class LiteralPrinterParser implements SymbolPrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int result = peek(context, text, position);
            if (result < 0) {
                context.setErrorMessage("Expected literal '%s' but found '%s'".formatted(
                                literal,
                                textLookahead(text, position, literal.length()))
                        .replace("\n", "\\n")
                        .replace("\t", "\\t")
                        .replace("\r", "\\r"));

            }
            return result;
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }

        @Override
        public String toString() {
            if (literal.isEmpty()) {
                return "";
            }
            long specialCharCount = literal.chars()
                    .filter(c -> PATTERN_LETTERS.contains((char) c) ||
                                 SPECIAL_CHARACTERS.contains((char) c))
                    .count();

            if (specialCharCount == 0) {
                return literal;
            }

            if (specialCharCount > 1 && !literal.contains("'")) {
                return "'" + literal + "'";
            } else {
                StringBuilder sb = new StringBuilder();
                for (char c : literal.toCharArray()) {
                    if (PATTERN_LETTERS.contains(c) || SPECIAL_CHARACTERS.contains(c)) {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
                return sb.toString();
            }
        }
    }

    static class TextPrinterParser implements SymbolPrinterParser {

        private final TextOption option;

        TextPrinterParser() {
            this(TextOption.NOTHING);
        }

        TextPrinterParser(TextOption option) {
            this.option = option;
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            String text = context.symbol().text();
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
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                context.setErrorMessage(
                        "Invalid escape sequence found near '%s'".formatted(
                                textLookahead(text, position, 10)));
                return endPosition;
            }
            StringBuilder buf = unescapeText(text, position, endPosition);
            String tokenText = buf.toString();
            if (tokenText.equals(option.getDefaultValue())) {
                tokenText = "";
            }
            context.addField(SymbolField.TEXT, tokenText);
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
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            SymbolPrinterParser[] parserChain = context.printerParser().printerParsers;
            SymbolPrinterParser[] successors = getSuccessors(parserChain);

            if (successors.length == 0) {
                return text.length();
            }

            var unescapeMap = option.getUnescapeMap();
            var escapeChar = option.getEscapeChar();
            while (position < text.length()) {
                boolean match = false;
                for (var successor : successors) {
                    int successorEnd = successor.peek(context, text, position);
                    if (successorEnd >= 0 && successorEnd != position) {
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

        private int findParserIndex(SymbolPrinterParser[] parserChain) {
            for (int i = 0; i < parserChain.length; i++) {
                if (parserChain[i] == this) {
                    return i;
                }
            }
            throw new RuntimeException("Parser is not part of the chain");
        }

        private SymbolPrinterParser[] getSuccessors(SymbolPrinterParser[] parserChain) {
            int parserIndex = findParserIndex(parserChain);
            if (parserIndex < 0 || parserIndex == parserChain.length - 1) {
                return new SymbolPrinterParser[0];
            }

            List<SymbolPrinterParser> delimiters = new ArrayList<>();
            for (int i = parserIndex + 1; i < parserChain.length; i++) {
                SymbolPrinterParser successor = parserChain[i];
                delimiters.add(successor);
                if (!successor.isOptional()) {
                    break;
                }
            }
            return delimiters.toArray(new SymbolPrinterParser[0]);
        }

        @Override
        public String toString() {
            return option == TextOption.NOTHING ? "x" : "X";
        }
    }

    static class SymbolicTypePrinterParser implements SymbolPrinterParser {

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String symbolicName = vocabulary.getSymbolicName(context.symbol().type());
            if (symbolicName == null) {
                return false;
            }
            buf.append(symbolicName);
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                if (context.vocabulary() == null) {
                    context.setErrorMessage("No vocabulary set");
                } else {
                    context.setErrorMessage("Unrecognized symbolic type name starting with '%s'".formatted(
                            textLookahead(text, position, 5)));
                }
                return endPosition;
            }
            String symbolicName = text.subSequence(position, endPosition).toString();
            int type = getType(symbolicName, context.vocabulary());
            context.addField(SymbolField.TYPE, type);
            return endPosition;
        }

        private int getType(String symbolicName, Vocabulary vocabulary) {
            for (int i = -1; i < vocabulary.getMaxTokenType() + 1; i++) {
                if (Objects.equals(symbolicName, vocabulary.getSymbolicName(i))) {
                    return i;
                }
            }
            throw new RuntimeException("No symbolic type found");
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
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

        @Override
        public String toString() {
            return "s";
        }
    }

    static class LiteralTypePrinterParser implements SymbolPrinterParser {

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String literalName = vocabulary.getLiteralName(context.symbol().type());
            if (literalName == null) {
                return false;
            }
            buf.append(literalName);
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                if (context.vocabulary() == null) {
                    context.setErrorMessage("No vocabulary set");
                } else {
                    context.setErrorMessage("Unrecognized literal type name starting with '%s'".formatted(
                            textLookahead(text, position, 5)));
                }

                return endPosition;
            }
            String literalName = text.subSequence(position, endPosition).toString();
            int type = findLiteralType(literalName, context.vocabulary());
            context.addField(SymbolField.TYPE, type);
            context.addField(SymbolField.TEXT, literalName.substring(1, literalName.length() - 1));
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
        public int peek(SymbolParseContext context, CharSequence text, int position) {
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

        @Override
        public String toString() {
            return "l";
        }
    }

    static class TypePrinterParser implements SymbolPrinterParser {

        private SymbolPrinterParser[] printerParsers;
        private final TypeFormat format;

        TypePrinterParser(TypeFormat format) {
            switch (format) {
                case INTEGER_ONLY ->
                        printerParsers = new SymbolPrinterParser[]{new IntegerPrinterParser(
                                SymbolField.TYPE)};
                case SYMBOLIC_FIRST ->
                        printerParsers = new SymbolPrinterParser[]{new SymbolicTypePrinterParser(),
                                new LiteralTypePrinterParser(),
                                new IntegerPrinterParser(SymbolField.TYPE)};
                case LITERAL_FIRST ->
                        printerParsers = new SymbolPrinterParser[]{new LiteralTypePrinterParser(),
                                new SymbolicTypePrinterParser(),
                                new IntegerPrinterParser(SymbolField.TYPE)};
                case SYMBOLIC_ONLY ->
                        printerParsers = new SymbolPrinterParser[]{new SymbolicTypePrinterParser()};
                case LITERAL_ONLY ->
                        printerParsers = new SymbolPrinterParser[]{new LiteralTypePrinterParser()};
            }
            this.format = format;
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            for (SymbolPrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            for (SymbolPrinterParser parser : printerParsers) {
                if (parser.peek(context, text, position) > 0) {
                    return parser.parse(context, text, position);
                }
            }
            context.setErrorMessage("Unrecognized type information starting with '%s'".formatted(
                    textLookahead(text, position, 5)));
            return ~position;
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            for (SymbolPrinterParser parser : printerParsers) {
                int peeked = parser.peek(context, text, position);
                if (peeked > 0) {
                    return peeked;
                }
            }
            return ~position;
        }

        @Override
        public String toString() {
            return switch (format) {
                case INTEGER_ONLY -> "i";
                case SYMBOLIC_ONLY -> "s";
                case LITERAL_ONLY -> "l";
                case SYMBOLIC_FIRST -> "S";
                case LITERAL_FIRST -> "L";
            };
        }
    }

    static class EOFPrinterParser implements SymbolPrinterParser {

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            Symbol symbol = context.symbol();
            if (symbol.type() == Symbol.EOF) {
                buf.append("EOF");
                return true;
            }
            return false;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            int end = peek(context, text, position);
            if (end < 0) {
                context.setErrorMessage("Expected 'EOF' but found '%s'".formatted(textLookahead(text,
                        position, 3)));
                return end;
            }
            context.addField(SymbolField.TYPE, Symbol.EOF);
            context.addField(SymbolField.TEXT, "<EOF>");
            return end;
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            if (position + 3 > text.length()) {
                return ~position;
            }
            boolean matches = text.subSequence(position, position + 3).toString().equals("EOF");
            return matches ? position + 3 : ~position;
        }

        @Override
        public String toString() {
            return "<EOF>";
        }
    }

    static class WhitespacePrinterParser implements SymbolPrinterParser {

        private final String whitespace;

        public WhitespacePrinterParser(String whitespace) {
            if (!whitespace.chars().allMatch(Character::isWhitespace)) {
                throw new IllegalArgumentException("Can only append whitespace");
            }
            this.whitespace = whitespace;
        }

        @Override
        public boolean format(SymbolFormatContext context, StringBuilder buf) {
            buf.append(whitespace);
            return true;
        }

        @Override
        public int parse(SymbolParseContext context, CharSequence text, int position) {
            return peek(context, text, position);
        }

        @Override
        public int peek(SymbolParseContext context, CharSequence text, int position) {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
            return position;
        }

        @Override
        public String toString() {
            return whitespace;
        }
    }
}