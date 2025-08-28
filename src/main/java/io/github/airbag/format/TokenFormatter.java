package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class TokenFormatter {

    private final CompositePrinterParser printerParser;

    private final List<TokenField<?>> fields;

    TokenFormatter(CompositePrinterParser printerParser,
                          List<TokenField<?>> fields) {
        this.printerParser = printerParser;
        this.fields = List.copyOf(fields);
    }

    public String format(Token token) {
        TokenFormatContext ctx = new TokenFormatContext(token);
        StringBuilder buf = new StringBuilder();
        if (!printerParser.format(ctx, buf)) {
            throw new TokenException();
        }
        return buf.toString();
    }

    public Token parse(String input) {
        TokenParseContext ctx = new TokenParseContext(new HashMap<>());
        int position = printerParser.parse(ctx, input, 0);
        if (position < 0) {
            throw new TokenParseException();
        }
        return ctx.resolveFields();
    }

    public BiPredicate<Token, Token> equalizer() {
        return Tokens.equalizer(fields);
    }

}
