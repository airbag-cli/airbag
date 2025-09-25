package io.github.airbag.symbol;

import io.github.airbag.gen.ExpressionLexer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterTest {

    private static final Symbol DEFAULT = Symbol.of().get();

    private static final Symbol SYMBOL1 = Symbol.of().index(0).start(0).stop(3).text("var").type(
            ExpressionLexer.ID).channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL).line(1).position(3).get();

    private static final Symbol SYMBOL2 = Symbol.of().index(1).start(4).stop(5).text("=").type(
            ExpressionLexer.T__0).channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL).line(1).position(5).get();

    private static final Symbol SYMBOL3 = Symbol.of().index(2).start(6).stop(8).text("15").type(
            ExpressionLexer.INT).channel(ExpressionLexer.DEFAULT_TOKEN_CHANNEL).line(1).position(8).get();

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
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX).toFormatter();
            assertEquals("1", indexFormatter.format(SYMBOL2));
        }

        @Test
        void integerFormatterStrict() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX, true).toFormatter();
            assertThrows(SymbolException.class, () -> indexFormatter.format(DEFAULT));
            assertEquals("0", indexFormatter.format(SYMBOL1));
        }

        @Test
        void startIntegerFormatter() {
            SymbolFormatter startFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.START).toFormatter();
            assertEquals("6", startFormatter.format(SYMBOL3));
        }

        @Test
        void stopIntegerFormatter() {
            SymbolFormatter stopFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.STOP).toFormatter();
            assertEquals("8", stopFormatter.format(SYMBOL3));
        }

        @Test
        void typeIntegerFormatter() {
            SymbolFormatter typeFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE).toFormatter();
            assertEquals(String.valueOf(ExpressionLexer.ID), typeFormatter.format(SYMBOL1));
        }

        @Test
        void channelIntegerFormatter() {
            SymbolFormatter channelFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.CHANNEL).toFormatter();
            assertEquals(String.valueOf(ExpressionLexer.DEFAULT_TOKEN_CHANNEL), channelFormatter.format(SYMBOL1));
        }

        @Test
        void lineIntegerFormatter() {
            SymbolFormatter lineFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.LINE).toFormatter();
            assertEquals("1", lineFormatter.format(SYMBOL1));
        }

        @Test
        void positionIntegerFormatter() {
            SymbolFormatter positionFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.POSITION).toFormatter();
            assertEquals("8", positionFormatter.format(SYMBOL3));
        }

        @Test
        void emptyLiteralPrinterParser() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("").toFormatter();
            assertEquals("", literalFormatter.format(DEFAULT));
        }

        @Test
        void literalFormatter() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("Hello").toFormatter();
            assertEquals("Hello", literalFormatter.format(DEFAULT));
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
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX).toFormatter();
            assertEquals(-115, indexFormatter.parse("-115").index());
        }

        @Test
        void indexIntegerFormatter() {
            SymbolFormatter indexFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.INDEX).toFormatter();
            assertEquals(1, indexFormatter.parse("1").index());
        }

        @Test
        void startIntegerFormatter() {
            SymbolFormatter startFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.START).toFormatter();
            assertEquals(1, startFormatter.parse("1").start());
        }

        @Test
        void stopIntegerFormatter() {
            SymbolFormatter stopFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.STOP).toFormatter();
            assertEquals(1, stopFormatter.parse("1").stop());
        }

        @Test
        void typeIntegerFormatter() {
            SymbolFormatter typeFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.TYPE).toFormatter();
            assertEquals(1, typeFormatter.parse("1").type());
        }

        @Test
        void channelIntegerFormatter() {
            SymbolFormatter channelFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.CHANNEL).toFormatter();
            assertEquals(1, channelFormatter.parse("1").channel());
        }

        @Test
        void lineIntegerFormatter() {
            SymbolFormatter lineFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.LINE).toFormatter();
            assertEquals(1, lineFormatter.parse("1").line());
        }

        @Test
        void positionIntegerFormatter() {
            SymbolFormatter positionFormatter = new SymbolFormatterBuilder().appendInteger(SymbolField.POSITION).toFormatter();
            assertEquals(1, positionFormatter.parse("1").position());
        }

        @Test
        void emptyLiteralPrinterParser() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("").toFormatter();
            assertTrue(TESTER.test(DEFAULT, literalFormatter.parse("")));
        }

        @Test
        void literalFormatter() {
            SymbolFormatter literalFormatter = new SymbolFormatterBuilder().appendLiteral("Hello").toFormatter();
            assertTrue(TESTER.test(DEFAULT, literalFormatter.parse("Hello")));
        }

    }
}