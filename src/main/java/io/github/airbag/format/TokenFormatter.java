package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.format.TokenFormatterBuilder.TokenPrinterParser;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

/**
 * Formatter for printing and parsing ANTLR tokens.
 * <p>
 * This class provides the main API for formatting and parsing. It is immutable and thread-safe.
 * <p>
 * A formatter is created using {@link TokenFormatterBuilder}.
 *
 * @see TokenFormatterBuilder
 * @since 1.0
 */
public class TokenFormatter {

    public static final TokenFormatter ANTLR = antlrFormat(null);

    private final TokenPrinterParser printerParser;

    private final Vocabulary vocabulary;

    TokenFormatter(CompositePrinterParser printerParser) {
        this(printerParser, null);
    }

    TokenFormatter(CompositePrinterParser printerParser, Vocabulary vocabulary) {
        this.printerParser = printerParser;
        this.vocabulary = vocabulary;
    }

    public static TokenFormatter antlrFormat(Vocabulary vocabulary) {
        return new TokenFormatterBuilder().appendLiteral("[@")
                .appendIndex()
                .appendLiteral(",")
                .appendStartIndex()
                .appendLiteral(":")
                .appendStopIndex()
                .appendLiteral("=")
                .appendLiteral("'")
                .appendText()
                .appendLiteral("',<")
                .appendType()
                .appendLiteral(">,")
                .appendLine()
                .appendLiteral(":")
                .appendPosition()
                .appendLiteral(']')
                .setVocabulary(vocabulary)
                .build();
    }

    /**
     * Parses the given string to produce a token.
     *
     * @param input the string to parse
     * @return the parsed token
     */
    public Token parse(String input) {
        var context = new TokenParseContext();
        context.setBuilder(Tokens.singleTokenOf());
        int position = printerParser.parse(context, input, 0);
        if (position < 0) {
            throw new TokenParseException(input, position, printerParser);
        }
        return context.getBuilder().get();
    }

    /**
     * Formats the given ANTLR token into a string.
     *
     * @param token the token to format
     * @return the formatted string
     */
    public String format(Token token) {
        StringBuilder buf = new StringBuilder();
        printerParser.format(token, buf);
        return buf.toString();
    }
}
