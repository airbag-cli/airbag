package io.github.airbag.symbol;

import io.github.airbag.symbol.SymbolFormatterBuilder.SymbolPrinterParser;
import io.github.airbag.symbol.SymbolFormatterBuilder.TextPrinterParser;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterBuilderTest {

    private static final Symbol SYMBOL = Symbol.of()
            .index(0)
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
            assertAll(() -> assertEquals("i",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE,
                                    true).toString()),
                    () -> assertEquals("I",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.TYPE,
                                    false).toString()),
                    () -> assertEquals("n",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.INDEX,
                                    true).toString()),
                    () -> assertEquals("N",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.INDEX,
                                    false).toString()),
                    () -> assertEquals("r",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.LINE,
                                    true).toString()),
                    () -> assertEquals("R",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.LINE,
                                    false).toString()),
                    () -> assertEquals("p",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.POSITION,
                                    true).toString()),
                    () -> assertEquals("P",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.POSITION,
                                    false).toString()),
                    () -> assertEquals("c",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.CHANNEL,
                                    true).toString()),
                    () -> assertEquals("C",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.CHANNEL,
                                    false).toString()),
                    () -> assertEquals("b",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,
                                    true).toString()),
                    () -> assertEquals("B",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.START,
                                    false).toString()),
                    () -> assertEquals("e",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.STOP,
                                    true).toString()),
                    () -> assertEquals("E",
                            new SymbolFormatterBuilder.IntegerPrinterParser(SymbolField.STOP,
                                    false).toString()));
        }

    }

    @Nested
    class LiteralPrinterParserTest {

        private static final SymbolFormatContext FORMAT_CTX = new SymbolFormatContext(null, null);
        private static final SymbolParseContext PARSE_CTX = new SymbolParseContext(null, null);

        @Test
        void testFormat() {
            var printer = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            var buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX, buf));
            assertEquals("=>", buf.toString());
        }

        @Test
        void testFormatEmpty() {
            var printer = new SymbolFormatterBuilder.LiteralPrinterParser("");
            var buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(2, parser.peek(PARSE_CTX, "=> 123", 0));
            assertEquals(5, parser.peek(PARSE_CTX, "abc=>123", 3));
        }

        @Test
        void testPeekFailure() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(~0, parser.peek(PARSE_CTX, "-> 123", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(~0, parser.peek(PARSE_CTX, "=", 0));
            assertEquals(~1, parser.peek(PARSE_CTX, "a=", 1));
        }

        @Test
        void testPeekAtEnd() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(2, parser.peek(PARSE_CTX, "=>", 0));
        }

        @Test
        void testPeekEmpty() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("");
            assertEquals(0, parser.peek(PARSE_CTX, "abc", 0));
            assertEquals(3, parser.peek(PARSE_CTX, "abc", 3));
        }

        @Test
        void testPeekInvalidPosition() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            assertThrows(IndexOutOfBoundsException.class, () -> parser.peek(PARSE_CTX, "=>", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.peek(PARSE_CTX, "=>", 3));
        }

        @Test
        void testParseSuccess() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            var ctx = new SymbolParseContext(null, null);
            assertEquals(2, parser.parse(ctx, "=>123", 0));
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("=>");
            var ctx = new SymbolParseContext(null, null);
            assertEquals(~0, parser.parse(ctx, "->123", 0));
            assertEquals("Expected literal '=>' but found '->'", ctx.getErrorMessage());
        }

        @Test
        void testParseFailureWithSpecialChars() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("a\nb");
            var ctx = new SymbolParseContext(null, null);
            assertEquals(~0, parser.parse(ctx, "acb", 0));
            assertEquals("Expected literal 'a\\nb' but found 'acb'", ctx.getErrorMessage());
        }

        @Test
        void testParseEmpty() {
            var parser = new SymbolFormatterBuilder.LiteralPrinterParser("");
            var ctx = new SymbolParseContext(null, null);
            assertEquals(0, parser.parse(ctx, "abc", 0));
            assertNull(ctx.getErrorMessage());
            assertEquals(3, parser.parse(ctx, "abc", 3));
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertAll(() -> assertEquals("'abc'",
                            new SymbolFormatterBuilder.LiteralPrinterParser("abc").toString()),
                    () -> assertEquals("",
                            new SymbolFormatterBuilder.LiteralPrinterParser("").toString()),
                    () -> assertEquals("'a[b'",
                            new SymbolFormatterBuilder.LiteralPrinterParser("a[b").toString()),
                    () -> assertEquals("'a[b]c'",
                            new SymbolFormatterBuilder.LiteralPrinterParser("a[b]c").toString()),
                    () -> assertEquals("a\\'\\[\\b\\]",
                            new SymbolFormatterBuilder.LiteralPrinterParser("a'[b]").toString()),
                    () -> assertEquals("\\s",
                            new SymbolFormatterBuilder.LiteralPrinterParser("s").toString()),
                    () -> assertEquals("'[]'",
                            new SymbolFormatterBuilder.LiteralPrinterParser("[]").toString()),
                    () -> assertEquals("\\[",
                            new SymbolFormatterBuilder.LiteralPrinterParser("[").toString()));
        }
    }

    @Nested
    class TextPrinterParserTest {

        // FORMAT_CTX with a Symbol that has some text
        private static final Symbol SYMBOL_WITH_TEXT = Symbol.of().text("hello world").get();
        private static final SymbolFormatContext FORMAT_CTX_TEXT = new SymbolFormatContext(
                SYMBOL_WITH_TEXT,
                null);

        private static final Symbol SYMBOL_EMPTY_TEXT = Symbol.of().text("").get();
        private static final SymbolFormatContext FORMAT_CTX_EMPTY = new SymbolFormatContext(
                SYMBOL_EMPTY_TEXT,
                null);

        // This is a test helper that creates a TextPrinterParser and wraps it in a CompositePrinterParser
        // so it can be used in SymbolParseContext for peek/parse tests where successors are needed.
        private SymbolParseContext createTextParseContext(TextPrinterParser textParser,
                                                          SymbolPrinterParser... successors) {
            List<SymbolPrinterParser> chain = new ArrayList<>();
            chain.add(textParser); // The parser being tested
            Collections.addAll(chain, successors); // Successors in the chain
            SymbolFormatterBuilder.CompositePrinterParser rootParser = new SymbolFormatterBuilder.CompositePrinterParser(
                    chain,
                    false);
            return new SymbolParseContext(rootParser, null);
        }

        // --- format() tests ---
        @Test
        void testFormatNothingOption() {
            var printer = new TextPrinterParser(TextOption.NOTHING);
            StringBuilder buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX_TEXT, buf));
            assertEquals("hello world", buf.toString());
        }

        @Test
        void testFormatNothingOptionEmptyText() {
            var printer = new TextPrinterParser(TextOption.NOTHING);
            StringBuilder buf = new StringBuilder();
            //Formatting nothing fails with TextOption.NOTHING
            assertFalse(printer.format(FORMAT_CTX_EMPTY, buf));
            assertTrue(buf.isEmpty()); // Assuming getDefaultValue is ""
        }

        @Test
        void testFormatEscapedOption() {
            // Assuming TextOption.ESCAPED escapes \n, \t, \, '
            var printer = new TextPrinterParser(TextOption.ESCAPED);
            StringBuilder buf = new StringBuilder();
            Symbol symbol = Symbol.of().text("hello\nworld\t'\\").get(); // Text with special chars
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, null);

            assertTrue(printer.format(ctx, buf));
            assertEquals("hello\\nworld\\t'\\\\", buf.toString()); // Expected escaped string
        }

        @Test
        void testFormatEscapedOptionEmptyText() {
            var printer = new TextPrinterParser(TextOption.ESCAPED.withDefaultValue(""));
            StringBuilder buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX_EMPTY, buf));
            assertTrue(buf.isEmpty()); // Assuming getDefaultValue is "" and failOnDefault is false
        }


        // --- peek() tests ---

        @Test
        void testPeekNothingOptionNoSuccessors() {
            // TextPrinterParser is the only parser in the chain
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            SymbolParseContext ctx = createTextParseContext(textParser);

            assertEquals("some text to parse".length(),
                    textParser.peek(ctx, "some text to parse", 0));
        }

        @Test
        void testPeekNothingOptionWithLiteralSuccessor() {
            // TextPrinterParser followed by a LiteralPrinterParser("END")
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            SymbolParseContext ctx = createTextParseContext(textParser,
                    new SymbolFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals("hello world".length(), textParser.peek(ctx, "hello worldEND", 0));
        }

        @Test
        void testPeekNothingOptionLiteralNotFound() {
            // TextPrinterParser followed by a LiteralPrinterParser("END")
            // But "END" is not in the input
           TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            SymbolParseContext ctx = createTextParseContext(textParser,
                    new SymbolFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals("hello world".length(), textParser.peek(ctx, "hello world", 0));
        }

        @Test
        void testPeekEscapedOptionWithLiteralSuccessor() {
            // TextPrinterParser(ESCAPED) followed by LiteralPrinterParser("END")
            // Input has escaped characters
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            SymbolParseContext ctx = createTextParseContext(textPrinterParser,
                    new SymbolFormatterBuilder.LiteralPrinterParser("END"));

            assertEquals(12, textPrinterParser.peek(ctx, "hello\\nworldEND", 0));
        }

        @Test
        void testPeekEscapedOptionInvalidEscapeSequence() {
            // TextPrinterParser(ESCAPED) followed by LiteralPrinterParser("END")
            // Input has invalid escape sequence
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            SymbolParseContext ctx = createTextParseContext(textPrinterParser,
                    new SymbolFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals(~5, textPrinterParser.peek(ctx, "hello\\qworldEND", 0)); // invalid escape at position 5
        }

        // --- parse() tests ---

        @Test
        void testParseNothingOption() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.NOTHING);
            var ctx = createTextParseContext(textPrinterParser);
            int end = textPrinterParser.parse(ctx, "simple text", 0);
            assertEquals("simple text".length(), end);
            assertEquals("simple text", ctx.resolveFields().text());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseEscapedOption() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            var ctx = createTextParseContext(textPrinterParser);

            int end = textPrinterParser.parse(ctx, "escaped\\ntext", 0);
            assertEquals("escaped\\ntext".length(), end); // consumes entire string
            assertEquals("escaped\ntext", ctx.resolveFields().text()); // unescaped result
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseEscapedOptionInvalidEscapeSequence() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            var ctx = createTextParseContext(textPrinterParser);

            int end = textPrinterParser.parse(ctx, "invalid\\qescape", 0);
            assertEquals(~7, end); // Fails at '\q' (position 7)
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Invalid escape sequence found near 'invalid\\qe'", ctx.getErrorMessage());
        }

        // --- toString() tests ---

        @Test
        void testToStringNothingOption() {
            var printer = new TextPrinterParser(TextOption.NOTHING);
            assertEquals("x", printer.toString());
        }

        @Test
        void testToStringEscapedOption() {
            var printer = new TextPrinterParser(TextOption.ESCAPED);
            assertEquals("X", printer.toString());
        }
    }

    @Nested
    class SymbolicTypePrinterParserTest {

        private static final SymbolFormatterBuilder.SymbolicTypePrinterParser PARSER = new SymbolFormatterBuilder.SymbolicTypePrinterParser();
        private static final Vocabulary VOCABULARY;

        static {
            String[] literalNames = {null, null, null, "'c'", null, null, null};
            String[] symbolicNames = {null, "A", "B", "C", "B", "LONGEST_B", "LONG"};
            VOCABULARY = new VocabularyImpl(literalNames, symbolicNames);
            // EOF -> -1
            // A -> 1
            // B -> 2
            // C, 'c' -> 3
            // B -> 4
            // LONGEST_B -> 5
            // SYM -> 6
        }

        @Test
        void testFormatSuccess() {
            Symbol symbolA = Symbol.of().type(1).get(); // Type 1 is "A"
            SymbolFormatContext ctxA = new SymbolFormatContext(symbolA, VOCABULARY);
            StringBuilder bufA = new StringBuilder();
            assertTrue(PARSER.format(ctxA, bufA));
            assertEquals("A", bufA.toString());

            Symbol symbolEof = Symbol.of().type(Symbol.EOF).get();
            SymbolFormatContext ctxEof = new SymbolFormatContext(symbolEof, VOCABULARY);
            StringBuilder bufEof = new StringBuilder();
            assertTrue(PARSER.format(ctxEof, bufEof));
            assertEquals("EOF", bufEof.toString());
        }

        @Test
        void testFormatNoVocabulary() {
            Symbol symbol = Symbol.of().type(1).get();
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, null); // No vocabulary
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNoSymbolicName() {
            String[] literalNames = {null, null, "'b'"};
            String[] symbolicNames = {null, "A", null};
            Vocabulary vocab = new VocabularyImpl(literalNames, symbolicNames);

            Symbol symbol = Symbol.of().type(2).get(); // Type 2 has literal name 'b' but no symbolic name
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, vocab);
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("A".length(), PARSER.peek(ctx, "A rest of string", 0));
            assertEquals(1 + "B".length(), PARSER.peek(ctx, " B rest of string", 1));
        }

        @Test
        void testPeekLongestMatch() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("LONGEST_B".length(), PARSER.peek(ctx, "LONGEST_B", 0));
            assertEquals("LONG".length(), PARSER.peek(ctx, "LONG", 0));
        }

        @Test
        void testPeekFailure() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.peek(ctx, "D rest of string", 0)); // D is not a symbolic name
        }

        @Test
        void testPeekNoVocabulary() {
            SymbolParseContext ctx = new SymbolParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "A", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "A", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "A", 2));
        }

        @Test
        void testParseSuccess() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("A".length(), PARSER.parse(ctx, "A rest", 0));
            assertEquals(1, ctx.resolveFields().type()); // Type 1 is "A"
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.parse(ctx, "D rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Unrecognized symbolic type name starting with 'D res'", ctx.getErrorMessage());
        }

        @Test
        void testParseNoVocabulary() {
            SymbolParseContext ctx = new SymbolParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "A rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("No vocabulary set", ctx.getErrorMessage());
        }

        @Test
        void testParseWithAmbiguousSymbolicName() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("B".length(), PARSER.parse(ctx, "B", 0));
            assertEquals(2, ctx.resolveFields().type()); // Should resolve to the first type (2) for "B"
        }

        @Test
        void testParseLongestMatch() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("LONGEST_B".length(), PARSER.parse(ctx, "LONGEST_B", 0));
            assertEquals(5, ctx.resolveFields().type());
        }

        @Test
        void testParseEOF() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("EOF".length(), PARSER.parse(ctx, "EOF", 0));
            assertEquals(Symbol.EOF, ctx.resolveFields().type());
        }

        @Test
        void testToString() {
            assertEquals("s", PARSER.toString());
        }
    }

    @Nested
    class LiteralTypePrinterParserTest {

        private static final SymbolFormatterBuilder.LiteralTypePrinterParser PARSER = new SymbolFormatterBuilder.LiteralTypePrinterParser();
        private static final Vocabulary VOCABULARY;

        static {
            String[] literalNames = {null, "'a'", "'b'", null, "'b'", "'longest_b'", "'long'"};
            String[] symbolicNames = {null, "A", "B", "C", "B_ALIAS", "LONGEST_B_S", "LONG_S"};
            VOCABULARY = new VocabularyImpl(literalNames, symbolicNames);
            // 'a', A -> 1
            // 'b', B -> 2
            // C -> 3
            // 'b', B_ALIAS -> 4
            // 'longest_b', LONGEST_B_S -> 5
            // 'long', LONG_S -> 6
        }

        @Test
        void testFormatSuccess() {
            Symbol symbol = Symbol.of().type(1).get(); // Type 1 is literal 'a'
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, VOCABULARY);
            StringBuilder buf = new StringBuilder();
            assertTrue(PARSER.format(ctx, buf));
            assertEquals("'a'", buf.toString());
        }

        @Test
        void testFormatNoVocabulary() {
            Symbol symbol = Symbol.of().type(1).get();
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, null); // No vocabulary
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNoLiteralName() {
            Symbol symbol = Symbol.of().type(3).get(); // Type 3 has symbolic name C but no literal name
            SymbolFormatContext ctx = new SymbolFormatContext(symbol, VOCABULARY);
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("'a'".length(), PARSER.peek(ctx, "'a' rest of string", 0));
            assertEquals(1 + "'b'".length(), PARSER.peek(ctx, " 'b' rest of string", 1));
        }

        @Test
        void testPeekLongestMatch() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("'longest_b'".length(), PARSER.peek(ctx, "'longest_b'", 0));
            assertEquals("'long'".length(), PARSER.peek(ctx, "'long'", 0));
        }

        @Test
        void testPeekFailure() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.peek(ctx, "'d' rest of string", 0)); // 'd' is not in vocabulary
        }

        @Test
        void testPeekNoVocabulary() {
            SymbolParseContext ctx = new SymbolParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "'a'", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "'a'", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "'a'", 4));
        }

        @Test
        void testParseSuccess() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("'a'".length(), PARSER.parse(ctx, "'a' rest", 0));
            Symbol result = ctx.resolveFields();
            assertEquals(1, result.type());
            assertEquals("a", result.text());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.parse(ctx, "'d' rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Unrecognized literal type name starting with ''d' r'", ctx.getErrorMessage());
        }

        @Test
        void testParseNoVocabulary() {
            SymbolParseContext ctx = new SymbolParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "'a' rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("No vocabulary set", ctx.getErrorMessage());
        }

        @Test
        void testParseWithAmbiguousLiteralName() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("'b'".length(), PARSER.parse(ctx, "'b'", 0));
            assertEquals(2, ctx.resolveFields().type()); // Should resolve to the first type (2) for "'b'"
        }

        @Test
        void testParseLongestMatch() {
            SymbolParseContext ctx = new SymbolParseContext(null, VOCABULARY);
            assertEquals("'longest_b'".length(), PARSER.parse(ctx, "'longest_b'", 0));
            Symbol result = ctx.resolveFields();
            assertEquals(5, result.type());
            assertEquals("longest_b", result.text());
        }

        @Test
        void testToString() {
            assertEquals("l", PARSER.toString());
        }
    }
}