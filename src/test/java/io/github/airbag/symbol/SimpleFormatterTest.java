package io.github.airbag.symbol;

import io.github.airbag.gen.ExpressionLexer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test simple formatter SymbolFormatter.SIMPLE")
public class SimpleFormatterTest {

    private SymbolFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = SymbolFormatter.SIMPLE.withVocabulary(ExpressionLexer.VOCABULARY);
    }

    @Test
    void testToString() {
        assertEquals("<EOF> | l[:c] | (S[:c] \\'X\\')", formatter.toString());
    }

    @Nested
    @DisplayName("format()")
    class Format {

        @Test
        @DisplayName("should format symbol with literal name using the primary format")
        void formatSymbolWithLiteralName() {
            Symbol symbol = Symbol.of().type(ExpressionLexer.T__3).text("+").get();
            assertEquals("'+'", formatter.format(symbol));
        }

        @Test
        @DisplayName("should format symbol without literal name using the alternative format")
        void formatSymbolWithoutLiteralName() {
            Symbol symbol = Symbol.of().type(ExpressionLexer.INT).text("123").get();
            assertEquals("(INT '123')", formatter.format(symbol));
        }

        @Test
        @DisplayName("should format symbol with symbolic name using the alternative format")
        void formatSymbolWithSymbolicName() {
            Symbol symbol = Symbol.of().type(ExpressionLexer.ID).text("myVar").get();
            assertEquals("(ID 'myVar')", formatter.format(symbol));
        }

        @Test
        @DisplayName("should escape special characters in text when using alternative format")
        void formatSymbolWithTextToEscape() {
            Symbol symbol = Symbol.of().type(ExpressionLexer.ID).text("can't").get();
            assertEquals("(ID 'can\\'t')", formatter.format(symbol));
        }

        @Test
        @DisplayName("EOF should have special treatment")
        void formatEOF() {
            Symbol token = Symbol.of().type(ExpressionLexer.EOF).text("<EOF>").get();
            assertEquals("EOF", formatter.format(token));
        }

        @Test
        @DisplayName("should format token with non-default channel")
        void formatSymbolWithChannel() {
            // Literal token with channel
            Symbol literalSymbol =Symbol.of().type(ExpressionLexer.T__3).text( "+").channel(2).get();
            assertEquals("'+':2", formatter.format(literalSymbol));

            // Symbolic token with channel
            Symbol symbolicSymbol = Symbol.of().type(ExpressionLexer.ID).text("myVar").channel(3).get();
            assertEquals("(ID:3 'myVar')", formatter.format(symbolicSymbol));
        }
    }

    @Nested
    @DisplayName("parse()")
    class Parse {

        @Test
        @DisplayName("should parse a literal name into a token")
        void parseLiteralName() {
            Symbol expected = Symbol.of().type(ExpressionLexer.T__1).text("*").get();
            Symbol actual = formatter.parse("'*'");
            assertEquals(expected.type(), actual.type());
            assertEquals(expected.text(), actual.text());
        }

        @Test
        @DisplayName("should parse the alternative format into a token")
        void parseAlternativeFormat() {
            Symbol expected = Symbol.of().type(ExpressionLexer.INT).text("456").get();
            Symbol actual = formatter.parse("(INT '456')");
            assertEquals(expected.type(), actual.type());
            assertEquals(expected.text(), actual.text());
        }

        @Test
        @DisplayName("should parse the alternative format with escaped text")
        void parseAlternativeFormatWithEscapedText() {
            Symbol expected = Symbol.of().type(ExpressionLexer.ID).text("an'id").get();
            Symbol actual = formatter.parse("(ID 'an\\'id')");
            assertEquals(expected.type(), actual.type());
            assertEquals(expected.text(), actual.text());
        }

        @Test
        @DisplayName("should throw exception for invalid format")
        void parseInvalidFormat() {
            assertThrows(SymbolParseException.class, () -> formatter.parse("invalid"));
        }

        @Test
        @DisplayName("should throw exception for incomplete alternative format")
        void parseIncompleteAlternativeFormat() {
            assertThrows(SymbolParseException.class, () -> formatter.parse("(ID 'abc'"));
        }

        @Test
        @DisplayName("should throw exception for unknown token type")
        void parseUnknownSymbolType() {
            assertThrows(SymbolParseException.class, () -> formatter.parse("(UNKNOWN 'abc')"));
        }

        @Test
        @DisplayName("EOF should have special treatment")
        void parseEOF() {
            Symbol token = formatter.parse("EOF");
            assertEquals(Symbol.EOF, token.type());
        }

        @Test
        @DisplayName("should parse token with non-default channel")
        void parseSymbolWithChannel() {
            // Literal token with channel
            Symbol actualLiteral = formatter.parse("'+':2");
            assertEquals(ExpressionLexer.T__3, actualLiteral.type());
            assertEquals("+", actualLiteral.text());
            assertEquals(2, actualLiteral.channel());

            // Symbolic token with channel
            Symbol actualSymbolic = formatter.parse("(ID:3 'myVar')");
            assertEquals(ExpressionLexer.ID, actualSymbolic.type());
            assertEquals("myVar", actualSymbolic.text());
            assertEquals(3, actualSymbolic.channel());
        }
    }
}