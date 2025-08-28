package io.github.airbag;

import io.github.airbag.format.TokenFormatter;
import io.github.airbag.format.TokenFormatterBuilder;
import io.github.airbag.format.TokenParseException;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenFormatterTest {

    private static final Token TOKEN = Tokens.singleTokenOf()
            .index(0)
            .startIndex(0)
            .stopIndex(2)
            .text("Hello")
            .type(-213)
            .line(1)
            .charPositionInLine(0)
            .get();

    @Test
    void testIntegerFormatter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.INDEX)
                .toFormatter();
        assertEquals("0", formatter.format(TOKEN));
        assertTrue(formatter.equalizer().test(TOKEN, formatter.parse("0")));
    }

    @Test
    void testNegativeInteger() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertEquals("-213", formatter.format(TOKEN));
        assertTrue(formatter.equalizer().test(TOKEN, formatter.parse("-213")));
    }

    @Test
    void invalidNegative() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertThrows(TokenParseException.class,() -> formatter.parse("-"));
    }

    @Test
    void multipleDashes() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertThrows(TokenParseException.class,() -> formatter.parse("--10"));
    }
}
