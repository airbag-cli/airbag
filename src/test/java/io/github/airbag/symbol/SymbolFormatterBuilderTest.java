package io.github.airbag.symbol;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterBuilderTest {

    private static final Symbol SYMBOL = Symbol.of().index(0)
            .start(-1)
            .stop(2)
            .text("test")
            .type(3)
            .channel(-4)
            .line(5)
            .position(6)
            .get();

    private static final SymbolFormatContext FORMAT_CTX = new SymbolFormatContext(SYMBOL, null);

    private static final String INPUT = "  <-10>   'My text is \\' quoted' --10\t\r\n  ";

    @Nested
    class IntegerPrinterParserTest {

        private static SymbolFormatterBuilder.CompositePrinterParser bundleUp(SymbolFormatterBuilder.IntegerPrinterParser integerPrinterParser) {
            return new SymbolFormatterBuilder.CompositePrinterParser(List.of(integerPrinterParser),
                    false);
        }

        @Test
        void testIntegerPrinterPositiveNumber() {
            var integerPrinter = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.INDEX,
                    false);
            StringBuilder buf = new StringBuilder();
            boolean b = assertDoesNotThrow(() -> integerPrinter.format(FORMAT_CTX, buf));
            assertTrue(b);
            assertEquals("0", buf.toString());
        }

        @Test
        void testIntegerPrinterNegativeNumber() {
            var integerPrinter = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.CHANNEL,
                    false);
            StringBuilder buf = new StringBuilder();
            boolean b = assertDoesNotThrow(() -> integerPrinter.format(FORMAT_CTX, buf));
            assertTrue(b);
            assertEquals("-4", buf.toString());
        }

        @Test
        void testPrintDefaultValue() {
            var strictPrinter = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,
                    true);
            var lenientPrinter = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,
                    false);
            StringBuilder buf = new StringBuilder();
            boolean b = assertDoesNotThrow(() -> strictPrinter.format(FORMAT_CTX, buf));
            assertFalse(b);
            assertTrue(buf.isEmpty());
            b = assertDoesNotThrow(() -> lenientPrinter.format(FORMAT_CTX, buf));
            assertTrue(b);
            assertEquals("-1", buf.toString());
        }

        @Test
        void testPeekWithInvalidPosition() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertThrows(IndexOutOfBoundsException.class,
                    () -> integerPrinterParser.peek(ctx, INPUT, 100));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> integerPrinterParser.peek(ctx, INPUT, -10));
        }

        @Test
        void testPeekWithInvalidNumber() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(~1, integerPrinterParser.peek(ctx, " --10", 1));
            assertEquals(~0, integerPrinterParser.peek(ctx, " --10", 0));
        }

        @Test
        void testParseWithInvalidNumber() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(~1, integerPrinterParser.parse(ctx, " --10", 1));
            assertEquals("Expected an integer for field 'type' but found '--1'",
                    ctx.getErrorMessage());
        }

        @Test
        void testParseWithNegativeNumber() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " --10", 2));
            Symbol symbol = ctx.resolveFields();
            assertEquals(-10, symbol.type());
        }

        @Test
        void testParseWithPositiveNumber() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " 1050 -1000", 1));
            Symbol symbol = ctx.resolveFields();
            assertEquals(1050, symbol.type());
        }

        @Test
        void testParseWithLeadingZeros() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " 0050 -1000", 1));
            Symbol symbol = ctx.resolveFields();
            assertEquals(50, symbol.type());
        }

        @Test
        void testParseIntegerOverflow() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            String overflowValue = " " + "2147483648"; // Integer.MAX_VALUE + 1
            assertEquals(~1, integerPrinterParser.parse(ctx, overflowValue, 1));
            assertTrue(ctx.getErrorMessage().contains("is out of range for field 'type'"));
        }

        @Test
        void testParseIntegerUnderflow() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            String underflowValue = " " + "-2147483649"; // Integer.MIN_VALUE - 1
            assertEquals(~1, integerPrinterParser.parse(ctx, underflowValue, 1));
            assertTrue(ctx.getErrorMessage().contains("is out of range for field 'type'"));
        }

        @Test
        void testParseZero() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(2, integerPrinterParser.parse(ctx, " 0 ", 1));
            Symbol symbol = ctx.resolveFields();
            assertEquals(0, symbol.type());
        }

        @Test
        void testParseWithNonNumeric() {
            var integerPrinterParser = new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE);
            var ctx = new SymbolParseContext(null, null);
            assertEquals(~1, integerPrinterParser.parse(ctx, " abc", 1));
            assertEquals("Expected an integer for field 'type' but found 'abc'",
                    ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertAll(
                    () -> assertEquals("i", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE,true).toString()),
                    () -> assertEquals("I", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE,false).toString()),
                    () -> assertEquals("n", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.INDEX,true).toString()),
                    () -> assertEquals("N", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.INDEX,false).toString()),
                    () -> assertEquals("r", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.LINE,true).toString()),
                    () -> assertEquals("R", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.LINE,false).toString()),
                    () -> assertEquals("p", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.POSITION,true).toString()),
                    () -> assertEquals("P", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.POSITION,false).toString()),
                    () -> assertEquals("c", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.CHANNEL,true).toString()),
                    () -> assertEquals("C", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.CHANNEL,false).toString()),
                    () -> assertEquals("b", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,true).toString()),
                    () -> assertEquals("B", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,false).toString()),
                    () -> assertEquals("e", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.STOP,true).toString()),
                    () -> assertEquals("E", new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.STOP,false).toString())
            );
        }

    }
}