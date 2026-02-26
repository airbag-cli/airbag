package io.github.airbag.token;

import io.github.airbag.Airbag;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.airbag.Airbag.assertToken;
import static org.junit.jupiter.api.Assertions.*;

public class TokenFormatterTest {

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

        private final TokenFormatter formatter = TokenFormatter.ofPattern("s").withVocabulary(VOCABULARY);

        @Test
        void testSuccessfulLenientParse() {
            FormatterParsePosition position = new FormatterParsePosition(0);
            String input = "ID and more";
            Token expected = new TokenBuilder().type(8).get();

            Token parsed = formatter.parse(input, position);

            Airbag.assertToken(expected, parsed, TokenField.all());
            assertEquals(2, position.getIndex()); // Position after 'ID'
            assertEquals(-1, position.getErrorIndex());
        }

        @Test
        void testFailingLenientParse() {
            FormatterParsePosition position = new FormatterParsePosition(0);
            String input = "123 and more"; // '123' is not a symbolic name

            Token parsed = formatter.parse(input, position);

            assertNull(parsed);
            assertEquals(0, position.getIndex()); // Position is unchanged
            assertEquals(0, position.getErrorIndex()); // Error at the getStartIndex
        }

        @Test
        void testParseMultipleSymbols() {
            TokenFormatter multiFormatter = TokenFormatter.ofPattern("s \\'x\\'").withVocabulary(VOCABULARY);
            String input = "ID 'one' INT 'two'";
            FormatterParsePosition position = new FormatterParsePosition(0);

            // Parse "ID one"
            Token s1 = multiFormatter.parse(input, position);
            assertTrue(position.getMessage().isEmpty());
            assertEquals(8, position.getIndex());
            Airbag.assertToken(new TokenBuilder().type(8).text("one").get(), s1, TokenField.all());

            // Advance past space
            position.setIndex(position.getIndex() + 1);

            // Parse "INT two"
            Token s2 = multiFormatter.parse(input, position);
            Airbag.assertToken(new TokenBuilder().type(9).text("two").get(), s2, TokenField.all());
            assertEquals(18, position.getIndex());
        }
    }

    @Nested
    class ExceptionHandlingTest {


        @Test
        void formatShouldThrowExceptionOnFailure() {
            // Strict symbolic formatter 's' requires a symbolic name.
            TokenFormatter formatter = TokenFormatter.ofPattern("s").withVocabulary(VOCABULARY);
            // Type 1 has a literal name ('=') but no symbolic name.
            Token symbol = new TokenBuilder().type(1).text("=").get();
            assertThrows(TokenFormatterException.class, () -> formatter.format(symbol));
        }

        @Test
        void parseShouldThrowExceptionOnMalformedInput() {
            TokenFormatter formatter = TokenFormatter.ofPattern("s:'x'").withVocabulary(VOCABULARY);
            String malformedInput = "ID'myId'"; // Missing colon
            assertThrows(TokenParseException.class, () -> formatter.parse(malformedInput));
        }

        @Test
        void parseShouldThrowExceptionOnTrailingInput() {
            TokenFormatter formatter = TokenFormatter.ofPattern("s").withVocabulary(VOCABULARY);
            String inputWithTrailingChars = "ID trailing";
            assertThrows(TokenParseException.class, () -> formatter.parse(inputWithTrailingChars));
        }

        @Test
        void parseShouldThrowExceptionOnNullInput() {
            assertThrows(NullPointerException.class, () -> TokenFormatter.ANTLR.parse(null));
        }
    }

    @Nested
    class OfPatternFormatterTest {

        @Test
        void testSimplePattern() {
            TokenFormatter formatter = TokenFormatter.ofPattern("s:\\'x\\'").withVocabulary(VOCABULARY);
            Token symbol = new TokenBuilder().type(8).text("myId").get(); // ID
            String formatted = formatter.format(symbol);
            assertEquals("ID:'myId'", formatted);

            Token parsed = formatter.parse(formatted);
            Airbag.assertToken(symbol, parsed, TokenField.all());
        }

        @Test
        void testPatternWithAlternatives() {
            TokenFormatter formatter = TokenFormatter.ofPattern("l|s").withVocabulary(VOCABULARY);

            // Test literal part
            Token literalSymbol = new TokenBuilder().type(4).text("+").get(); // '+'
            String formattedLiteral = formatter.format(literalSymbol);
            assertEquals("'+'", formattedLiteral);
            Airbag.assertToken(literalSymbol, formatter.parse(formattedLiteral), TokenField.all());

            // Test symbolic part
            Token symbolicSymbol = new TokenBuilder().type(8).get(); // ID
            String formattedSymbolic = formatter.format(symbolicSymbol);
            assertEquals("ID", formattedSymbolic);
            Airbag.assertToken(symbolicSymbol, formatter.parse(formattedSymbolic), TokenField.all());
        }

        @Test
        void testPatternWithOptionalSection() {
            TokenFormatter formatter = TokenFormatter.ofPattern("s[:c]").withVocabulary(VOCABULARY);

            // Test without optional part
            Token symbol = new TokenBuilder().type(8).channel(0).get(); // ID on default getChannel
            String formatted = formatter.format(symbol);
            assertEquals("ID", formatted);
            Airbag.assertToken(symbol, formatter.parse(formatted), TokenField.all());

            // Test with optional part
            Token symbolWithChannel = new TokenBuilder().type(8).channel(1).get();
            String formattedWithChannel = formatter.format(symbolWithChannel);
            assertEquals("ID:1", formattedWithChannel);
            Airbag.assertToken(symbolWithChannel, formatter.parse(formattedWithChannel), TokenField.all());
        }

        @Test
        void testInvalidPatternThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> TokenFormatter.ofPattern("s["));
        }
    }

    @Nested
    class AntlrFormatterTest {

        private final static TokenFormatter ANTLR_WITH_VOCAB = TokenFormatter.ANTLR.withVocabulary(VOCABULARY);
        private final static TokenFormatter ANTLR_WITHOUT_VOCAB = TokenFormatter.ANTLR;

        @Test
        void formatsSymbolCorrectlyWithAndWithoutVocabulary() {
            Token symbol = new TokenBuilder()
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
            // Token with a custom getChannel
            Token customChannelSymbol = new TokenBuilder()
                    .type(8)
                    .text("custom")
                    .index(1)
                    .channel(1) // Custom getChannel
                    .line(2)
                    .position(0)
                    .start(6)
                    .stop(11)
                    .get();

            // Token with default getChannel
            Token defaultChannelSymbol = new TokenBuilder()
                    .type(9)
                    .text("123")
                    .index(2)
                    .channel(0) // Default getChannel
                    .line(3)
                    .position(0)
                    .start(12)
                    .stop(14)
                    .get();

            // Test custom getChannel with vocabulary
            String expectedCustomChannelWithVocab = "[@1,6:11='custom',<ID>,getChannel=1,2:0]";
            assertEquals(expectedCustomChannelWithVocab, ANTLR_WITH_VOCAB.format(customChannelSymbol));

            // Test default getChannel with vocabulary (should not show getChannel)
            String expectedDefaultChannelWithVocab = "[@2,12:14='123',<INT>,3:0]";
            assertEquals(expectedDefaultChannelWithVocab, ANTLR_WITH_VOCAB.format(defaultChannelSymbol));

            // Test custom getChannel without vocabulary
            String expectedCustomChannelWithoutVocab = "[@1,6:11='custom',<8>,getChannel=1,2:0]";
            assertEquals(expectedCustomChannelWithoutVocab, ANTLR_WITHOUT_VOCAB.format(customChannelSymbol));

            // Test default getChannel without vocabulary (should not show getChannel)
            String expectedDefaultChannelWithoutVocab = "[@2,12:14='123',<9>,3:0]";
            assertEquals(expectedDefaultChannelWithoutVocab, ANTLR_WITHOUT_VOCAB.format(defaultChannelSymbol));
        }

        @Test
        void testFormatterToString() {
            assertEquals("\\[@N,B:E=\\'X\\',<L>[',getChannel='c],R:P\\]", ANTLR_WITH_VOCAB.toString());
        }

        @Test
        void parsesSymbolWithDefaultChannelWhenNotSpecified() {
            String input = "[@0,0:5='testId',<ID>,1:0]";
            Token parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Token expectedSymbol = new TokenBuilder()
                    .type(8) // ID
                    .text("testId")
                    .index(0)
                    .line(1)
                    .position(0)
                    .start(0)
                    .stop(5)
                    .channel(0) // Default getChannel
                    .get();

            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
        }

        @Test
        void parsesSymbolWithExplicitDefaultChannel() {
            String input = "[@0,0:5='testId',<ID>,getChannel=0,1:0]";
            Token parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Token expectedSymbol = new TokenBuilder()
                    .type(8) // ID
                    .text("testId")
                    .index(0)
                    .line(1)
                    .position(0)
                    .start(0)
                    .stop(5)
                    .channel(0) // Explicitly default getChannel
                    .get();

            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
        }

        @Test
        void parsesSymbolWithCustomChannel() {
            String input = "[@1,6:11='custom',<ID>,getChannel=1,2:0]";
            Token parsedSymbol = ANTLR_WITH_VOCAB.parse(input);

            Token expectedSymbol = new TokenBuilder()
                    .type(8) // ID
                    .text("custom")
                    .index(1)
                    .line(2)
                    .position(0)
                    .start(6)
                    .stop(11)
                    .channel(1) // Custom getChannel
                    .get();

            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
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
        private final static TokenFormatter SIMPLE_WITH_VOCAB = TokenFormatter.SIMPLE.withVocabulary(
                VOCABULARY);
        private final static TokenFormatter SIMPLE_WITHOUT_VOCAB = TokenFormatter.SIMPLE;

        @Test
        void formatsEOFCorrectly() {
            Token eofSymbol = new TokenBuilder().type(Token.EOF).get();
            assertEquals("EOF", SIMPLE_WITH_VOCAB.format(eofSymbol));
            assertEquals("EOF", SIMPLE_WITHOUT_VOCAB.format(eofSymbol));
        }

        @Test
        void formatsLiteralCorrectlyWithAndWithoutVocabulary() {
            // Token for '+'
            Token plusSymbol = new TokenBuilder()
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
            // Token for ID "testId"
            Token idSymbol = new TokenBuilder()
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
            // Literal symbol with custom getChannel
            Token plusSymbolWithChannel = new TokenBuilder()
                    .type(4) // '+'
                    .text("+")
                    .channel(1)
                    .get();

            // Symbolic symbol with custom getChannel
            Token idSymbolWithChannel = new TokenBuilder()
                    .type(8) // ID
                    .text("custom")
                    .channel(1)
                    .get();

            // Test literal with custom getChannel and vocabulary
            assertEquals("'+':1", SIMPLE_WITH_VOCAB.format(plusSymbolWithChannel));
            // Test literal with custom getChannel without vocabulary
            assertEquals("(4:1 '+')", SIMPLE_WITHOUT_VOCAB.format(plusSymbolWithChannel));

            // Test symbolic with custom getChannel and vocabulary
            assertEquals("(ID:1 'custom')", SIMPLE_WITH_VOCAB.format(idSymbolWithChannel));
            // Test symbolic with custom getChannel without vocabulary
            assertEquals("(8:1 'custom')", SIMPLE_WITHOUT_VOCAB.format(idSymbolWithChannel));

            // Test literal with default getChannel (should not show getChannel)
            Token plusSymbolDefaultChannel = new TokenBuilder()
                    .type(4)
                    .text("+")
                    .channel(0)
                    .get();
            assertEquals("'+'", SIMPLE_WITH_VOCAB.format(plusSymbolDefaultChannel));
            assertEquals("(4 '+')", SIMPLE_WITHOUT_VOCAB.format(plusSymbolDefaultChannel));

            // Test symbolic with default getChannel (should not show getChannel)
            Token idSymbolDefaultChannel = new TokenBuilder()
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
            Token parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Token expectedSymbol = new TokenBuilder().type(Token.EOF).text("<EOF>").get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
        }

        @Test
        void parsesLiteralCorrectly() {
            String input = "'+'";
            Token parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Token expectedSymbol = new TokenBuilder()
                    .type(4) // '+'
                    .text("+")
                    .channel(0)
                    .get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());

            input = "'-':1";
            parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            expectedSymbol = new TokenBuilder()
                    .type(5) // '-'
                    .text("-")
                    .channel(1)
                    .get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
        }

        @Test
        void parsesSymbolicCorrectly() {
            String input = "(ID 'myVar')";
            Token parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Token expectedSymbol = new TokenBuilder()
                    .type(8) // ID
                    .text("myVar")
                    .channel(0)
                    .get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());

            input = "(INT:2 '123')";
            parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            expectedSymbol = new TokenBuilder()
                    .type(9) // INT
                    .text("123")
                    .channel(2)
                    .get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());
        }

        @Test
        void parsesSymbolWithCustomChannel() {
            String inputLiteral = "'+':5";
            Token parsedLiteral = SIMPLE_WITH_VOCAB.parse(inputLiteral);
            Token expectedLiteral = new TokenBuilder().type(4).text("+").channel(5).get();
            Airbag.assertToken(expectedLiteral, parsedLiteral, TokenField.all());

            String inputSymbolic = "(ID:10 'variable')";
            Token parsedSymbolic = SIMPLE_WITH_VOCAB.parse(inputSymbolic);
            Token expectedSymbolic = new TokenBuilder().type(8).text("variable").channel(10).get();
            Airbag.assertToken(expectedSymbolic, parsedSymbolic, TokenField.all());
        }

        @Test
        void parsesSymbolWithExplicitDefaultChannel() {
            // For SIMPLE, explicit default getChannel is formatted as no getChannel part,
            // so parsing "(ID 'getText')" should result in getChannel 0.
            String input = "(ID 'test')";
            Token parsedSymbol = SIMPLE_WITH_VOCAB.parse(input);
            Token expectedSymbol = new TokenBuilder()
                    .type(8) // ID
                    .text("test")
                    .channel(0) // Default getChannel
                    .get();
            Airbag.assertToken(expectedSymbol, parsedSymbol, TokenField.all());

            // For literal, "+":0 would not be formatted as such, just "+", so no explicit test needed beyond parsesLiteralCorrectly
        }
    }

    @Nested
    class ParseListTest {

        private final TokenFormatter formatter = TokenFormatter.SIMPLE.withVocabulary(VOCABULARY);

        private static Vocabulary createVocabularyWithoutWS() {
            String[] literalNames = new String[]{
                    null, "'='", "'*'", "'/'", "'+'", "'-'", "'('", "')'"
            };
            String[] symbolicNames =
                    new String[]{
                            null, null, null, null, null, null, null, null, "ID", "INT", "NEWLINE",
                            // No "WS"
                    };
            return new VocabularyImpl(literalNames, symbolicNames);
        }

        @Test
        void testParseListWithImplicitIndexingAndWhitespace() {
            String input = "(ID 'a') (ID 'b')";
            java.util.List<Token> symbols = formatter.parseList(input);

            assertEquals(2, symbols.size());

            Token expected1 = new TokenBuilder().type(8).text("a").channel(0).index(0).get();
            Airbag.assertToken(expected1, symbols.get(0), TokenField.all());

            Token expected2 = new TokenBuilder().type(8).text("b").channel(0).index(1).get();
            Airbag.assertToken(expected2, symbols.get(1), TokenField.all());
        }

        @Test
        void testParseListWithVariousWhitespace() {
            String input = "(ID 'a')\n\t(ID 'b')  '+'   "; // mixed symbols, with newlines, tabs, and trailing spaces
            java.util.List<Token> symbols = formatter.parseList(input);

            assertEquals(3, symbols.size());

            Token expected1 = new TokenBuilder().type(8).text("a").channel(0).index(0).get();
            Airbag.assertToken(expected1, symbols.get(0), TokenField.all());

            Token expected2 = new TokenBuilder().type(8).text("b").channel(0).index(1).get();
            Airbag.assertToken(expected2, symbols.get(1), TokenField.all());

            Token expected3 = new TokenBuilder().type(4).text("+").channel(0).index(2).get();
            Airbag.assertToken(expected3, symbols.get(2), TokenField.all());
        }

        @Test
        void testParseListWithoutWhitespaceSeparators() {
            String input = "(ID 'a')(ID 'b')";
            java.util.List<Token> symbols = formatter.parseList(input);

            assertEquals(2, symbols.size());
            Token expected1 = new TokenBuilder().type(8).text("a").channel(0).index(0).get();
            Airbag.assertToken(expected1, symbols.get(0), TokenField.all());

            Token expected2 = new TokenBuilder().type(8).text("b").channel(0).index(1).get();
            Airbag.assertToken(expected2, symbols.get(1), TokenField.all());
        }

        @Test
        void testParseEmptyList() {
            String input = "   ";
            java.util.List<Token> symbols = formatter.parseList(input);
            assertTrue(symbols.isEmpty());

            input = "";
            symbols = formatter.parseList(input);
            assertTrue(symbols.isEmpty());
        }

        @Test
        void testParseListFailsWithWhitespaceWhenNotIgnored() {
            TokenFormatter formatterWithoutWS = TokenFormatter.SIMPLE.withVocabulary(createVocabularyWithoutWS());
            String input = "(ID 'a') (ID 'b')";

            // The space between symbols is not a recognized whitespace token if the vocabulary does not contain 'WS'.
            // Therefore, parsing should fail after the first symbol due to extraneous input.
            TokenParseException e = assertThrows(TokenParseException.class, () -> formatterWithoutWS.parseList(input, false));
            assertEquals("""
                            Parse failed at index 8:
                            Expected 'EOF' but found ' (I'
                            Expected literal '(' but found ' '
                            Unrecognized literal type name starting with ' (ID '
                            
                            (ID 'a')>> (ID 'b')
                            """, e.getMessage());
        }
    }

    @Nested
    class JsonFormatterTest {

        Token symbol = new TokenBuilder()
                .type(8) // Corresponds to "ID" in the vocabulary
                .text("testId")
                .index(0)
                .line(1)
                .position(0)
                .start(0)
                .stop(5)
                .get();

        TokenFormatter JSON = TokenFormatter.JSON;

        @Test
        void testFormatToken() {
            assertEquals("""
                    {
                        "type" : "8",
                        "text" : "testId",
                        "index" : "0",
                        "line" : "1",
                        "charPositionInLine" : "0",
                        "startIndex" : "0",
                        "stopIndex" : "5"
                    }""", JSON.format(symbol));
        }

        @Test
        void testFormatTokenWithVocab() {
            assertEquals("""
                    {
                        "type" : "8",
                        "text" : "testId",
                        "symbolicName" : "ID",
                        "index" : "0",
                        "line" : "1",
                        "charPositionInLine" : "0",
                        "startIndex" : "0",
                        "stopIndex" : "5"
                    }""", JSON.withVocabulary(VOCABULARY).format(symbol));
        }

    }

    @Nested
    class XMLFormatterTest {

        Token symbol = new TokenBuilder()
                .type(8) // Corresponds to "ID" in the vocabulary
                .text("testId")
                .index(0)
                .line(1)
                .position(0)
                .start(0)
                .stop(5)
                .get();

        TokenFormatter XML = TokenFormatter.XML;

        @Test
        void testFormatToken() {
            assertEquals("""
                    <token>
                        <type>8</type>
                        <text>testId</text>
                        <index>0</index>
                        <line>1</line>
                        <charPositionInLine>0</charPositionInLine>
                        <startIndex>0</startIndex>
                        <stopIndex>5</stopIndex>
                    </token>
                    """, XML.format(symbol));
        }

        @Test
        void testFormatTokenWithVocab() {
            assertEquals("""
                    <token>
                        <type symbolic="ID">8</type>
                        <text>testId</text>
                        <index>0</index>
                        <line>1</line>
                        <charPositionInLine>0</charPositionInLine>
                        <startIndex>0</startIndex>
                        <stopIndex>5</stopIndex>
                    </token>
                    """, XML.withVocabulary(VOCABULARY).format(symbol));
        }
    }
}