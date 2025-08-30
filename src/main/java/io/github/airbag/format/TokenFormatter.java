package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * A formatter for ANTLR {@link Token} objects.
 * <p>
 * This class can be used to format a token into a string representation, or to parse a string into a token.
 * The format of the string is defined by a {@link TokenFormatterBuilder}.
 */
public class TokenFormatter {

    //TODO type, channel as optional
    public static final TokenFormatter ANTLR = new TokenFormatterBuilder().appendLiteral("[@")
            .appendInteger(TokenField.INDEX)
            .appendLiteral(",")
            .appendInteger(TokenField.START)
            .appendLiteral(":")
            .appendInteger(TokenField.STOP)
            .appendLiteral("='")
            .appendText()
            .appendLiteral("',<")
            .appendInteger(TokenField.TYPE)
            .appendLiteral(">,")
            .appendInteger(TokenField.LINE)
            .appendLiteral(":")
            .appendInteger(TokenField.POSITION)
            .appendLiteral("]")
            .toFormatter();

    //TODO alternatives
    public static final TokenFormatter SIMPLE = new TokenFormatterBuilder().appendLiteral("(")
            .appendInteger(TokenField.TYPE)
            .appendLiteral(" '")
            .appendText()
            .appendLiteral("')")
            .toFormatter();

    //TODO
    public static final TokenFormatter JSON = null;

    //TODO
    public static final TokenFormatter XML = null;

    /**
     * The composite printer/parser that defines the formatting and parsing logic.
     */
    private final CompositePrinterParser printerParser;

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
        this.printerParser = printerParser;
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
        if (!printerParser.format(ctx, buf)) {
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
        TokenParseContext ctx = new TokenParseContext(new HashMap<>(), printerParser, vocabulary);
        int position = printerParser.parse(ctx, input, 0);
        if (position < 0) {
            throw new TokenParseException(input, ~position);
        }
        return ctx.resolveFields();
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
        return new TokenFormatter(printerParser, fields, vocabulary);
    }

    /**
     * Returns a {@link BiPredicate} that can be used to compare two tokens for equality.
     * <p>
     * The predicate compares only the fields that are used by this formatter.
     *
     * @return A predicate that can be used to compare two tokens for equality.
     */
    public BiPredicate<Token, Token> equalizer() {
        return Tokens.equalizer(fields);
    }

}