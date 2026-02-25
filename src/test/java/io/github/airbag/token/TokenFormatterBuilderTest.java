package io.github.airbag.token;

import io.github.airbag.token.TokenFormatterBuilder.TokenPrinterParser;
import io.github.airbag.token.TokenFormatterBuilder.TextPrinterParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenFormatterBuilderTest {

    private static final Token SYMBOL = new TokenBuilder()
            .index(0)
            .start(-1)
            .stop(2)
            .text("test")
            .type(3)
            .channel(-4)
            .line(5)
            .position(6)
            .get();

    private static final TokenFormatContext FORMAT_CTX = new TokenFormatContext(SYMBOL, null);

    private static final String INPUT = "  <-10>   'My getText is \\' quoted' --10\t\r\n  ";

    @Nested
    class IntegerPrinterParserTest {




        @Test
        void testIntegerPrinterPositiveNumber() {
            var integerPrinter = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.INDEX,
                    false);
            StringBuilder buf = new StringBuilder();
            boolean b = assertDoesNotThrow(() -> integerPrinter.format(FORMAT_CTX, buf));
            assertTrue(b);
            assertEquals("0", buf.toString());
        }

        @Test
        void testIntegerPrinterNegativeNumber() {
            var integerPrinter = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.CHANNEL,
                    false);
            StringBuilder buf = new StringBuilder();
            boolean b = assertDoesNotThrow(() -> integerPrinter.format(FORMAT_CTX, buf));
            assertTrue(b);
            assertEquals("-4", buf.toString());
        }

        @Test
        void testPrintDefaultValue() {
            var strictPrinter = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.START,
                    true);
            var lenientPrinter = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.START,
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
        void testPeekEmptyInput() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(~0, integerPrinterParser.peek(ctx, "", 0));
        }

        @Test
        void testParseEmptyInput() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(~0, integerPrinterParser.parse(ctx, "", 0));
        }

        @Test
        void testPeekWithInvalidPosition() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertThrows(IndexOutOfBoundsException.class,
                    () -> integerPrinterParser.peek(ctx, INPUT, 100));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> integerPrinterParser.peek(ctx, INPUT, -10));
        }

        @Test
        void testPeekWithInvalidNumber() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(~1, integerPrinterParser.peek(ctx, " --10", 1));
            assertEquals(~0, integerPrinterParser.peek(ctx, " --10", 0));
        }

        @Test
        void testParseWithInvalidNumber() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(~1, integerPrinterParser.parse(ctx, " --10", 1));
            assertEquals("Expected an integer for field 'type' but found '--1'",
                    ctx.getErrorMessage());
        }

        @Test
        void testParseWithNegativeNumber() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " --10", 2));
            Token symbol = ctx.resolveFields();
            assertEquals(-10, symbol.getType());
        }

        @Test
        void testParseWithPositiveNumber() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " 1050 -1000", 1));
            Token symbol = ctx.resolveFields();
            assertEquals(1050, symbol.getType());
        }

        @Test
        void testParseWithLeadingZeros() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(5, integerPrinterParser.parse(ctx, " 0050 -1000", 1));
            Token symbol = ctx.resolveFields();
            assertEquals(50, symbol.getType());
        }

        @Test
        void testParseIntegerOverflow() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            String overflowValue = " " + "2147483648"; // Integer.MAX_VALUE + 1
            assertEquals(~1, integerPrinterParser.parse(ctx, overflowValue, 1));
            assertTrue(ctx.getErrorMessage().contains("is out of range for field 'type'"));
        }

        @Test
        void testParseIntegerUnderflow() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            String underflowValue = " " + "-2147483649"; // Integer.MIN_VALUE - 1
            assertEquals(~1, integerPrinterParser.parse(ctx, underflowValue, 1));
            assertTrue(ctx.getErrorMessage().contains("is out of range for field 'type'"));
        }

        @Test
        void testParseZero() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(2, integerPrinterParser.parse(ctx, " 0 ", 1));
            Token symbol = ctx.resolveFields();
            assertEquals(0, symbol.getType());
        }

        @Test
        void testParseWithNonNumeric() {
            var integerPrinterParser = new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE);
            var ctx = new TokenParseContext(null, null);
            assertEquals(~1, integerPrinterParser.parse(ctx, " abc", 1));
            assertEquals("Expected an integer for field 'type' but found 'abc'",
                    ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertAll(() -> assertEquals("i",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE,
                                    true).toString()),
                    () -> assertEquals("I",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.TYPE,
                                    false).toString()),
                    () -> assertEquals("n",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.INDEX,
                                    true).toString()),
                    () -> assertEquals("N",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.INDEX,
                                    false).toString()),
                    () -> assertEquals("r",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.LINE,
                                    true).toString()),
                    () -> assertEquals("R",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.LINE,
                                    false).toString()),
                    () -> assertEquals("p",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.POSITION,
                                    true).toString()),
                    () -> assertEquals("P",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.POSITION,
                                    false).toString()),
                    () -> assertEquals("c",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.CHANNEL,
                                    true).toString()),
                    () -> assertEquals("C",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.CHANNEL,
                                    false).toString()),
                    () -> assertEquals("b",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.START,
                                    true).toString()),
                    () -> assertEquals("B",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.START,
                                    false).toString()),
                    () -> assertEquals("e",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.STOP,
                                    true).toString()),
                    () -> assertEquals("E",
                            new TokenFormatterBuilder.IntegerPrinterParser(TokenField.STOP,
                                    false).toString()));
        }

    }

    @Nested
    class LiteralPrinterParserTest {

        private static final TokenFormatContext FORMAT_CTX = new TokenFormatContext(null, null);
        private static final TokenParseContext PARSE_CTX = new TokenParseContext(null, null);

        @Test
        void testFormat() {
            var printer = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            var buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX, buf));
            assertEquals("=>", buf.toString());
        }

        @Test
        void testFormatEmpty() {
            var printer = new TokenFormatterBuilder.LiteralPrinterParser("");
            var buf = new StringBuilder();
            assertTrue(printer.format(FORMAT_CTX, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(2, parser.peek(PARSE_CTX, "=> 123", 0));
            assertEquals(5, parser.peek(PARSE_CTX, "abc=>123", 3));
        }

        @Test
        void testPeekFailure() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(~0, parser.peek(PARSE_CTX, "-> 123", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(~0, parser.peek(PARSE_CTX, "=", 0));
            assertEquals(~1, parser.peek(PARSE_CTX, "a=", 1));
        }

        @Test
        void testPeekAtEnd() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            assertEquals(2, parser.peek(PARSE_CTX, "=>", 0));
        }

        @Test
        void testPeekEmpty() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("");
            assertEquals(0, parser.peek(PARSE_CTX, "abc", 0));
            assertEquals(3, parser.peek(PARSE_CTX, "abc", 3));
        }

        @Test
        void testPeekInvalidPosition() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            assertThrows(IndexOutOfBoundsException.class, () -> parser.peek(PARSE_CTX, "=>", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.peek(PARSE_CTX, "=>", 3));
        }

        @Test
        void testParseSuccess() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            var ctx = new TokenParseContext(null, null);
            assertEquals(2, parser.parse(ctx, "=>123", 0));
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("=>");
            var ctx = new TokenParseContext(null, null);
            assertEquals(~0, parser.parse(ctx, "->123", 0));
            assertEquals("Expected literal '=>' but found '->'", ctx.getErrorMessage());
        }

        @Test
        void testParseFailureWithSpecialChars() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("a\nb");
            var ctx = new TokenParseContext(null, null);
            assertEquals(~0, parser.parse(ctx, "acb", 0));
            assertEquals("Expected literal 'a\\nb' but found 'acb'", ctx.getErrorMessage());
        }

        @Test
        void testParseEmpty() {
            var parser = new TokenFormatterBuilder.LiteralPrinterParser("");
            var ctx = new TokenParseContext(null, null);
            assertEquals(0, parser.parse(ctx, "abc", 0));
            assertNull(ctx.getErrorMessage());
            assertEquals(3, parser.parse(ctx, "abc", 3));
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertAll(() -> assertEquals("'abc'",
                            new TokenFormatterBuilder.LiteralPrinterParser("abc").toString()),
                    () -> assertEquals("",
                            new TokenFormatterBuilder.LiteralPrinterParser("").toString()),
                    () -> assertEquals("'a[b'",
                            new TokenFormatterBuilder.LiteralPrinterParser("a[b").toString()),
                    () -> assertEquals("'a[b]c'",
                            new TokenFormatterBuilder.LiteralPrinterParser("a[b]c").toString()),
                    () -> assertEquals("a\\'\\[\\b\\]",
                            new TokenFormatterBuilder.LiteralPrinterParser("a'[b]").toString()),
                    () -> assertEquals("\\s",
                            new TokenFormatterBuilder.LiteralPrinterParser("s").toString()),
                    () -> assertEquals("'[]'",
                            new TokenFormatterBuilder.LiteralPrinterParser("[]").toString()),
                    () -> assertEquals("\\[",
                            new TokenFormatterBuilder.LiteralPrinterParser("[").toString()));
        }
    }

    @Nested
    class TextPrinterParserTest {

        // FORMAT_CTX with a Token that has some getText
        private static final Token SYMBOL_WITH_TEXT = new TokenBuilder().text("hello world").get();
        private static final TokenFormatContext FORMAT_CTX_TEXT = new TokenFormatContext(
                SYMBOL_WITH_TEXT,
                null);

        private static final Token SYMBOL_EMPTY_TEXT = new TokenBuilder().text("").get();
        private static final TokenFormatContext FORMAT_CTX_EMPTY = new TokenFormatContext(
                SYMBOL_EMPTY_TEXT,
                null);

        // This is a test helper that creates a TextPrinterParser and wraps it in a CompositePrinterParser
        // so it can be used in TokenParseContext for peek/parse tests where successors are needed.
        private TokenParseContext createTextParseContext(TextPrinterParser textParser,
                                                         TokenPrinterParser... successors) {
            List<TokenPrinterParser> chain = new ArrayList<>();
            chain.add(textParser); // The parser being tested
            Collections.addAll(chain, successors); // Successors in the chain
            TokenFormatterBuilder.CompositePrinterParser rootParser = new TokenFormatterBuilder.CompositePrinterParser(
                    chain,
                    false);
            return new TokenParseContext(rootParser, null);
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
            Token symbol = new TokenBuilder().text("hello\nworld\t'\\").get(); // Text with special chars
            TokenFormatContext ctx = new TokenFormatContext(symbol, null);

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
        void testPeekEmptyInput() {
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            TokenParseContext ctx = createTextParseContext(textParser);
            assertEquals(0, textParser.peek(ctx, "", 0));
        }

        @Test
        void testParseEmptyInput() {
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            var ctx = createTextParseContext(textParser);
            int end = textParser.parse(ctx, "", 0);
            assertEquals(0, end);
            assertEquals("", ctx.resolveFields().getText());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testPeekNothingOptionNoSuccessors() {
            // TextPrinterParser is the only parser in the chain
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            TokenParseContext ctx = createTextParseContext(textParser);

            assertEquals("some getText to parse".length(),
                    textParser.peek(ctx, "some getText to parse", 0));
        }

        @Test
        void testPeekNothingOptionWithLiteralSuccessor() {
            // TextPrinterParser followed by a LiteralPrinterParser("END")
            TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            TokenParseContext ctx = createTextParseContext(textParser,
                    new TokenFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals("hello world".length(), textParser.peek(ctx, "hello worldEND", 0));
        }

        @Test
        void testPeekNothingOptionLiteralNotFound() {
            // TextPrinterParser followed by a LiteralPrinterParser("END")
            // But "END" is not in the input
           TextPrinterParser textParser = new TextPrinterParser(TextOption.NOTHING);
            TokenParseContext ctx = createTextParseContext(textParser,
                    new TokenFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals("hello world".length(), textParser.peek(ctx, "hello world", 0));
        }

        @Test
        void testPeekEscapedOptionWithLiteralSuccessor() {
            // TextPrinterParser(ESCAPED) followed by LiteralPrinterParser("END")
            // Input has escaped characters
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            TokenParseContext ctx = createTextParseContext(textPrinterParser,
                    new TokenFormatterBuilder.LiteralPrinterParser("END"));

            assertEquals(12, textPrinterParser.peek(ctx, "hello\\nworldEND", 0));
        }

        @Test
        void testPeekEscapedOptionInvalidEscapeSequence() {
            // TextPrinterParser(ESCAPED) followed by LiteralPrinterParser("END")
            // Input has invalid escape sequence
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            TokenParseContext ctx = createTextParseContext(textPrinterParser,
                    new TokenFormatterBuilder.LiteralPrinterParser("END"));
            assertEquals(~5, textPrinterParser.peek(ctx, "hello\\qworldEND", 0)); // invalid escape at getCharPositionInLine 5
        }

        // --- parse() tests ---

        @Test
        void testParseNothingOption() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.NOTHING);
            var ctx = createTextParseContext(textPrinterParser);
            int end = textPrinterParser.parse(ctx, "simple getText", 0);
            assertEquals("simple getText".length(), end);
            assertEquals("simple getText", ctx.resolveFields().getText());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseEscapedOption() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            var ctx = createTextParseContext(textPrinterParser);

            int end = textPrinterParser.parse(ctx, "escaped\\ntext", 0);
            assertEquals("escaped\\ntext".length(), end); // consumes entire string
            assertEquals("escaped\ntext", ctx.resolveFields().getText()); // unescaped result
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseEscapedOptionInvalidEscapeSequence() {
            TextPrinterParser textPrinterParser = new TextPrinterParser(TextOption.ESCAPED);
            var ctx = createTextParseContext(textPrinterParser);

            int end = textPrinterParser.parse(ctx, "invalid\\qescape", 0);
            assertEquals(~7, end); // Fails at '\q' (getCharPositionInLine 7)
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

        @Test
        void testParseWithTextInsideOptionalGroup() throws TokenParseException {
            var formatter = new TokenFormatterBuilder()
                    .appendLiteral("START-")
                    .startOptional()
                    .appendText()
                    .appendLiteral("-MIDDLE")
                    .endOptional()
                    .appendLiteral("-END")
                    .toFormatter();

            // Optional part is present
            Token symbol1 = formatter.parse("START-getText-MIDDLE-END");
            assertEquals("getText", symbol1.getText());

            // Optional part is absent
            Token symbol2 = formatter.parse("START--END");
            assertEquals(TokenField.TEXT.getDefault(), symbol2.getText());
        }
    }

    @Nested
    class SymbolicTypePrinterParserTest {

        private static final TokenFormatterBuilder.SymbolicTypePrinterParser PARSER = new TokenFormatterBuilder.SymbolicTypePrinterParser();
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
            Token symbolA = new TokenBuilder().type(1).get(); // Type 1 is "A"
            TokenFormatContext ctxA = new TokenFormatContext(symbolA, VOCABULARY);
            StringBuilder bufA = new StringBuilder();
            assertTrue(PARSER.format(ctxA, bufA));
            assertEquals("A", bufA.toString());

            Token symbolEof = new TokenBuilder().type(Token.EOF).get();
            TokenFormatContext ctxEof = new TokenFormatContext(symbolEof, VOCABULARY);
            StringBuilder bufEof = new StringBuilder();
            assertTrue(PARSER.format(ctxEof, bufEof));
            assertEquals("EOF", bufEof.toString());
        }

        @Test
        void testFormatNoVocabulary() {
            Token symbol = new TokenBuilder().type(1).get();
            TokenFormatContext ctx = new TokenFormatContext(symbol, null); // No vocabulary
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNoSymbolicName() {
            String[] literalNames = {null, null, "'b'"};
            String[] symbolicNames = {null, "A", null};
            Vocabulary vocab = new VocabularyImpl(literalNames, symbolicNames);

            Token symbol = new TokenBuilder().type(2).get(); // Type 2 has literal name 'b' but no symbolic name
            TokenFormatContext ctx = new TokenFormatContext(symbol, vocab);
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("A".length(), PARSER.peek(ctx, "A rest of string", 0));
            assertEquals(1 + "B".length(), PARSER.peek(ctx, " B rest of string", 1));
        }

        @Test
        void testPeekLongestMatch() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("LONGEST_B".length(), PARSER.peek(ctx, "LONGEST_B", 0));
            assertEquals("LONG".length(), PARSER.peek(ctx, "LONG", 0));
        }

        @Test
        void testPeekFailure() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.peek(ctx, "D rest of string", 0)); // D is not a symbolic name
        }

        @Test
        void testPeekNoVocabulary() {
            TokenParseContext ctx = new TokenParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "A", 0));
        }

        @Test
        void testPeekNoInput() {
            TokenParseContext ctx = new TokenParseContext(null,VOCABULARY); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "", 0));
        }

        @Test
        void testParseEmptyInput() {
            TokenParseContext ctx = new TokenParseContext(null,VOCABULARY); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "A", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "A", 2));
        }

        @Test
        void testParseSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("A".length(), PARSER.parse(ctx, "A rest", 0));
            assertEquals(1, ctx.resolveFields().getType()); // Type 1 is "A"
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.parse(ctx, "D rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Unrecognized symbolic type name starting with 'D res'", ctx.getErrorMessage());
        }

        @Test
        void testParseNoVocabulary() {
            TokenParseContext ctx = new TokenParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "A rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("No vocabulary set", ctx.getErrorMessage());
        }

        @Test
        void testParseWithAmbiguousSymbolicName() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("B".length(), PARSER.parse(ctx, "B", 0));
            assertEquals(2, ctx.resolveFields().getType()); // Should resolve to the first getType (2) for "B"
        }

        @Test
        void testParseLongestMatch() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("LONGEST_B".length(), PARSER.parse(ctx, "LONGEST_B", 0));
            assertEquals(5, ctx.resolveFields().getType());
        }

        @Test
        void testParseEOF() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("EOF".length(), PARSER.parse(ctx, "EOF", 0));
            assertEquals(Token.EOF, ctx.resolveFields().getType());
        }

        @Test
        void testToString() {
            assertEquals("s", PARSER.toString());
        }
    }

    @Nested
    class LiteralTypePrinterParserTest {

        private static final TokenFormatterBuilder.LiteralTypePrinterParser PARSER = new TokenFormatterBuilder.LiteralTypePrinterParser();
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
            Token symbol = new TokenBuilder().type(1).get(); // Type 1 is literal 'a'
            TokenFormatContext ctx = new TokenFormatContext(symbol, VOCABULARY);
            StringBuilder buf = new StringBuilder();
            assertTrue(PARSER.format(ctx, buf));
            assertEquals("'a'", buf.toString());
        }

        @Test
        void testFormatNoVocabulary() {
            Token symbol = new TokenBuilder().type(1).get();
            TokenFormatContext ctx = new TokenFormatContext(symbol, null); // No vocabulary
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNoLiteralName() {
            Token symbol = new TokenBuilder().type(3).get(); // Type 3 has symbolic name C but no literal name
            TokenFormatContext ctx = new TokenFormatContext(symbol, VOCABULARY);
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("'a'".length(), PARSER.peek(ctx, "'a' rest of string", 0));
            assertEquals(1 + "'b'".length(), PARSER.peek(ctx, " 'b' rest of string", 1));
        }

        @Test
        void testPeekLongestMatch() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("'longest_b'".length(), PARSER.peek(ctx, "'longest_b'", 0));
            assertEquals("'long'".length(), PARSER.peek(ctx, "'long'", 0));
        }

        @Test
        void testPeekFailure() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.peek(ctx, "'d' rest of string", 0)); // 'd' is not in vocabulary
        }

        @Test
        void testPeekNoVocabulary() {
            TokenParseContext ctx = new TokenParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "'a'", 0));
        }

        @Test
        void testPeekOutOfBounds() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "'a'", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "'a'", 4));
        }

        @Test
        void testPeekNoInput() {
            TokenParseContext ctx = new TokenParseContext(null,VOCABULARY); // No vocabulary
            assertEquals(~0, PARSER.peek(ctx, "", 0));
        }

        @Test
        void testParseEmptyInput() {
            TokenParseContext ctx = new TokenParseContext(null,VOCABULARY); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "", 0));
        }

        @Test
        void testParseSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("'a'".length(), PARSER.parse(ctx, "'a' rest", 0));
            Token result = ctx.resolveFields();
            assertEquals(1, result.getType());
            assertEquals("a", result.getText());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals(~0, PARSER.parse(ctx, "'d' rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Unrecognized literal type name starting with ''d' r'", ctx.getErrorMessage());
        }

        @Test
        void testParseNoVocabulary() {
            TokenParseContext ctx = new TokenParseContext(null, null); // No vocabulary
            assertEquals(~0, PARSER.parse(ctx, "'a' rest", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("No vocabulary set", ctx.getErrorMessage());
        }

        @Test
        void testParseWithAmbiguousLiteralName() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("'b'".length(), PARSER.parse(ctx, "'b'", 0));
            assertEquals(2, ctx.resolveFields().getType()); // Should resolve to the first getType (2) for "'b'"
        }

        @Test
        void testParseLongestMatch() {
            TokenParseContext ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals("'longest_b'".length(), PARSER.parse(ctx, "'longest_b'", 0));
            Token result = ctx.resolveFields();
            assertEquals(5, result.getType());
            assertEquals("longest_b", result.getText());
        }

        @Test
        void testToString() {
            assertEquals("l", PARSER.toString());
        }
    }

    @Nested
    class TypePrinterParserTest {
        private static final Vocabulary VOCABULARY;
        private static final Token SYMBOL_SYM_ONLY = new TokenBuilder().type(1).get(); // Symbolic: A
        private static final Token SYMBOL_BOTH = new TokenBuilder().type(2).get(); // Symbolic: B, Literal: 'b'
        private static final Token SYMBOL_LIT_ONLY = new TokenBuilder().type(3).get(); // Literal: 'c'
        private static final Token SYMBOL_NEITHER = new TokenBuilder().type(4).get(); // No symbolic or literal name

        static {
            String[] literalNames = {null, null, "'b'", "'c'", null, null};
            String[] symbolicNames = {null, "A", "B", null, null, "123"};
            VOCABULARY = new VocabularyImpl(literalNames, symbolicNames);
        }

        @Test
        void testFormatIntegerOnly() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.INTEGER_ONLY);
            var buf = new StringBuilder();
            assertTrue(p.format(new TokenFormatContext(SYMBOL_BOTH, VOCABULARY), buf));
            assertEquals("2", buf.toString());
        }

        @Test
        void testFormatSymbolicOnly() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_ONLY);
            var buf = new StringBuilder();

            // Success
            assertTrue(p.format(new TokenFormatContext(SYMBOL_SYM_ONLY, VOCABULARY), buf));
            assertEquals("A", buf.toString());

            // Failure
            buf.setLength(0);
            assertFalse(p.format(new TokenFormatContext(SYMBOL_LIT_ONLY, VOCABULARY), buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatLiteralOnly() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.LITERAL_ONLY);
            var buf = new StringBuilder();

            // Success
            assertTrue(p.format(new TokenFormatContext(SYMBOL_LIT_ONLY, VOCABULARY), buf));
            assertEquals("'c'", buf.toString());

            // Failure
            buf.setLength(0);
            assertFalse(p.format(new TokenFormatContext(SYMBOL_SYM_ONLY, VOCABULARY), buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatSymbolicFirst() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_FIRST);
            var buf = new StringBuilder();

            // Prefers symbolic
            p.format(new TokenFormatContext(SYMBOL_BOTH, VOCABULARY), buf);
            assertEquals("B", buf.toString());
            buf.setLength(0);

            // Falls back to literal
            p.format(new TokenFormatContext(SYMBOL_LIT_ONLY, VOCABULARY), buf);
            assertEquals("'c'", buf.toString());
            buf.setLength(0);

            // Falls back to integer
            p.format(new TokenFormatContext(SYMBOL_NEITHER, VOCABULARY), buf);
            assertEquals("4", buf.toString());
        }

        @Test
        void testFormatLiteralFirst() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.LITERAL_FIRST);
            var buf = new StringBuilder();

            // Prefers literal
            p.format(new TokenFormatContext(SYMBOL_BOTH, VOCABULARY), buf);
            assertEquals("'b'", buf.toString());
            buf.setLength(0);

            // Falls back to symbolic
            p.format(new TokenFormatContext(SYMBOL_SYM_ONLY, VOCABULARY), buf);
            assertEquals("A", buf.toString());
            buf.setLength(0);

            // Falls back to integer
            p.format(new TokenFormatContext(SYMBOL_NEITHER, VOCABULARY), buf);
            assertEquals("4", buf.toString());
        }

        @Test
        void testParseSymbolicFirst() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_FIRST);
            var ctx = new TokenParseContext(null, VOCABULARY);

            p.parse(ctx, "A", 0);
            assertEquals(1, ctx.resolveFields().getType());

            ctx = new TokenParseContext(null, VOCABULARY);
            p.parse(ctx, "'c'", 0);
            assertEquals(3, ctx.resolveFields().getType());

            ctx = new TokenParseContext(null, VOCABULARY);
            p.parse(ctx, "4", 0);
            assertEquals(4, ctx.resolveFields().getType());

            // "123" is a symbolic name, should be parsed as such first
            ctx = new TokenParseContext(null, VOCABULARY);
            p.parse(ctx, "123", 0);
            assertEquals(5, ctx.resolveFields().getType());
        }

        @Test
        void testParseLiteralFirst() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.LITERAL_FIRST);
            var ctx = new TokenParseContext(null, VOCABULARY);

            p.parse(ctx, "'b'", 0);
            assertEquals(2, ctx.resolveFields().getType());

            ctx = new TokenParseContext(null, VOCABULARY);
            p.parse(ctx, "A", 0);
            assertEquals(1, ctx.resolveFields().getType());

            ctx = new TokenParseContext(null, VOCABULARY);
            p.parse(ctx, "4", 0);
            assertEquals(4, ctx.resolveFields().getType());
        }

        @Test
        void testParseFailure() {
            var p = new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_FIRST);
            var ctx = new TokenParseContext(null, VOCABULARY);
            assertEquals(~0, p.parse(ctx, "unrecognized", 0));
            assertEquals("Unrecognized type information starting with 'unrec'", ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertEquals("i", new TokenFormatterBuilder.TypePrinterParser(TypeFormat.INTEGER_ONLY).toString());
            assertEquals("s", new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_ONLY).toString());
            assertEquals("l", new TokenFormatterBuilder.TypePrinterParser(TypeFormat.LITERAL_ONLY).toString());
            assertEquals("S", new TokenFormatterBuilder.TypePrinterParser(TypeFormat.SYMBOLIC_FIRST).toString());
            assertEquals("L", new TokenFormatterBuilder.TypePrinterParser(TypeFormat.LITERAL_FIRST).toString());
        }
    }

    @Nested
    class EOFPrinterParserTest {
        private static final TokenFormatterBuilder.EOFPrinterParser PARSER = new TokenFormatterBuilder.EOFPrinterParser();

        @Test
        void testFormatSuccess() {
            Token symbol = new TokenBuilder().type(Token.EOF).get();
            TokenFormatContext ctx = new TokenFormatContext(symbol, null);
            StringBuilder buf = new StringBuilder();
            assertTrue(PARSER.format(ctx, buf));
            assertEquals("EOF", buf.toString());
        }

        @Test
        void testFormatFailure() {
            Token symbol = new TokenBuilder().type(1).get(); // Not EOF
            TokenFormatContext ctx = new TokenFormatContext(symbol, null);
            StringBuilder buf = new StringBuilder();
            assertFalse(PARSER.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testPeekSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, null);
            assertEquals(3, PARSER.peek(ctx, "EOF", 0));
            assertEquals(5, PARSER.peek(ctx, "  EOF  ", 2));
        }

        @Test
        void testPeekFailure() {
            TokenParseContext ctx = new TokenParseContext(null, null);
            assertEquals(~0, PARSER.peek(ctx, "EOX", 0));
            assertEquals(~0, PARSER.peek(ctx, "EO", 0));
            assertEquals(~0, PARSER.peek(ctx, "FOE", 0));
            assertEquals(~0, PARSER.peek(ctx, "", 0));
            assertEquals(~1, PARSER.peek(ctx, "AEO F", 1)); // Partial match
        }

        @Test
        void testPeekOutOfBounds() {
            TokenParseContext ctx = new TokenParseContext(null, null);
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "EOF", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> PARSER.peek(ctx, "EOF", 4));
        }

        @Test
        void testParseSuccess() {
            TokenParseContext ctx = new TokenParseContext(null, null);
            assertEquals(3, PARSER.parse(ctx, "EOF", 0));
            Token result = ctx.resolveFields();
            assertEquals(Token.EOF, result.getType());
            assertEquals("<EOF>", result.getText());
            assertNull(ctx.getErrorMessage());
        }

        @Test
        void testParseFailure() {
            TokenParseContext ctx = new TokenParseContext(null, null);
            assertEquals(~0, PARSER.parse(ctx, "EOX", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Expected 'EOF' but found 'EOX'", ctx.getErrorMessage());

            ctx = new TokenParseContext(null, null);
            assertEquals(~0, PARSER.parse(ctx, "EO", 0));
            assertNotNull(ctx.getErrorMessage());
            assertEquals("Expected 'EOF' but found 'EO'", ctx.getErrorMessage());
        }

        @Test
        void testToString() {
            assertEquals("<EOF>", PARSER.toString());
        }
    }

    @Nested
    class WhitespacePrinterParserTest {

        @Test
        void testConstructorValidation() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TokenFormatterBuilder.WhitespacePrinterParser("a"));
            assertDoesNotThrow(
                    () -> new TokenFormatterBuilder.WhitespacePrinterParser("   \t\n  "));
            assertDoesNotThrow(() -> new TokenFormatterBuilder.WhitespacePrinterParser(""));
        }

        @Test
        void testFormat() {
            var p = new TokenFormatterBuilder.WhitespacePrinterParser("   \t\n  ");
            var buf = new StringBuilder();
            assertTrue(p.format(null, buf)); // Context can be null for this parser
            assertEquals("   \t\n  ", buf.toString());

            var pEmpty = new TokenFormatterBuilder.WhitespacePrinterParser("");
            var bufEmpty = new StringBuilder();
            assertTrue(pEmpty.format(null, bufEmpty));
            assertTrue(bufEmpty.isEmpty());
        }

        @Test
        void testPeekAndParse() {
            var p = new TokenFormatterBuilder.WhitespacePrinterParser(""); // Whitespace content doesn't matter for parsing
            var ctx = new TokenParseContext(null, null);

            // No whitespace
            assertEquals(0, p.peek(ctx, "abc", 0));
            assertEquals(0, p.parse(ctx, "abc", 0));

            // Leading whitespace
            assertEquals(3, p.peek(ctx, "   abc", 0));
            assertEquals(3, p.parse(ctx, "   abc", 0));

            // Mixed whitespace
            assertEquals(4, p.peek(ctx, "\t \n abc", 0));
            assertEquals(4, p.parse(ctx, "\t \n abc", 0));

            // All whitespace
            assertEquals(5, p.peek(ctx, " \t\n  ", 0));
            assertEquals(5, p.parse(ctx, " \t\n  ", 0));

            // End of string
            assertEquals(3, p.peek(ctx, "   ", 0));
            assertEquals(3, p.parse(ctx, "   ", 0));

            // Invalid getCharPositionInLine
            assertThrows(IndexOutOfBoundsException.class, () -> p.peek(ctx, " ", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> p.peek(ctx, " ", 2));
        }

        @Test
        void testToString() {
            assertEquals(" ", new TokenFormatterBuilder.WhitespacePrinterParser(" ").toString());
            assertEquals("\t\n", new TokenFormatterBuilder.WhitespacePrinterParser("\t\n").toString());
            assertEquals("", new TokenFormatterBuilder.WhitespacePrinterParser("").toString());
        }
    }

    @Nested
    class CompositePrinterParserTest {

        private final TokenPrinterParser succeedingParser = new TokenFormatterBuilder.LiteralPrinterParser(
                "OK");
        private final TokenPrinterParser failingParser = new TokenPrinterParser() {
            @Override
            public boolean format(TokenFormatContext context, StringBuilder buf) {
                return false;
            }

            @Override
            public int parse(TokenParseContext context, CharSequence text, int position) {
                return ~position;
            }

            @Override
            public int peek(TokenParseContext context, CharSequence text, int position) {
                return ~position;
            }
        };

        @Test
        void testFormatNonOptional() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, succeedingParser),
                    false);
            var buf = new StringBuilder();
            assertTrue(composite.format(null, buf));
            assertEquals("OKOK", buf.toString());
        }

        @Test
        void testFormatNonOptionalWithFailure() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, failingParser),
                    false);
            var buf = new StringBuilder("Initial");
            assertFalse(composite.format(null, buf));
            assertEquals("Initial", buf.toString()); // Buffer should be reverted
        }

        @Test
        void testFormatOptional() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, succeedingParser),
                    true);
            var buf = new StringBuilder();
            assertTrue(composite.format(null, buf));
            assertEquals("OKOK", buf.toString());
        }

        @Test
        void testFormatOptionalWithFailure() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, failingParser),
                    true);
            var buf = new StringBuilder("Initial");
            assertTrue(composite.format(null, buf)); // Optional composite returns true
            assertEquals("Initial", buf.toString());     // Buffer should be reverted
        }

        @Test
        void testParseNonOptional() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, succeedingParser),
                    false);
            assertEquals(4, composite.parse(new TokenParseContext(null, null), "OKOK", 0));
            assertEquals(~2, composite.parse(new TokenParseContext(null, null), "OKFAIL", 0));
        }

        @Test
        void testParseOptional() {
            var composite = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser, succeedingParser),
                    true);
            assertEquals(4, composite.parse(null, "OKOK", 0));
            assertEquals(0, composite.parse(null, "FAILOK", 0)); // Skips optional part
        }

        @Test
        void testToString() {
            var nonOptional = new TokenFormatterBuilder.CompositePrinterParser(
                    List.of(succeedingParser),
                    false);
            assertEquals("OK", nonOptional.toString());

            var optional = new TokenFormatterBuilder.CompositePrinterParser(List.of(succeedingParser),
                    true);
            assertEquals("[OK]", optional.toString());
        }
    }

    @Nested
    class OptionalSectionTest {

        @Test
        void testNestedOptionalThrowsException() {
            var builder = new TokenFormatterBuilder();
            builder.startOptional();
            assertThrows(IllegalStateException.class, builder::startOptional);
        }

        @Test
        void testEndWithoutStartThrowsException() {
            var builder = new TokenFormatterBuilder();
            assertThrows(IllegalStateException.class, builder::endOptional);
        }

        @Test
        void testFormatOptionalSection() {
            // Strict getChannel 'c' is only printed if not default (0)
            var vocab = new VocabularyImpl(new String[]{null, "ID"}, new String[]{null, "ID"});
            var formatter = new TokenFormatterBuilder().appendSymbolicType()
                    .startOptional()
                    .appendLiteral("[")
                    .appendInteger(TokenField.CHANNEL, true)
                    .appendLiteral("]")
                    .endOptional()
                    .toFormatter().withVocabulary(vocab);

            // Channel is default, should be omitted
            var symbolDefaultChannel = new TokenBuilder().type(1).channel(0).get();
            assertEquals("ID", formatter.format(symbolDefaultChannel));

            // Channel is non-default, should be included
            var symbolNonDefaultChannel = new TokenBuilder().type(1).channel(2).get();
            assertEquals("ID[2]", formatter.format(symbolNonDefaultChannel));
        }

        @Test
        void testParseOptionalSection() throws TokenParseException {
            var vocab = new VocabularyImpl(new String[]{null, "ID"}, new String[]{null, "ID"});
            var formatter = new TokenFormatterBuilder().appendSymbolicType()
                    .startOptional()
                    .appendLiteral(":")
                    .appendInteger(TokenField.LINE)
                    .endOptional()
                    .toFormatter().withVocabulary(vocab);

            // With optional section present
            Token symbol1 = formatter.parse("ID:123");
            assertEquals(1, symbol1.getType());
            assertEquals(123, symbol1.getLine());

            // Without optional section
            Token symbol2 = formatter.parse("ID");
            assertEquals(1, symbol2.getType());
            assertEquals(-1,
                    symbol2.getLine()); // Default value for getLine because it was not parsed
        }
    }
}