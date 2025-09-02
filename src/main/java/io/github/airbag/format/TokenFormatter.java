package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.*;

import static java.util.Map.entry;

/**
 * A formatter for ANTLR {@link Token} objects.
 * <p>
 * This class can be used to format a token into a string representation, or to parse a string into a token.
 * The format of the string is defined by a {@link TokenFormatterBuilder}.
 */
public class TokenFormatter {

    //TODO channel as optional
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
            .appendLiteral(">,")
            .appendInteger(TokenField.LINE)
            .appendLiteral(":")
            .appendInteger(TokenField.POSITION)
            .appendLiteral("]")
            .toFormatter();

    public static final TokenFormatter SIMPLE =
            new TokenFormatterBuilder().appendLiteralType()
                    .toFormatter()
                    .withAlternative(new TokenFormatterBuilder().appendEOF().toFormatter())
                    .withAlternative(
                            new TokenFormatterBuilder().appendLiteral("(")
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
                                    .toFormatter());

    //TODO
    public static final TokenFormatter JSON = null;

    //TODO
    public static final TokenFormatter XML = null;

    /**
     * A list of printer parsers which will be applied in order until the fist success.
     */
    private final List<CompositePrinterParser> printerParsers;

    /**
     * The set of token fields that this formatter operates on.
     */
    private final Set<TokenField<?>> fields;

    /**
     * The ANTLR vocabulary used for resolving symbolic and literal token names.
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
     * Parses a string into a token.
     *
     * @param input The string to parse.
     * @return The parsed token.
     * @throws TokenParseException if the string cannot be parsed.
     */
    public Token parse(String input) {
        int position = 0;
        TokenParseContext ctx = null;
        for (var parser : printerParsers) {
            ctx = new TokenParseContext(new HashMap<>(), parser, vocabulary);
            position = parser.parse(ctx, input, 0);
            if (position > 0) {
                break;
            }
        }
        if (position < 0) {
            throw new TokenParseException(input, ~position);
        }
        return Objects.requireNonNull(ctx).resolveFields();
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