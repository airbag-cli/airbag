package io.github.airbag.tree;

import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolFormatterBuilder;
import io.github.airbag.symbol.TypeFormat;
import io.github.airbag.tree.NodeFormatterBuilder.IntegerRulePrinterParser;
import io.github.airbag.tree.NodeFormatterBuilder.LiteralPrinterParser;
import io.github.airbag.tree.NodeFormatterBuilder.StringRuleNamePrinterParser;
import io.github.airbag.tree.pattern.PatternBuilder;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NodeFormatterBuilderTest {

    static RootParseContext createCtx() {
        return new RootParseContext(null, null, null);
    }

    static RootParseContext createCtx(SymbolFormatter formatter) {
        return new RootParseContext(formatter, null, null);
    }

    @Nested
    class LiteralPrinterParserTest {

        @Test
        void testFormat() {
            var printer = new LiteralPrinterParser("=>");
            var buf = new StringBuilder();
            assertTrue(printer.format(null, buf)); // Context is not used
            assertEquals("=>", buf.toString());
        }

        @Test
        void testFormatEmpty() {
            var printer = new LiteralPrinterParser("");
            var buf = new StringBuilder();
            assertTrue(printer.format(null, buf)); // Context is not used
            assertTrue(buf.isEmpty());
        }

        @Test
        void testParseSuccess() {
            var parser = new LiteralPrinterParser("=>");
            var ctx = createCtx();

            assertEquals(2, parser.parse(ctx, "=>123", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseFailure() {
            var parser = new LiteralPrinterParser("=>");
            var ctx = createCtx();

            assertEquals(~0, parser.parse(ctx, "->123", 0));
            assertFalse(ctx.getErrorMessages().isEmpty());
            assertEquals("Expected literal '=>' but found '->'", ctx.getErrorMessages());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseFailureWithSpecialChars() {
            var parser = new LiteralPrinterParser("a\nb");
            var ctx = createCtx();

            assertEquals(~0, parser.parse(ctx, "acb", 0));
            assertFalse(ctx.getErrorMessages().isEmpty());
            assertEquals("Expected literal 'a\\nb' but found 'acb'", ctx.getErrorMessages());
        }

        @Test
        void testParseEmptyLiteral() {
            var parser = new LiteralPrinterParser("");
            var ctx = createCtx();

            assertEquals(0, parser.parse(ctx, "abc", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());

            assertEquals(3, parser.parse(ctx, "abc", 3));
            assertTrue(ctx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseOutOfBounds() {
            var parser = new LiteralPrinterParser("=>");
            var ctx = createCtx();

            // Literal is "=>", input is only "="
            assertEquals(~0, parser.parse(ctx, "=", 0));
            assertFalse(ctx.getErrorMessages().isEmpty());
            assertEquals("Expected literal '=>' but found '='", ctx.getErrorMessages());
            assertEquals(0, ctx.getMaxError());

            // Literal is "=>", input is "a=" but position is 1, so effectively input is "="
            ctx = createCtx();
            assertEquals(~1, parser.parse(ctx, "a=", 1));
            assertFalse(ctx.getErrorMessages().isEmpty());
            assertEquals("Expected literal '=>' but found '='", ctx.getErrorMessages());
            assertEquals(1, ctx.getMaxError());
        }

        @Test
        void testParseInvalidPosition() {
            var parser = new LiteralPrinterParser("=>");
            var ctx = createCtx();

            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ctx, "=>", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ctx, "=>", 3));
        }
    }

    @Nested
    class IntegerRulePrinterParserTest {

        @Test
        void testFormatRuleNode() {
            var printer = new IntegerRulePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 123);
            var ctx = new NodeFormatContext(null, null, null);
            ctx.setNode(ruleNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("123", buf.toString());
        }

        @Test
        void testFormatPatternNode() {
            var printer = new IntegerRulePrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 456, null);
            var ctx = new NodeFormatContext(null, null, null);
            ctx.setNode(patternNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("456", buf.toString());
        }

        @Test
        void testFormatOtherNodeTypes() {
            var printer = new IntegerRulePrinterParser();
            var buf = new StringBuilder();
            var terminalNode = Node.Terminal.attachTo(null, Symbol.of().type(0).get());
            var ctx = new NodeFormatContext(null, null, null);
            ctx.setNode(terminalNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNullNode() {
            var printer = new IntegerRulePrinterParser();
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testParsePositiveNumberIntoRuleContext() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(3, parser.parse(ruleCtx, "123abc", 0));
            assertEquals(123, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseNegativeNumberIntoRuleContext() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(4, parser.parse(ruleCtx, "-123abc", 0));
            assertEquals(-123, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseZeroIntoRuleContext() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(1, parser.parse(ruleCtx, "0abc", 0));
            assertEquals(0, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseWithLeadingZerosIntoRuleContext() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(3, parser.parse(ruleCtx, "007abc", 0));
            assertEquals(7, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseIntoPatternContext() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var patternCtx = rootCtx.new Pattern(rootCtx);

            assertEquals(5, parser.parse(patternCtx, "89012def", 0));
            assertEquals(89012, patternCtx.resolve(null).index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseInvalidNumberFormat() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "abc", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Expected an integer for a rule index but found 'abc'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseEmptyString() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Expected an integer for a rule index but found '<text end>'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseInvalidPosition() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "123", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "123", 4));
        }

        @Test
        void testParseIntegerOverflow() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);
            String overflowValue = "2147483648"; // Integer.MAX_VALUE + 1

            // This should cause an error in Integer.parseInt, which will be caught in parse and recorded.
            assertEquals(~10, parser.parse(ruleCtx, overflowValue, 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("The number '2147483648' is out of range",
                    rootCtx.getErrorMessages()); // Standard NumberFormatException message
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseIntegerUnderflow() {
            var parser = new IntegerRulePrinterParser();
            var rootCtx = createCtx();
            var ruleCtx = rootCtx.new Rule(rootCtx);
            String underflowValue = "-2147483649"; // Integer.MIN_VALUE - 1

            // This should cause an error in Integer.parseInt, which will be caught in parse and recorded.
            assertEquals(~11, parser.parse(ruleCtx, underflowValue, 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("The number '-2147483649' is out of range",
                    rootCtx.getErrorMessages()); // Standard NumberFormatException message
            assertEquals(0, rootCtx.getMaxError());
        }
    }

    @Nested
    class StringRuleNamePrinterParserTest {

        private final Recognizer<?, ?> PARSER = new ExpressionParser(null);

        @Test
        void testFormatRuleNodeWithValidIndex() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 1); // Index for "ruleB"
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(ruleNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("stat", buf.toString());
        }

        @Test
        void testFormatPatternNodeWithValidIndex() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 0, null); // Index for "ruleA"
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(patternNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("prog", buf.toString());
        }

        @Test
        void testFormatRuleNodeWithRecognizerNull() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 1);
            var ctx = new NodeFormatContext(null, null, null); // Null recognizer
            ctx.setNode(ruleNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatRuleNodeWithIndexOutOfBounds() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 99); // Out of bounds index
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(ruleNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatOtherNodeTypes() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var terminalNode = Node.Terminal.attachTo(null, Symbol.of().type(0).get());
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(terminalNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNullNode() {
            var printer = new StringRuleNamePrinterParser();
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, PARSER);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testParseRuleNameIntoRuleContextSuccess() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(4, parser.parse(ruleCtx, "exprdef", 0));
            assertEquals(2, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseRuleNameIntoPatternContextSuccess() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var patternCtx = rootCtx.new Pattern(rootCtx);

            assertEquals(4, parser.parse(patternCtx, "progtest", 0));
            assertEquals(0, patternCtx.resolve(null).index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseUnrecognizedRuleName() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "unknownRule", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Unrecognized rule name starting with 'unkno'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseWithRecognizerNull() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, null); // Null recognizer
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "ruleA", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Unrecognized rule name starting with 'ruleA'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseEmptyString() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Unrecognized rule name starting with '<text end>'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParsePartialRuleName() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "rule", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("Unrecognized rule name starting with 'rule'", rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseInvalidPosition() {
            var parser = new StringRuleNamePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "ruleA", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "ruleA", 6));
        }
    }

    @Nested
    class SymbolPrinterParserTest {

        private final SymbolFormatter SYMBOL_FORMATTER = SymbolFormatter.SIMPLE;
        private final SymbolFormatter CUSTOM_FIXED_TEXT_FORMATTER = new SymbolFormatterBuilder().appendLiteral(
                "FixedText").toFormatter();
        private final SymbolFormatter CUSTOM_TYPE_ONLY_FORMATTER = new SymbolFormatterBuilder().appendType(
                TypeFormat.SYMBOLIC_FIRST).toFormatter();

        @Test
        void testFormatTerminalNode() {
            var symbol = Symbol.of().type(1).text("hello").get();
            var printer = new NodeFormatterBuilder.SymbolPrinterParser();
            var buf = new StringBuilder();

            var terminalNode = Node.Terminal.attachTo(null, symbol);
            var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE, null, null);
            ctx.setNode(terminalNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("(1 'hello')", buf.toString());
        }

        @Test
        void testFormatErrorNode() {
            var symbol = Symbol.of().type(2).text("error_token").get();
            var printer = new NodeFormatterBuilder.SymbolPrinterParser();
            var buf = new StringBuilder();

            var errorNode = Node.Error.attachTo(null, symbol);
            var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE, null, null);
            ctx.setNode(errorNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("(2 'error_token')", buf.toString());
        }

        @Test
        void testFormatRuleNodeReturnsFalse() {
            var printer = new NodeFormatterBuilder.SymbolPrinterParser();
            var buf = new StringBuilder();

            var ruleNode = Node.Rule.attachTo(null, 0);
            var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE, null, null);
            ctx.setNode(ruleNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNullNodeReturnsFalse() {
            var printer = new NodeFormatterBuilder.SymbolPrinterParser();
            var buf = new StringBuilder();

            var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE, null, null);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatWithNullSymbolFormatterThrowsNPE() {
            var printer = new NodeFormatterBuilder.SymbolPrinterParser();
            var buf = new StringBuilder();

            var symbol = Symbol.of().type(0).text("T").get();
            // No symbol formatter in context
            var ctx = new NodeFormatContext(null, null, null);
            ctx.setNode(Node.Terminal.attachTo(null, symbol));

            assertThrows(NullPointerException.class, () -> printer.format(ctx, buf));
        }

        @Test
        void testParseSuccessIntoTerminalContext() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            // SIMPLE formatter formats as "(type 'text')" or "'literal'" or "EOF"
            // For parsing, it will try alternatives.
            // Example: "('literal_text')"
            String input = "(4 'parsed_text')";
            int parsedLength = input.length();

            assertEquals(parsedLength, parser.parse(terminalCtx, input + "remainder", 0));
            // Check the parsed symbol's text. Type will be -1 (EOF or default for literals without explicit type)
            DerivationTree.Terminal terminalNode = assertInstanceOf(DerivationTree.Terminal.class,
                    terminalCtx.resolve(null));
            assertEquals("parsed_text", terminalNode.symbol().text());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseSuccessLiteralIntoTerminalContext() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            String[] literals = new String[]{null, "'a_literal'"};
            var rootCtx = createCtx(SymbolFormatter.SIMPLE.withVocabulary(new VocabularyImpl(
                    literals,
                    null)));
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            // Literal format: "'literal'"
            String input = "'a_literal'";
            int parsedLength = input.length();

            assertEquals(parsedLength,
                    parser.parse(terminalCtx, input + "remainder", 0),
                    terminalCtx.root().getErrorMessages());
            DerivationTree.Terminal terminalNode = assertInstanceOf(DerivationTree.Terminal.class,
                    terminalCtx.resolve(null));
            assertEquals("a_literal", terminalNode.symbol().text());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseSuccessEOFIntoTerminalContext() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            String input = "EOF";
            int parsedLength = input.length();

            assertEquals(parsedLength, parser.parse(terminalCtx, input + "remainder", 0));
            DerivationTree.Terminal terminalNode = assertInstanceOf(DerivationTree.Terminal.class,
                    terminalCtx.resolve(null));
            Symbol symbol = terminalNode.symbol();
            assertEquals(Symbol.EOF, symbol.type());
            assertEquals("<EOF>", symbol.text()); // Text will be "EOF" for EOF symbol
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseSuccessIntoErrorContext() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var errorCtx = rootCtx.new Error(rootCtx);

            String input = "(-5 'error_text')";
            int parsedLength = input.length();

            assertEquals(parsedLength, parser.parse(errorCtx, input + "remainder", 0));
            DerivationTree.Error errorNode = assertInstanceOf(DerivationTree.Error.class,
                    errorCtx.resolve(null));
            assertEquals("error_text", errorNode.symbol().text());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseFailureReportsErrorWithCustomFormatter() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            // Input does not match "FixedText" pattern
            assertEquals(~0, parser.parse(terminalCtx, "some invalid input", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("""
                    Expected 'EOF' but found 'som'
                    Expected literal '(' but found 's'
                    No vocabulary set""", rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseWithNullSymbolFormatterThrowsNPE() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx();
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            assertEquals(~0, parser.parse(terminalCtx, "input", 0));
            assertEquals("No symbol formatter set.", rootCtx.getErrorMessages());
        }

        @Test
        void testParseWithWrongContextTypeThrowsRuntimeException() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var ruleCtx = rootCtx.new Rule(rootCtx); // Incorrect context type

            assertThrows(RuntimeException.class,
                    () -> parser.parse(ruleCtx, "(4 'text')", 0),
                    "Wrong context type");
        }

        @Test
        void testParseInvalidPositionThrowsException() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx();
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            assertThrows(IndexOutOfBoundsException.class,
                    () -> parser.parse(terminalCtx, "('text')", -1));
            assertThrows(IndexOutOfBoundsException.class,
                    () -> parser.parse(terminalCtx, "('text')", "('text')".length() + 1));
        }

        @Test
        void testParseEmptyStringFailureWithCustomTypeFormatter() {
            var parser = new NodeFormatterBuilder.SymbolPrinterParser();
            var rootCtx = createCtx(SymbolFormatter.SIMPLE);
            var terminalCtx = rootCtx.new Terminal(rootCtx);

            assertEquals(~0, parser.parse(terminalCtx, "", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals("""
                    Expected 'EOF' but found '<text end>'
                    Expected literal '(' but found '<text end>'
                    No vocabulary set""", rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }
    }

    @Nested
    class RulePrinterParserTest {

        private final Recognizer<?, ?> PARSER = new ExpressionParser(null);

        @Test
        void testFormatRuleNodePreferringStringName() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 1); // Index for "ruleB"
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(ruleNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("stat", buf.toString());
        }

        @Test
        void testFormatRuleNodeFallsBackToInteger() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 99); // Index out of bounds for MOCK_RECOGNIZER
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(ruleNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("99", buf.toString());
        }

        @Test
        void testFormatRuleNodeWithNullRecognizerFallsBackToInteger() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 2);
            var ctx = new NodeFormatContext(null, null, null); // Null recognizer
            ctx.setNode(ruleNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("2", buf.toString());
        }

        @Test
        void testFormatPatternNodePreferringStringName() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 0, null); // Index for "ruleA"
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(patternNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("prog", buf.toString());
        }

        @Test
        void testFormatPatternNodeFallsBackToInteger() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 100, null); // Index out of bounds
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(patternNode);

            assertTrue(printer.format(ctx, buf));
            assertEquals("100", buf.toString());
        }

        @Test
        void testFormatOtherNodeTypesReturnsFalse() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var terminalNode = Node.Terminal.attachTo(null, Symbol.of().type(0).get());
            var ctx = new NodeFormatContext(null, null, PARSER);
            ctx.setNode(terminalNode);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatNullNodeReturnsFalse() {
            var printer = new NodeFormatterBuilder.RulePrinterParser();
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, PARSER);

            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testParseRuleNamePreferringStringName() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(4, parser.parse(ruleCtx, "exprxyz", 0));
            assertEquals(2, ruleCtx.index());
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseRuleNameWithNullRecognizerFallsBackToInteger() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, null); // Null recognizer
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(2, parser.parse(ruleCtx, "42abc", 0));
            assertEquals(42, ruleCtx.index());
        }

        @Test
        void testParseFallsBackToInteger() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            // "unknown" is not a rule name, so it should try IntegerRulePrinterParser
            assertEquals(2, parser.parse(ruleCtx, "42abc", 0));
            assertEquals(42, ruleCtx.index());
        }

        @Test
        void testParseFailureWhenNeitherStringNorIntegerMatches() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "unknown", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals(
                    "Expected an integer for a rule index but found 'unk'\nUnrecognized rule name starting with 'unkno'",
                    rootCtx.getErrorMessages());
            assertEquals(0,
                    rootCtx.getMaxError()); // Max error should be from StringRuleNamePrinterParser
        }

        @Test
        void testParseEmptyStringFailure() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertEquals(~0, parser.parse(ruleCtx, "", 0));
            assertFalse(rootCtx.getErrorMessages().isEmpty());
            assertEquals(
                    "Expected an integer for a rule index but found '<text end>'\nUnrecognized rule name starting with '<text end>'",
                    rootCtx.getErrorMessages());
            assertEquals(0, rootCtx.getMaxError());
        }

        @Test
        void testParseInvalidPositionThrowsException() {
            var parser = new NodeFormatterBuilder.RulePrinterParser();
            var rootCtx = new RootParseContext(null, null, PARSER);
            var ruleCtx = rootCtx.new Rule(rootCtx);

            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "expr", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ruleCtx, "prog", 6));
        }
    }

    @Nested
    class ChildrenPrinterParserTest {

        @Test
        void testFormatThrowsUnsupportedOperationException() {
            var separatorPrinterParser = new LiteralPrinterParser(" ");
            var childrenPrinterParser = new NodeFormatterBuilder.ChildrenPrinterParser(
                    separatorPrinterParser);
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);

            assertThrows(UnsupportedOperationException.class,
                    () -> childrenPrinterParser.format(ctx, buf));
        }

        @Test
        void testParseThrowsUnsupportedOperationException() {
            var separatorPrinterParser = new LiteralPrinterParser(" ");
            var childrenPrinterParser = new NodeFormatterBuilder.ChildrenPrinterParser(
                    separatorPrinterParser);
            var ctx = createCtx();

            assertThrows(UnsupportedOperationException.class,
                    () -> childrenPrinterParser.parse(ctx, "any text", 0));
        }

        @Test
        void testConstructorWithNullSeparator() {
            // Ensure that the constructor allows null separator if needed, though typically it won't be null
            var childrenPrinterParser = new NodeFormatterBuilder.ChildrenPrinterParser(null);
            assertNotNull(childrenPrinterParser);
            assertNull(childrenPrinterParser.separator());
        }
    }

    @Nested
    class WhitespacePrinterParserTest {

        @Test
        void testConstructorValidWhitespace() {
            assertDoesNotThrow(() -> new NodeFormatterBuilder.WhitespacePrinterParser(" ", false));
            assertDoesNotThrow(() -> new NodeFormatterBuilder.WhitespacePrinterParser("\t", false));
            assertDoesNotThrow(() -> new NodeFormatterBuilder.WhitespacePrinterParser("\n", false));
            assertDoesNotThrow(() -> new NodeFormatterBuilder.WhitespacePrinterParser("  \t\n",
                    false));
            assertDoesNotThrow(() -> new NodeFormatterBuilder.WhitespacePrinterParser("", false));
        }

        @Test
        void testConstructorInvalidWhitespace() {
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> new NodeFormatterBuilder.WhitespacePrinterParser("a ", false));
            assertEquals("Can only append whitespace", exception.getMessage());

            exception = assertThrows(IllegalArgumentException.class,
                    () -> new NodeFormatterBuilder.WhitespacePrinterParser(" 1", false));
            assertEquals("Can only append whitespace", exception.getMessage());

            exception = assertThrows(IllegalArgumentException.class,
                    () -> new NodeFormatterBuilder.WhitespacePrinterParser(" \tX\n", false));
            assertEquals("Can only append whitespace", exception.getMessage());
        }

        @Test
        void testFormatFixedWhitespace() {
            var printer = new NodeFormatterBuilder.WhitespacePrinterParser("   ", false);
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);

            assertTrue(printer.format(ctx, buf));
            assertEquals("   ", buf.toString());

            buf = new StringBuilder();
            printer = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            assertTrue(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatIndentedWhitespace() {
            var printer = new NodeFormatterBuilder.WhitespacePrinterParser("  ",
                    true); // 2 spaces per indent
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);

            // Depth 0
            ctx.setNode(Node.Rule.attachTo(null, 0));
            assertTrue(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());

            // Depth 1
            buf = new StringBuilder();
            ctx.setNode(Node.Rule.attachTo(Node.Rule.attachTo(null, 0), 1));
            assertTrue(printer.format(ctx, buf));
            assertEquals("  ", buf.toString());

            // Depth 3
            buf = new StringBuilder();
            Node root = Node.Rule.attachTo(null, 0);
            Node child1 = Node.Rule.attachTo(root, 1);
            Node child2 = Node.Rule.attachTo(child1, 2);
            ctx.setNode(child2);
            assertTrue(printer.format(ctx, buf));
            assertEquals("    ", buf.toString()); // 3 * "  "
        }

        @Test
        void testParseConsumesWhitespace() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("",
                    false); // "ignored" will be ignored
            var ctx = createCtx();

            assertEquals(4, parser.parse(ctx, "  \t\nremainder", 0)); // consumes 5 whitespace chars
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());

            assertEquals(3,
                    parser.parse(ctx, "abc", 3)); // no whitespace, should return current position
            assertTrue(ctx.getErrorMessages().isEmpty());
        }

        @Test
        void testParseConsumesOnlyWhitespace() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();

            assertEquals(3, parser.parse(ctx, "   abc", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseEmptyString() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();
            assertEquals(0, parser.parse(ctx, "", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseNoWhitespace() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();
            assertEquals(0, parser.parse(ctx, "abc", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseAtEndOfInput() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();
            assertEquals(4, parser.parse(ctx, "  \t\n", 0));
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseWithOffset() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();
            assertEquals(7, parser.parse(ctx, "abc  \t\nxyz", 3)); // starts at ' ' (index 3)
            assertTrue(ctx.getErrorMessages().isEmpty());
            assertEquals(0, ctx.getMaxError());
        }

        @Test
        void testParseInvalidPosition() {
            var parser = new NodeFormatterBuilder.WhitespacePrinterParser("", false);
            var ctx = createCtx();
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ctx, " ", -1));
            assertThrows(IndexOutOfBoundsException.class, () -> parser.parse(ctx, " ", 2));
        }

    }


    @Nested
    class PatternPrinterParserTest {

        private final PatternFormatter PATTERN_FORMATTER = PatternFormatter.SIMPLE.withRecognizer(new ExpressionParser(null));


        @Test
        void testFormatPatternNode() {
            var pattern = new PatternBuilder().toPattern();
            var printer = new NodeFormatterBuilder.PatternPrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 0, pattern);
            var ctx = new NodeFormatContext(null, PATTERN_FORMATTER, null);
            ctx.setNode(patternNode);
            assertTrue(printer.format(ctx, buf));
            // Simple formatter with empty pattern results in an empty string
            assertTrue(buf.toString().isEmpty());
        }


        @Test
        void testFormatOtherNodeTypesReturnsFalse() {
            var printer = new NodeFormatterBuilder.PatternPrinterParser();
            var buf = new StringBuilder();
            var ruleNode = Node.Rule.attachTo(null, 0);
            var ctx = new NodeFormatContext(null, PATTERN_FORMATTER, null);
            ctx.setNode(ruleNode);
            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
            var terminalNode = Node.Terminal.attachTo(null, Symbol.of().type(0).get());
            ctx.setNode(terminalNode);
            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }


        @Test
        void testFormatNullNodeReturnsFalse() {
            var printer = new NodeFormatterBuilder.PatternPrinterParser();
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, PATTERN_FORMATTER, null);
            assertFalse(printer.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }


        @Test
        void testFormatWithNullPatternFormatterThrowsNPE() {
            var pattern = new PatternBuilder().toPattern();
            var printer = new NodeFormatterBuilder.PatternPrinterParser();
            var buf = new StringBuilder();
            var patternNode = Node.Pattern.attachTo(null, 0, pattern);
            var ctx = new NodeFormatContext(null, null, null); // No pattern formatter
            ctx.setNode(patternNode);
            assertThrows(NullPointerException.class, () -> printer.format(ctx, buf));
        }


        @Test
        void testParseSuccessIntoPatternContext() {
            var parser = new NodeFormatterBuilder.PatternPrinterParser();
            var rootCtx = new RootParseContext(null, PATTERN_FORMATTER, null);
            var patternCtx = rootCtx.new Pattern(rootCtx);
            String input = "<label:expr>"; // A valid pattern string for the simple formatter
            int parsedLength = input.length();
            assertEquals(parsedLength, parser.parse(patternCtx, input + "remainder", 0), patternCtx.root().getErrorMessages());
            DerivationTree.Pattern patternNode = assertInstanceOf(DerivationTree.Pattern.class, patternCtx.resolve(null));
            var resolvedPattern = patternNode.getPattern();
            assertNotNull(resolvedPattern);
            assertTrue(rootCtx.getErrorMessages().isEmpty());
        }


        @Test
        void testParseWithWrongContextTypeThrowsRuntimeException() {
            var parser = new NodeFormatterBuilder.PatternPrinterParser();
            var rootCtx = new RootParseContext(null, PATTERN_FORMATTER, null);
            var ruleCtx = rootCtx.new Rule(rootCtx); // Incorrect context type
            assertThrows(RuntimeException.class,
                    () -> parser.parse(ruleCtx, "<label:prog>", 0),
                    "Wrong context type");
        }


        @Test
        void testParseWithNullPatternFormatterThrowsNPE() {
            var parser = new NodeFormatterBuilder.PatternPrinterParser();
            var rootCtx = createCtx();
            var patternCtx = rootCtx.new Pattern(rootCtx);
            assertThrows(NullPointerException.class,
                    () -> parser.parse(patternCtx, "some_input", 0));

        }


        @Test
        void testParseInvalidPosition() {
            var parser = new NodeFormatterBuilder.PatternPrinterParser();
            var rootCtx = new RootParseContext(null, PATTERN_FORMATTER, null);
            var patternCtx = rootCtx.new Pattern(rootCtx);
            assertThrows(IndexOutOfBoundsException.class,
                    () -> parser.parse(patternCtx, "<1:label>", -1));

            assertThrows(IndexOutOfBoundsException.class,
                    () -> parser.parse(patternCtx, "<1:label>", "<1:label>".length() + 1));

        }

        @Test
        void testParseEmptyString() {
            var parser = new NodeFormatterBuilder.PatternPrinterParser();
            var rootCtx = new RootParseContext(null, PATTERN_FORMATTER, null);
            var patternCtx = rootCtx.new Pattern(rootCtx);
            assertEquals(0, parser.parse(patternCtx, "", 0));
        }
    }
}