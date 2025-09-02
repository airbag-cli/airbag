package io.github.airbag;

import io.github.airbag.format.TokenFormatter;
import io.github.airbag.format.TokenParseException;
import io.github.airbag.gen.ExpressionLexer;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TokenFormatter.SIMPLE")
public class SimpleFormatterTest {

    private TokenFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = TokenFormatter.SIMPLE.withVocabulary(ExpressionLexer.VOCABULARY);
    }

    @Nested
    @DisplayName("format()")
    class Format {

        @Test
        @DisplayName("should format token with literal name using the primary format")
        void formatTokenWithLiteralName() {
            Token token = new CommonToken(ExpressionLexer.T__3, "+");
            assertEquals("'+'", formatter.format(token));
        }

        @Test
        @DisplayName("should format token without literal name using the alternative format")
        void formatTokenWithoutLiteralName() {
            Token token = new CommonToken(ExpressionLexer.INT, "123");
            assertEquals("(INT '123')", formatter.format(token));
        }

        @Test
        @DisplayName("should format token with symbolic name using the alternative format")
        void formatTokenWithSymbolicName() {
            Token token = new CommonToken(ExpressionLexer.ID, "myVar");
            assertEquals("(ID 'myVar')", formatter.format(token));
        }

        @Test
        @DisplayName("should escape special characters in text when using alternative format")
        void formatTokenWithTextToEscape() {
            Token token = new CommonToken(ExpressionLexer.ID, "can't");
            assertEquals("(ID 'can\\'t')", formatter.format(token));
        }

        @Test
        @DisplayName("EOF should have special treatment")
        void formatEOF() {
            Token token = new CommonToken(ExpressionLexer.EOF, "<EOF>");
            assertEquals("EOF", formatter.format(token));
        }
    }

    @Nested
    @DisplayName("parse()")
    class Parse {

        @Test
        @DisplayName("should parse a literal name into a token")
        void parseLiteralName() {
            Token expected = new CommonToken(ExpressionLexer.T__1, "*");
            Token actual = formatter.parse("'*'");
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getText(), actual.getText());
        }

        @Test
        @DisplayName("should parse the alternative format into a token")
        void parseAlternativeFormat() {
            Token expected = new CommonToken(ExpressionLexer.INT, "456");
            Token actual = formatter.parse("(INT '456')");
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getText(), actual.getText());
        }

        @Test
        @DisplayName("should parse the alternative format with escaped text")
        void parseAlternativeFormatWithEscapedText() {
            Token expected = new CommonToken(ExpressionLexer.ID, "an'id");
            Token actual = formatter.parse("(ID 'an\\'id')");
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getText(), actual.getText());
        }

        @Test
        @DisplayName("should throw exception for invalid format")
        void parseInvalidFormat() {
            assertThrows(TokenParseException.class, () -> formatter.parse("invalid"));
        }

        @Test
        @DisplayName("should throw exception for incomplete alternative format")
        void parseIncompleteAlternativeFormat() {
            assertThrows(TokenParseException.class, () -> formatter.parse("(ID 'abc'"));
        }

        @Test
        @DisplayName("should throw exception for unknown token type")
        void parseUnknownTokenType() {
            assertThrows(TokenParseException.class, () -> formatter.parse("(UNKNOWN 'abc')"));
        }

        @Test
        @DisplayName("EOF should have special treatment")
        void parseEOF() {
            Token token = formatter.parse("EOF");
            assertEquals(Token.EOF, token.getType());
        }
    }
}
