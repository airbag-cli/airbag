package io.github.airbag;

import io.github.airbag.format.TokenException;
import io.github.airbag.format.TokenFormatter;
import io.github.airbag.format.TokenFormatterBuilder;
import io.github.airbag.format.TokenParseException;
import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
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

    private static final Vocabulary VOCABULARY = new Vocabulary() {


        @Override
        public int getMaxTokenType() {
            return 4;
        }

        @Override
        public String getLiteralName(int tokenType) {
            if (tokenType == 3) {
                return "'='";
            } else if (tokenType == 4){
                return "'=='";
            }
            return null;
        }

        @Override
        public String getSymbolicName(int tokenType) {
            if (tokenType == 1) {
                return "ID";
            } else if (tokenType == 2) {
                return "IDENTIFIER";
            }
            return null;
        }

        @Override
        public String getDisplayName(int tokenType) {
            return "";
        }
    };

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
        assertThrows(TokenParseException.class, () -> formatter.parse("-"));
    }

    @Test
    void multipleDashes() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendInteger(TokenField.TYPE)
                .toFormatter();
        assertThrows(TokenParseException.class, () -> formatter.parse("--10"));
    }

    @Test
    void testCharacterParserPrinter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(":")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(")")
                .toFormatter();

        assertEquals("(0:-213)", formatter.format(TOKEN));
        Token parsed = formatter.parse("(0:-213)");
        assertTrue(formatter.equalizer().test(TOKEN, parsed));
    }

    @Test
    void testInvalidLiteral() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(":")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(")")
                .toFormatter();
        assertThrows(TokenParseException.class, () -> formatter.parse("(0;-10)"));
    }

    @Test
    void testTextParserWithDelimiter() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendInteger(TokenField.TYPE)
                .appendLiteral(":")
                .appendText()
                .appendLiteral(":")
                .appendInteger(TokenField.INDEX)
                .appendLiteral(")")
                .toFormatter();
        assertEquals("(-213:Hello:0)", formatter.format(TOKEN));
        var equalizer = formatter.equalizer();
        assertTrue(equalizer.test(TOKEN, formatter.parse("(-213:Hello:0)")));
    }

    @Test
    void testTextParserWithoutDelimiter() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendInteger(TokenField.TYPE)
                .appendLiteral(":")
                .appendText().toFormatter();
        assertEquals("-213:Hello", formatter.format(TOKEN));
        var equalizer = formatter.equalizer();
        assertTrue(equalizer.test(TOKEN, formatter.parse("-213:Hello")));
    }

    @Test
    void testSymbolicTypeParser() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteral("(")
                .appendSymbolicType()
                .appendLiteral(":")
                .appendText()
                .appendLiteral(")")
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("number").get();
        assertEquals("(ID:number)", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("(ID:number)"));
        assertEquals(ExpressionLexer.ID, parsed.getType());
        assertEquals("number", parsed.getText());
    }

    @Test
    void testOnlySymbolic() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("number").get();
        assertEquals("ID", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("ID"));
        assertEquals(ExpressionLexer.ID, parsed.getType());
    }

    @Test
    void testSymbolicTypeParserNoVocabulary() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .toFormatter();
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because there's no vocabulary to find the symbolic name
        assertThrows(TokenException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(TokenParseException.class, () -> formatter.parse("ID"));
    }

    @Test
    void testSymbolicTypeParserForLiteral() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendSymbolicType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // EQ corresponds to the '=' literal, which has a literal name but no symbolic name
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because '=' has no symbolic name
        assertThrows(TokenException.class, () -> formatter.format(token));
    }

    @Test
    void testLiteralTypeParser() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).get();
        assertEquals("'='", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("'='"));
        assertEquals(ExpressionLexer.T__0, parsed.getType());
    }

    @Test
    void testLiteralTypeParserNoVocabulary() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter();
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.T__0).text("=").get();

        // Formatting should fail because there's no vocabulary to find the literal name
        assertThrows(TokenException.class, () -> formatter.format(token));

        // Parsing should fail for the same reason
        assertThrows(TokenParseException.class, () -> formatter.parse("="));
    }

    @Test
    void testLiteralTypeParserForSymbolic() {
        TokenFormatter formatter = new TokenFormatterBuilder()
                .appendLiteralType()
                .toFormatter()
                .withVocabulary(ExpressionLexer.VOCABULARY);

        // ID has a symbolic name but no literal name
        Token token = Tokens.singleTokenOf().type(ExpressionLexer.ID).text("id").get();

        // Formatting should fail because 'ID' has no literal name
        assertThrows(TokenException.class, () -> formatter.format(token));
    }

    @Test
    void testTakeLongestSymbolicMatch() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token parsed = formatter.parse("IDENTIFIER");
        assertEquals(2, parsed.getType());
    }

        @Test
    void testTakeLongestLiteralMatch() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendLiteralType()
                .toFormatter()
                .withVocabulary(VOCABULARY);
        Token parsed = formatter.parse("'=='");
        assertEquals(4, parsed.getType());
    }

    @Test
    void testSymbolicPrinterForEOF() {
        TokenFormatter formatter = new TokenFormatterBuilder().appendSymbolicType()
                .toFormatter()
                .withVocabulary(
                        ExpressionLexer.VOCABULARY);
        Token token = Tokens.singleTokenOf().type(Token.EOF).get();
        assertEquals("EOF", formatter.format(token));
        Token parsed = assertDoesNotThrow(() -> formatter.parse("EOF"));
        assertEquals(Token.EOF, parsed.getType());
    }

}