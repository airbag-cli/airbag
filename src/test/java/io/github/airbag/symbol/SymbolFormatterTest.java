package io.github.airbag.symbol;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.airbag.Airbag.assertSymbol;
import static org.junit.jupiter.api.Assertions.*;

public class SymbolFormatterTest {

    private static Vocabulary createVocabulary() {
        String[] literalNames = new String[]{
                null, "'='", "'*'", "'/'", "'+'", "'-'", "'('", "')'"
        };
        String[] symbolicNames =
                new String[]{
                        null, null, null, null, null, null, null, null, "ID", "INT", "NEWLINE",
                        "WS"
                };
        return new VocabularyImpl(literalNames, symbolicNames);
    }

    private final static Vocabulary VOCABULARY = createVocabulary();

    @Nested
    class LenientParseTest {

        private final SymbolFormatter formatter = SymbolFormatter.ofPattern("s").withVocabulary(VOCABULARY);

        @Test
        void testSuccessfulLenientParse() {
            FormatterParsePosition position = new FormatterParsePosition(0);
            String input = "ID and more";
            Symbol expected = Symbol.of().type(8).get();

            Symbol parsed = formatter.parse(input, position);

            assertEquals(expected, parsed);
            assertEquals(2, position.getIndex()); // Position after 'ID'
            assertEquals(-1, position.getErrorIndex());
        }

        @Test
        void testFailingLenientParse() {
            FormatterParsePosition position = new FormatterParsePosition(0);
            String input = "123 and more"; // '123' is not a symbolic name

            Symbol parsed = formatter.parse(input, position);

            assertNull(parsed);
            assertEquals(0, position.getIndex()); // Position is unchanged
            assertEquals(0, position.getErrorIndex()); // Error at the start
        }

        @Test
        void testParseMultipleSymbols() {
            SymbolFormatter multiFormatter = SymbolFormatter.ofPattern("s \\'x\\'").withVocabulary(VOCABULARY);
            String input = "ID 'one' INT 'two'";
            FormatterParsePosition position = new FormatterParsePosition(0);

            // Parse "ID one"
            Symbol s1 = multiFormatter.parse(input, position);
            assertTrue(position.getMessage().isEmpty());
            assertEquals(8, position.getIndex());
            assertEquals(Symbol.of().type(8).text("one").get(), s1);

            // Advance past space
            position.setIndex(position.getIndex() + 1);

            // Parse "INT two"
            Symbol s2 = multiFormatter.parse(input, position);
            assertEquals(Symbol.of().type(9).text("two").get(), s2);
            assertEquals(18, position.getIndex());
        }
    }

    @Nested
    class ExceptionHandlingTest {


        @Test
        void formatShouldThrowExceptionOnFailure() {
            // Strict symbolic formatter 's' requires a symbolic name.
            SymbolFormatter formatter = SymbolFormatter.ofPattern("s").withVocabulary(VOCABULARY);
            // Type 1 has a literal name ('=') but no symbolic name.
            Symbol symbol = Symbol.of().type(1).text("=").get();
            assertThrows(SymbolFormatterException.class, () -> formatter.format(symbol));
        }

        @Test
        void parseShouldThrowExceptionOnMalformedInput() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("s:'x'").withVocabulary(VOCABULARY);
            String malformedInput = "ID'myId'"; // Missing colon
            assertThrows(SymbolParseException.class, () -> formatter.parse(malformedInput));
        }

        @Test
        void parseShouldThrowExceptionOnTrailingInput() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("s").withVocabulary(VOCABULARY);
            String inputWithTrailingChars = "ID trailing";
            assertThrows(SymbolParseException.class, () -> formatter.parse(inputWithTrailingChars));
        }

        @Test
        void parseShouldThrowExceptionOnNullInput() {
            assertThrows(NullPointerException.class, () -> SymbolFormatter.ANTLR.parse(null));
        }
    }

    @Nested
    class OfPatternFormatterTest {

        @Test
        void testSimplePattern() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("s:\\'x\\'").withVocabulary(VOCABULARY);
            Symbol symbol = Symbol.of().type(8).text("myId").get(); // ID
            String formatted = formatter.format(symbol);
            assertEquals("ID:'myId'", formatted);

            Symbol parsed = formatter.parse(formatted);
            assertEquals(symbol, parsed);
        }

        @Test
        void testPatternWithAlternatives() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("l|s").withVocabulary(VOCABULARY);

            // Test literal part
            Symbol literalSymbol = Symbol.of().type(4).text("+").get(); // '+'
            String formattedLiteral = formatter.format(literalSymbol);
            assertEquals("'+'", formattedLiteral);
            assertEquals(literalSymbol, formatter.parse(formattedLiteral));

            // Test symbolic part
            Symbol symbolicSymbol = Symbol.of().type(8).get(); // ID
            String formattedSymbolic = formatter.format(symbolicSymbol);
            assertEquals("ID", formattedSymbolic);
            assertSymbol(symbolicSymbol, formatter.parse(formattedSymbolic), SymbolField.all());
        }

        @Test
        void testPatternWithOptionalSection() {
            SymbolFormatter formatter = SymbolFormatter.ofPattern("s[:c]").withVocabulary(VOCABULARY);

            // Test without optional part
            Symbol symbol = Symbol.of().type(8).channel(0).get(); // ID on default channel
            String formatted = formatter.format(symbol);
            assertEquals("ID", formatted);
            assertEquals(symbol, formatter.parse(formatted));

            // Test with optional part
            Symbol symbolWithChannel = Symbol.of().type(8).channel(1).get();
            String formattedWithChannel = formatter.format(symbolWithChannel);
            assertEquals("ID:1", formattedWithChannel);
            assertEquals(symbolWithChannel, formatter.parse(formattedWithChannel));
        }

        @Test
        void testInvalidPatternThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> SymbolFormatter.ofPattern("s["));
        }
    }

    @Nested
    class AntlrFormatterTest {

        private final static SymbolFormatter ANTLR_WITH_VOCAB = SymbolFormatter.ANTLR.withVocabulary(VOCABULARY);
        private final static SymbolFormatter ANTLR_WITHOUT_VOCAB = SymbolFormatter.ANTLR;

        @Test
        void formatsSymbolCorrectlyWithAndWithoutVocabulary() {
            Symbol symbol = Symbol.of()
                    .type(8) // Corresponds to "ID" in the vocabulary
                    .text("testId")
                    .index(0)
                    .line(1)
                    .position(0)
                    .start(0)
                    .stop(5)
                    .get();

            // Test with vocabulary
            String expectedWithVocab = "[@0,0:5='testId',<ID>,1:0]";
            assertEquals(expectedWithVocab, ANTLR_WITH_VOCAB.format(symbol));

            // Test without vocabulary
            String expectedWithoutVocab = "[@0,0:5='testId',<8>,1:0]";
            assertEquals(expectedWithoutVocab, ANTLR_WITHOUT_VOCAB.format(symbol));
        }

        @Test
        void formatsSymbolWithCustomChannel() {
            // Symbol with a custom channel
            Symbol customChannelSymbol = Symbol.of()
                    .type(8)
                    .text("custom")
                    .index(1)
                    .channel(1) // Custom channel
                    .line(2)
                    .position(0)
                    .start(6)
                    .stop(11)
                    .get();

            // Symbol with default channel
            Symbol defaultChannelSymbol = Symbol.of()
                    .type(9)
                    .text("123")
                    .index(2)
                    .channel(0) // Default channel
                    .line(3)
                    .position(0)
                    .start(12)
                    .stop(14)
                    .get();

            // Test custom channel with vocabulary
            String expectedCustomChannelWithVocab = "[@1,6:11='custom',<ID>,channel=1,2:0]";
            assertEquals(expectedCustomChannelWithVocab, ANTLR_WITH_VOCAB.format(customChannelSymbol));

            // Test default channel with vocabulary (should not show channel)
            String expectedDefaultChannelWithVocab = "[@2,12:14='123',<INT>,3:0]";
            assertEquals(expectedDefaultChannelWithVocab, ANTLR_WITH_VOCAB.format(defaultChannelSymbol));

            // Test custom channel without vocabulary
            String expectedCustomChannelWithoutVocab = "[@1,6:11='custom',<8>,channel=1,2:0]";
            assertEquals(expectedCustomChannelWithoutVocab, ANTLR_WITHOUT_VOCAB.format(customChannelSymbol));

            // Test default channel without vocabulary (should not show channel)
            String expectedDefaultChannelWithoutVocab = "[@2,12:14='123',<9>,3:0]";
            assertEquals(expectedDefaultChannelWithoutVocab, ANTLR_WITHOUT_VOCAB.format(defaultChannelSymbol));
        }

        @Test
        void testFormatterToString() {
            assertEquals("\\[@N,B:E=\\'X\\',<L>[',channel='c],R:P\\]", ANTLR_WITH_VOCAB.toString());
        }

        @Test
        void parsesSymbolWithDefaultChannelWhenNotSpecified() {
            String input = "[@0,0:5='testId',<ID>,1:0]";
            Symbol parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Symbol expectedSymbol = Symbol.of()
                    .type(8) // ID
                    .text("testId")
                    .index(0)
                    .line(1)
                    .position(0)
                    .start(0)
                    .stop(5)
                    .channel(0) // Default channel
                    .get();

            assertEquals(expectedSymbol, parsedSymbol);
        }

        @Test
        void parsesSymbolWithExplicitDefaultChannel() {
            String input = "[@0,0:5='testId',<ID>,channel=0,1:0]";
            Symbol parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Symbol expectedSymbol = Symbol.of()
                    .type(8) // ID
                    .text("testId")
                    .index(0)
                    .line(1)
                    .position(0)
                    .start(0)
                    .stop(5)
                    .channel(0) // Explicitly default channel
                    .get();

            assertEquals(expectedSymbol, parsedSymbol);
        }

        @Test
        void parsesSymbolWithCustomChannel() {
            String input = "[@1,6:11='custom',<ID>,channel=1,2:0]";
            Symbol parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Symbol expectedSymbol = Symbol.of()
                    .type(8) // ID
                    .text("custom")
                    .index(1)
                    .line(2)
                    .position(0)
                    .start(6)
                    .stop(11)
                    .channel(1) // Custom channel
                    .get();

            assertEquals(expectedSymbol, parsedSymbol);
        }
    }

    @Nested
    class SimpleFormatterTest {
        private static Vocabulary createVocabulary() {
            String[] literalNames = new String[]{
                    null, "'='", "'*'", "'/'", "'+'", "'-'", "'('", "')'"
            };
            String[] symbolicNames =
                    new String[]{
                            null, null, null, null, null, null, null, null, "ID", "INT", "NEWLINE",
                            "WS"
                    };
            return new VocabularyImpl(literalNames, symbolicNames);
        }

        private final static Vocabulary VOCABULARY = createVocabulary();
        private final static SymbolFormatter SIMPLE_WITH_VOCAB = SymbolFormatter.SIMPLE.withVocabulary(
                VOCABULARY);
        private final static SymbolFormatter SIMPLE_WITHOUT_VOCAB = SymbolFormatter.SIMPLE;

        @Test
        void formatsEOFCorrectly() {
            Symbol eofSymbol = Symbol.of().type(Symbol.EOF).get();
            assertEquals("EOF", SIMPLE_WITH_VOCAB.format(eofSymbol));
            assertEquals("EOF", SIMPLE_WITHOUT_VOCAB.format(eofSymbol));
        }

        @Test
        void formatsLiteralCorrectlyWithAndWithoutVocabulary() {
            // Symbol for '+'
            Symbol plusSymbol = Symbol.of()
                    .type(4) // Corresponds to '+' in the vocabulary
                    .text("+")
                    .get();

            // Test with vocabulary
            assertEquals("'+'", SIMPLE_WITH_VOCAB.format(plusSymbol));

            // Test without vocabulary (should fall back to symbolic representation)
            assertEquals("(4 '+')", SIMPLE_WITHOUT_VOCAB.format(plusSymbol));
        }

        @Test
        void formatsSymbolicCorrectlyWithAndWithoutVocabulary() {
            // Symbol for ID "testId"
            Symbol idSymbol = Symbol.of()
                    .type(8) // Corresponds to "ID" in the vocabulary
                    .text("testId")
                    .get();

            // Test with vocabulary
            assertEquals("(ID 'testId')", SIMPLE_WITH_VOCAB.format(idSymbol));

            // Test without vocabulary
            assertEquals("(8 'testId')", SIMPLE_WITHOUT_VOCAB.format(idSymbol));
        }

        @Test
        void formatsSymbolWithCustomChannel() {
            // Literal symbol with custom channel
            Symbol plusSymbolWithChannel = Symbol.of()
                    .type(4) // '+'
                    .text("+")
                    .channel(1)
                    .get();

            // Symbolic symbol with custom channel
            Symbol idSymbolWithChannel = Symbol.of()
                    .type(8) // ID
                    .text("custom")
                    .channel(1)
                    .get();

            // Test literal with custom channel and vocabulary
            assertEquals("'+':1", SIMPLE_WITH_VOCAB.format(plusSymbolWithChannel));
            // Test literal with custom channel without vocabulary
            assertEquals("(4:1 '+')", SIMPLE_WITHOUT_VOCAB.format(plusSymbolWithChannel));

            // Test symbolic with custom channel and vocabulary
            assertEquals("(ID:1 'custom')", SIMPLE_WITH_VOCAB.format(idSymbolWithChannel));
            // Test symbolic with custom channel without vocabulary
            assertEquals("(8:1 'custom')", SIMPLE_WITHOUT_VOCAB.format(idSymbolWithChannel));

            // Test literal with default channel (should not show channel)
            Symbol plusSymbolDefaultChannel = Symbol.of()
                    .type(4)
                    .text("+")
                    .channel(0)
                    .get();
            assertEquals("'+'", SIMPLE_WITH_VOCAB.format(plusSymbolDefaultChannel));
            assertEquals("(4 '+')", SIMPLE_WITHOUT_VOCAB.format(plusSymbolDefaultChannel));

            // Test symbolic with default channel (should not show channel)
            Symbol idSymbolDefaultChannel = Symbol.of()
                    .type(8)
                    .text("default")
                    .channel(0)
                    .get();
            assertEquals("(ID 'default')", SIMPLE_WITH_VOCAB.format(idSymbolDefaultChannel));
            assertEquals("(8 'default')", SIMPLE_WITHOUT_VOCAB.format(idSymbolDefaultChannel));
        }

        @Test
        void testFormatterToString() {
            // The toString for SIMPLE should represent the alternatives
            String expectedToString = "<EOF> | l[:c] | (S[:c] \\'X\\')";
            assertEquals(expectedToString, SIMPLE_WITH_VOCAB.toString());
        }

        @Test
        void parsesEOFCorrectly() {
            String input = "EOF";
            Symbol parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Symbol expectedSymbol = Symbol.of().type(Symbol.EOF).text("<EOF>").get();
            assertEquals(expectedSymbol, parsedSymbol);
        }

        @Test
        void parsesLiteralCorrectly() {
            String input = "'+'";
            Symbol parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Symbol expectedSymbol = Symbol.of()
                    .type(4) // '+'
                    .text("+")
                    .channel(0)
                    .get();
            assertEquals(expectedSymbol, parsedSymbol);

            input = "'-':1";
            parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            expectedSymbol = Symbol.of()
                    .type(5) // '-'
                    .text("-")
                    .channel(1)
                    .get();
            assertEquals(expectedSymbol, parsedSymbol);
        }

        @Test
        void parsesSymbolicCorrectly() {
            String input = "(ID 'myVar')";
            Symbol parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Symbol expectedSymbol = Symbol.of()
                    .type(8) // ID
                    .text("myVar")
                    .channel(0)
                    .get();
            assertEquals(expectedSymbol, parsedSymbol);

            input = "(INT:2 '123')";
            parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            expectedSymbol = Symbol.of()
                    .type(9) // INT
                    .text("123")
                    .channel(2)
                    .get();
            assertEquals(expectedSymbol, parsedSymbol);
        }

        @Test
        void parsesSymbolWithCustomChannel() {
            String inputLiteral = "'+':5";
            Symbol parsedLiteral = SIMPLE_WITH_VOCAB.parse(inputLiteral);
            Symbol expectedLiteral = Symbol.of().type(4).text("+").channel(5).get();
            assertEquals(expectedLiteral, parsedLiteral);

            String inputSymbolic = "(ID:10 'variable')";
            Symbol parsedSymbolic = SIMPLE_WITH_VOCAB.parse(inputSymbolic);
            Symbol expectedSymbolic = Symbol.of().type(8).text("variable").channel(10).get();
            assertEquals(expectedSymbolic, parsedSymbolic);
        }

        @Test
        void parsesSymbolWithExplicitDefaultChannel() {
            // For SIMPLE, explicit default channel is formatted as no channel part,
            // so parsing "(ID 'text')" should result in channel 0.
            String input = "(ID 'test')";
            Symbol parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Symbol expectedSymbol = Symbol.of()
                    .type(8) // ID
                    .text("test")
                    .channel(0) // Default channel
                    .get();
            assertEquals(expectedSymbol, parsedSymbol);

            // For literal, "+":0 would not be formatted as such, just "+", so no explicit test needed beyond parsesLiteralCorrectly
        }
    }
}