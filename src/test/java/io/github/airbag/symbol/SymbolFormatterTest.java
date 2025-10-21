package io.github.airbag.symbol;

import io.github.airbag.gen.ExpressionLexer;
import org.antlr.v4.runtime.Vocabulary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterTest {

    private static final Symbol DEFAULT = Symbol.of().get();

    private static final Symbol SYMBOL1 = Symbol.of()
            .index(0)
            .start(0)
            .stop(3)
            .text("var")
            .type(ExpressionLexer.ID)
            .channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL)
            .line(1)
            .position(3)
            .get();

    private static final Symbol SYMBOL2 = Symbol.of()
            .index(1)
            .start(4)
            .stop(5)
            .text("=")
            .type(ExpressionLexer.T__0)
            .channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL)
            .line(1)
            .position(5)
            .get();

    private static final Symbol SYMBOL3 = Symbol.of()
            .index(2)
            .start(6)
            .stop(8)
            .text("15")
            .type(ExpressionLexer.INT)
            .channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL)
            .line(1)
            .position(8)
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
            } else if (tokenType == 4) {
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

    private static final BiPredicate<Symbol, Symbol> TESTER = SymbolField.equalizer(SymbolField.all());

    @Nested
    class Format {

        @Test
        void emptyFormatter() {
            SymbolFormatter empty = new SymbolFormatterBuilder().toFormatter();
            assertEquals("", empty.format(SYMBOL1));
        }

        @Test
        void indexIntegerFormatter() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX)
                    .toFormatter();
            assertEquals("1", indexFormatter.format(SYMBOL2));
        }

        @Test
        void integerFormatterStrict() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX,
                    true).toFormatter();
            assertThrows(SymbolException.class, () -> indexFormatter.format(DEFAULT));
            assertEquals("0", indexFormatter.format(SYMBOL1));
        }

        @Test
        void startIntegerFormatter() {
            SymbolFormatter startFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.START)
                    .toFormatter();
            assertEquals("6", startFormatter.format(SYMBOL3));
        }

        @Test
        void stopIntegerFormatter() {
            SymbolFormatter stopFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.STOP)
                    .toFormatter();
            assertEquals("8", stopFormatter.format(SYMBOL3));
        }

        @Test
        void typeIntegerFormatter() {
            SymbolFormatter typeFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                    .toFormatter();
            assertEquals(String.valueOf(ExpressionLexer.ID), typeFormatter.format(SYMBOL1));
        }

        @Test
        void channelIntegerFormatter() {
            SymbolFormatter channelFormatter = new SymbolFormatterBuilder().appendInteger(
                    SymbolField.CHANNEL).toFormatter();
            assertEquals(String.valueOf(ExpressionLexer.DEFAULT_TOKEN_CHANNEL),
                    channelFormatter.format(SYMBOL1));
        }

        @Test
        void lineIntegerFormatter() {
            SymbolFormatter lineFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.LINE)
                    .toFormatter();
            assertEquals("1", lineFormatter.format(SYMBOL1));
        }

        @Test
        void positionIntegerFormatter() {
            SymbolFormatter positionFormatter = new SymbolFormatterBuilder().appendInteger(
                    SymbolField.POSITION).toFormatter();
            assertEquals("8", positionFormatter.format(SYMBOL3));
        }

        @Test
        void emptyLiteralPrinterParser() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("")
                    .toFormatter();
            assertEquals("", literalFormatter.format(DEFAULT));
        }

        @Test
        void literalFormatter() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("Hello")
                    .toFormatter();
            assertEquals("Hello", literalFormatter.format(DEFAULT));
        }

        @Test
        void textFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("'")
                    .appendText()
                    .appendLiteral("'")
                    .toFormatter();
            assertEquals("'var'", formatter.format(SYMBOL1));
        }

        @Test
        void escapedText() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("'")
                    .appendText(TextOption.ESCAPED)
                    .appendLiteral("'")
                    .toFormatter();
            assertEquals("'\\\\new comment with \\n newline'",
                    formatter.format(Symbol.of().text("\\new comment with \n newline").get()));
        }

        @Test
        void symbolicFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertEquals("ID", formatter.format(SYMBOL1));
        }

        @Test
        void symbolicFormatterFail() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                    .toFormatter();
            assertThrows(SymbolException.class, () -> formatter.format(SYMBOL1));
        }

        @Test
        void literalAndSymbolicFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertEquals("type: ID", formatter.format(SYMBOL1));
        }

        @Test
        void literalAndSymbolicFormatterFail() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter();
            assertThrows(SymbolException.class, () -> formatter.format(SYMBOL1));
        }

        @Test
        void literalTypeFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteralType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertEquals("'='", formatter.format(SYMBOL2));
        }

        @Test
        void literalTypeFormatterNoVocabulary() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteralType()
                    .toFormatter();
            assertThrows(SymbolException.class, () -> formatter.format(SYMBOL2));
        }

        @Test
        void literalTypeFormatterForSymbolic() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteralType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertThrows(SymbolException.class, () -> formatter.format(SYMBOL1));
        }

        @Test
        void symbolicFormatterForLiteral() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertThrows(SymbolException.class, () -> formatter.format(SYMBOL2));
        }

        @Test
        void optionalPresent() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendSymbolicType()
                    .startOptional()
                    .appendLiteral(":")
                    .appendInteger(SymbolField.CHANNEL, true)
                    .endOptional()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol tokenWithChannel = Symbol.of().type(1).channel(5).get();
            assertEquals("ID:5", formatter.format(tokenWithChannel));
        }

        @Test
        void optionalAbsent() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendSymbolicType()
                    .startOptional()
                    .appendLiteral(":")
                    .appendInteger(SymbolField.CHANNEL, true)
                    .endOptional()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol tokenWithoutChannel = Symbol.of().type(1).channel(0).get(); // default channel
            assertEquals("ID", formatter.format(tokenWithoutChannel));
        }

        @Test
        void appendPattern() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendPattern("N,B:E,C,P,R")
                    .toFormatter();
            Symbol token = Symbol.of()
                    .index(1).start(2).stop(3)
                    .channel(4).position(5).line(6)
                    .get();
            assertEquals("1,2:3,4,5,6", formatter.format(token));
        }

        @Test
        void appendTypeSymbolicFirst() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendType(TypeFormat.SYMBOLIC_FIRST)
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol symbolic = Symbol.of().type(1).get();
            Symbol literal = Symbol.of().type(3).get();
            assertEquals("ID", formatter.format(symbolic));
            assertEquals("'='", formatter.format(literal));
        }

        @Test
        void appendEOF() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendEOF().toFormatter();
            Symbol eofSymbol = Symbol.of().type(Symbol.EOF).text("<EOF>").get();
            assertEquals("EOF", formatter.format(eofSymbol));
            Symbol notEofSymbol = Symbol.of().type(1).get();
            assertThrows(SymbolException.class, () -> formatter.format(notEofSymbol));
        }
    }

    @Nested
    class Parse {

        @Test
        void emptyFormatter() {
            SymbolFormatter empty = new SymbolFormatterBuilder().toFormatter();
            assertTrue(TESTER.test(DEFAULT, empty.parse("")));
        }

        @Test
        void negativeInteger() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX)
                    .toFormatter();
            assertEquals(-115, indexFormatter.parse("-115").index());
        }

        @Test
        void indexIntegerFormatter() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX)
                    .toFormatter();
            assertEquals(1, indexFormatter.parse("1").index());
        }

        @Test
        void startIntegerFormatter() {
            SymbolFormatter startFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.START)
                    .toFormatter();
            assertEquals(1, startFormatter.parse("1").start());
        }

        @Test
        void stopIntegerFormatter() {
            SymbolFormatter stopFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.STOP)
                    .toFormatter();
            assertEquals(1, stopFormatter.parse("1").stop());
        }

        @Test
        void typeIntegerFormatter() {
            SymbolFormatter typeFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                    .toFormatter();
            assertEquals(1, typeFormatter.parse("1").type());
        }

        @Test
        void channelIntegerFormatter() {
            SymbolFormatter channelFormatter = new SymbolFormatterBuilder().appendInteger(
                    SymbolField.CHANNEL).toFormatter();
            assertEquals(1, channelFormatter.parse("1").channel());
        }

        @Test
        void lineIntegerFormatter() {
            SymbolFormatter lineFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.LINE)
                    .toFormatter();
            assertEquals(1, lineFormatter.parse("1").line());
        }

        @Test
        void positionIntegerFormatter() {
            SymbolFormatter positionFormatter = new SymbolFormatterBuilder().appendInteger(
                    SymbolField.POSITION).toFormatter();
            assertEquals(1, positionFormatter.parse("1").position());
        }

        @Test
        void emptyLiteralPrinterParser() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("")
                    .toFormatter();
            assertTrue(TESTER.test(DEFAULT, literalFormatter.parse("")));
        }

        @Test
        void literalFormatter() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("Hello")
                    .toFormatter();
            assertTrue(TESTER.test(DEFAULT, literalFormatter.parse("Hello")));
        }

        @Test
        void textFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("'")
                    .appendText()
                    .appendLiteral("'")
                    .toFormatter();
            Symbol symbol = formatter.parse("'var'");
            assertEquals("var", symbol.text());
        }

        @Test
        void escapedText() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteral("'")
                    .appendText(TextOption.ESCAPED)
                    .appendLiteral("'")
                    .toFormatter();
            Symbol symbol = formatter.parse("'\\new comment with \\n newline'");
            assertEquals("\new comment with \n newline", symbol.text());
        }

        @Test
        void symbolicFormatter() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            Symbol symbol = formatter.parse("ID");
            assertEquals(ExpressionLexer.ID, symbol.type());
        }

        @Test
        void symbolicFormatterException() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                    .toFormatter();
            assertThrows(SymbolParseException.class, () -> formatter.parse("ID"));
        }

        @Test
        void literalAndSymbolicParser() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            Symbol symbol = formatter.parse("type: ID");
            assertEquals(ExpressionLexer.ID, symbol.type());
        }

        @Test
        void literalAndSymbolicParserNoVocabulary() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter();
            assertThrows(SymbolParseException.class, () -> formatter.parse("type: ID"));
        }

        @Test
        void literalAndSymbolicParserMismatch() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertThrows(SymbolParseException.class, () -> formatter.parse("typo: ID"));
        }

        @Test
        void literalAndSymbolicParserInvalidSymbol() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteral("type: ")
                    .appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            assertThrows(SymbolParseException.class, () -> formatter.parse("type: INVALID"));
        }

        @Test
        void invalidNegative() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                    .toFormatter();
            assertThrows(SymbolParseException.class, () -> formatter.parse("-"));
        }

        @Test
        void multipleDashes() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE)
                    .toFormatter();
            assertThrows(SymbolParseException.class, () -> formatter.parse("--10"));
        }

        @Test
        void literalTypeParser() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteralType()
                    .toFormatter()
                    .withVocabulary(ExpressionLexer.VOCABULARY);
            Symbol parsed = formatter.parse("'='");
            assertEquals(ExpressionLexer.T__0, parsed.type());
        }

        @Test
        void literalTypeParserNoVocabulary() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendLiteralType()
                    .toFormatter();
            assertThrows(SymbolParseException.class, () -> formatter.parse("'='"));
        }

        @Test
        void takeLongestSymbolicMatch() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendSymbolicType()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol parsed = formatter.parse("IDENTIFIER");
            assertEquals(2, parsed.type());
        }

        @Test
        void takeLongestLiteralMatch() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendLiteralType()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol parsed = formatter.parse("'=='");
            assertEquals(4, parsed.type());
        }

        @Test
        void optionalPresent() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendSymbolicType()
                    .startOptional()
                    .appendLiteral(":")
                    .appendInteger(SymbolField.CHANNEL, true)
                    .endOptional()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol parsed = formatter.parse("ID:5");
            assertEquals(1, parsed.type());
            assertEquals(5, parsed.channel());
        }

        @Test
        void optionalAbsent() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendSymbolicType()
                    .startOptional()
                    .appendLiteral(":")
                    .appendInteger(SymbolField.CHANNEL, true)
                    .endOptional()
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            Symbol parsed = formatter.parse("ID");
            assertEquals(1, parsed.type());
            assertEquals(0, parsed.channel());
        }

        @Test
        void appendPattern() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendPattern("N,B:E,C,P,R")
                    .toFormatter();
            Symbol parsed = formatter.parse("1,2:3,4,5,6");
            assertEquals(1, parsed.index());
            assertEquals(2, parsed.start());
            assertEquals(3, parsed.stop());
            assertEquals(4, parsed.channel());
            assertEquals(5, parsed.position());
            assertEquals(6, parsed.line());
        }

        @Test
        void appendTypeSymbolicFirst() {
            SymbolFormatter formatter = new SymbolFormatterBuilder()
                    .appendType(TypeFormat.SYMBOLIC_FIRST)
                    .toFormatter()
                    .withVocabulary(VOCABULARY);
            assertEquals(1, formatter.parse("ID").type());
            assertEquals(3, formatter.parse("'='").type());
        }

        @Test
        void appendEOF() {
            SymbolFormatter formatter = new SymbolFormatterBuilder().appendEOF().toFormatter();
            Symbol parsed = formatter.parse("EOF");
            assertEquals(Symbol.EOF, parsed.type());
            assertEquals("<EOF>", parsed.text());
        }

        @Test
        void parseWithAlternativesShouldReturnFurthestError() {
            SymbolFormatter partialMatch = SymbolFormatter.ofPattern("\\'a\\' \\'b\\'");
            SymbolFormatter noMatch = SymbolFormatter.ofPattern("'x' 'y'");
            SymbolFormatter formatter = partialMatch.withAlternative(noMatch);
            java.text.ParsePosition position = new java.text.ParsePosition(0);
            assertNull(formatter.parse("'a' 'c'", position));
            assertEquals(5, position.getErrorIndex());
        }

        @Test
        void conflictingFieldThrowsException() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("X[l]")
                    .withVocabulary(VOCABULARY);
            String conflictingString = "assign'='";
            assertThrows(SymbolParseException.class, () -> formatter.parse(conflictingString));
        }
    }
}