package io.github.airbag;

import io.github.airbag.format.TokenFormatter;
import io.github.airbag.format.TokenFormatterBuilder;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenFormatterTest {

    @Test
    void testFormat() {
        var formatter = TokenFormatter.ANTLR;
        Token token = Tokens.singleTokenOf()
                .type(15)
                .index(5)
                .startIndex(2)
                .stopIndex(4)
                .text("My text")
                .line(3)
                .charPositionInLine(10)
                .get();
        assertEquals("[@5,2:4='My text',<15>,3:10]", formatter.format(token));
    }

    @Test
    void testParse() {
        var formatter = TokenFormatter.ANTLR;
        Token t1 = Tokens.singleTokenOf()
                .type(15)
                .index(5)
                .startIndex(2)
                .stopIndex(4)
                .text("My text")
                .line(3)
                .charPositionInLine(10)
                .get();

        Token t2 = formatter.parse("[@5,2:4='My text',<15>,3:10]");
        assertTrue(Tokens.isStrongEqual(t1, t2));
    }

    @Test
    void testSimpleFormat() {
        var formatter = new TokenFormatterBuilder().appendLiteral('(').appendType().appendLiteral(" '").appendText().appendLiteral("')").build();
        Token t1 = Tokens.singleTokenOf()
                .type(15)
                .index(5)
                .startIndex(2)
                .stopIndex(4)
                .text("My text")
                .line(3)
                .charPositionInLine(10)
                .get();
        assertEquals("(15 'My text')", formatter.format(t1));
    }

    @Test
    void testSimpleParse() {
        var formatter = new TokenFormatterBuilder().appendLiteral('(').appendType().appendLiteral(" '").appendText().appendLiteral("')").build();
        Token t1 = Tokens.singleTokenOf()
                .type(15)
                .index(-1)
                .startIndex(2)
                .stopIndex(4)
                .text("My text")
                .line(3)
                .charPositionInLine(10)
                .get();
        assertTrue(Tokens.isWeakEqual(t1, formatter.parse("(15 'My text')")), formatter.parse("(15 'My text')").toString());
    }
}
