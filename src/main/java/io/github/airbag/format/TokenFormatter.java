package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * A formatter for ANTLR {@link Token} objects.
 * <p>
 * This class can be used to format a token into a string representation, or to parse a string into a token.
 * The format of the string is defined by a {@link TokenFormatterBuilder}.
 */
public class TokenFormatter {

    private final CompositePrinterParser printerParser;

    private final List<TokenField<?>> fields;

    /**
     * Constructs a new TokenFormatter.
     *
     * @param printerParser The printer/parser to use for formatting and parsing.
     * @param fields The fields that are used by this formatter.
     */
    TokenFormatter(CompositePrinterParser printerParser,
                          List<TokenField<?>> fields) {
        this.printerParser = printerParser;
        this.fields = List.copyOf(fields);
    }

    /**
     * Formats a token into a string.
     *
     * @param token The token to format.
     * @return The formatted string.
     * @throws TokenException if the token cannot be formatted.
     */
    public String format(Token token) {
        TokenFormatContext ctx = new TokenFormatContext(token);
        StringBuilder buf = new StringBuilder();
        if (!printerParser.format(ctx, buf)) {
            throw new TokenException("Failed to format token %s".formatted(Tokens.format(token, null)));
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
        TokenParseContext ctx = new TokenParseContext(new HashMap<>());
        int position = printerParser.parse(ctx, input, 0);
        if (position < 0) {
            throw new TokenParseException(input, ~position);
        }
        return ctx.resolveFields();
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