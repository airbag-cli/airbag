package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.NodeFormatterBuilder.NodePrinterParser;
import io.github.airbag.tree.TreeFormatterBuilder.CompositePrinterParser;
import io.github.airbag.tree.pattern.Pattern;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.atn.ATN;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeFormatterBuilderTest {

    @Nested
    class CompositePrinterParserTest {

        NodePrinterParser[] printerParsers = new NodePrinterParser[]{new NodeFormatterBuilder.LiteralPrinterParser(
                "Hello "), new NodeFormatterBuilder.LiteralPrinterParser("World")};
        CompositePrinterParser compositePrinterParser = new CompositePrinterParser(printerParsers);

        @Test
        void testFormat() {
            StringBuilder buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);
            assertTrue(compositePrinterParser.format(ctx, buf));
            assertEquals("Hello World", buf.toString());
        }

        @Test
        void testFormatEmpty() {
            var compositePrinterParser = new CompositePrinterParser(new NodePrinterParser[0]);
            var buf = new StringBuilder();
            var ctx = new NodeFormatContext(null, null, null);
            compositePrinterParser.format(ctx, buf);
            assertTrue(buf.isEmpty());
        }

        @Test
        void testFormatFailure() {
            var printerParsers = new NodePrinterParser[]{new NodeFormatterBuilder.LiteralPrinterParser(
                    "Rule: "), new NodeFormatterBuilder.RulePrinterParser()};
            var compositePrinter = new CompositePrinterParser(printerParsers);
            var ctx = new NodeFormatContext(null, null, null);
            var buf = new StringBuilder();
            assertFalse(compositePrinter.format(ctx, buf));
            assertTrue(buf.isEmpty());
        }

        @Test
        void testParseSuccess() {
            assertEquals(11, compositePrinterParser.parse(null, "Hello World", 0));
        }

        @Test
        void testParseFailure() {
            assertEquals(~5,
                    compositePrinterParser.parse(new RootParseContext(null, null, null),
                            "Hello World",
                            5));
            assertEquals(~6,
                    compositePrinterParser.parse(new RootParseContext(null, null, null),
                            "Hello world",
                            0));
        }

        @Nested
        class TreePrinterParserTest {

            private static final Recognizer<?, ?> MOCK_RECOGNIZER = new Recognizer<>() {
                @Override
                public String[] getTokenNames() {
                    return new String[] {null, "TOKEN1", "'!'", "TOKEN2", "'='"};
                }

                @Override
                public String[] getRuleNames() {
                    return new String[] {"rule1", "rule2", "rule3"};
                }

                @Override
                public String getGrammarFileName() {
                    return "";
                }

                @Override
                public ATN getATN() {
                    return null;
                }

                @Override
                public IntStream getInputStream() {
                    return null;
                }

                @Override
                public void setInputStream(IntStream input) {

                }

                @Override
                public TokenFactory<?> getTokenFactory() {
                    return null;
                }

                @Override
                public void setTokenFactory(TokenFactory<?> input) {

                }
            };

            private static final TreeFormatterBuilder.TreePrinterParser PP = new TreeFormatterBuilder.TreePrinterParser(
                    new NodePrinterParser[] {new NodeFormatterBuilder.LiteralPrinterParser("Rule: "), new NodeFormatterBuilder.RulePrinterParser()},
                    new NodePrinterParser[] {new NodeFormatterBuilder.LiteralPrinterParser("Terminal: "), new NodeFormatterBuilder.SymbolPrinterParser()},
                    new NodePrinterParser[] {new NodeFormatterBuilder.LiteralPrinterParser("Error: "), new NodeFormatterBuilder.SymbolPrinterParser()},
                    new NodePrinterParser[] {new NodeFormatterBuilder.LiteralPrinterParser("Pattern: "), new NodeFormatterBuilder.RulePrinterParser()}
            );

            @Test
            void testFormatRule() {
                StringBuilder buf = new StringBuilder();
                var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE, PatternFormatter.SIMPLE, MOCK_RECOGNIZER);
                ctx.setNode(Node.Rule.root(0));
                PP.format(ctx, buf);
                assertEquals("Rule: rule1", buf.toString());
            }

            @Test
            void testFormatTerminal() {
                StringBuilder buf = new StringBuilder();
                var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary()), PatternFormatter.SIMPLE, MOCK_RECOGNIZER);
                ctx.setNode(Node.Terminal.root(Symbol.of("(1 'myText')", SymbolFormatter.SIMPLE)));
                PP.format(ctx, buf);
                assertEquals("Terminal: (TOKEN1 'myText')", buf.toString());
            }

            @Test
            void testFormatError() {
                StringBuilder buf = new StringBuilder();
                var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary()), PatternFormatter.SIMPLE, MOCK_RECOGNIZER);
                ctx.setNode(Node.Error.root(Symbol.of("(1 'myText')", SymbolFormatter.SIMPLE)));
                PP.format(ctx, buf);
                assertEquals("Error: (TOKEN1 'myText')", buf.toString());
            }

            @Test
            void testFormatPattern() {
                StringBuilder buf = new StringBuilder();
                var ctx = new NodeFormatContext(SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary()), PatternFormatter.SIMPLE, MOCK_RECOGNIZER);
                ctx.setNode(Node.Pattern.root(2, Pattern.NOTHING));
                PP.format(ctx, buf);
                assertEquals("Pattern: rule3", buf.toString());
            }

            @Test
            void testParseTerminalSuccess() {
                SymbolFormatter symbolFormatter = SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary());
                PatternFormatter patternFormatter = PatternFormatter.SIMPLE;
                RootParseContext rootCtx = new RootParseContext(symbolFormatter, patternFormatter, MOCK_RECOGNIZER);

                String textToParse = "Terminal: (TOKEN1 'myText')";
                int startPosition = 0;

                int newPosition = PP.parse(rootCtx, textToParse, startPosition);

                assertTrue(newPosition > startPosition);
                assertEquals(textToParse.length(), newPosition);

                var node = rootCtx.resolve(null);
                var terminalNode = assertInstanceOf(DerivationTree.Terminal.class, node);
                var parsedSymbol = terminalNode.symbol();
                assertNotNull(parsedSymbol);
                assertEquals(1, parsedSymbol.type());
                assertEquals("myText", parsedSymbol.text());
            }

            @Test
            void testParseErrorSuccess() {
                SymbolFormatter symbolFormatter = SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary());
                PatternFormatter patternFormatter = PatternFormatter.SIMPLE;
                RootParseContext rootCtx = new RootParseContext(symbolFormatter, patternFormatter, MOCK_RECOGNIZER);

                String textToParse = "Error: (TOKEN1 'myError')";
                int startPosition = 0;

                int newPosition = PP.parse(rootCtx, textToParse, startPosition);

                assertTrue(newPosition > startPosition);
                assertEquals(textToParse.length(), newPosition);

                var node = rootCtx.resolve(null);
                var errorNode = assertInstanceOf(DerivationTree.Error.class, node);
                var parsedSymbol = errorNode.symbol();
                assertNotNull(parsedSymbol);
                assertEquals(1, parsedSymbol.type());
                assertEquals("myError", parsedSymbol.text());
            }

            @Test
            void testParsePatternSuccess() {
                SymbolFormatter symbolFormatter = SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary());
                PatternFormatter patternFormatter = PatternFormatter.SIMPLE;
                RootParseContext rootCtx = new RootParseContext(symbolFormatter, patternFormatter, MOCK_RECOGNIZER);

                String textToParse = "Pattern: rule3";
                int startPosition = 0;

                int newPosition = PP.parse(rootCtx, textToParse, startPosition);

                assertTrue(newPosition > startPosition);
                assertEquals(textToParse.length(), newPosition);

                var node = rootCtx.resolve(null);
                var patternNode = assertInstanceOf(DerivationTree.Pattern.class, node);
                assertEquals(2, patternNode.index()); // rule3 is at index 2
            }

            @Test
            void testParseRuleSuccess() {
                SymbolFormatter symbolFormatter = SymbolFormatter.SIMPLE.withVocabulary(MOCK_RECOGNIZER.getVocabulary());
                PatternFormatter patternFormatter = PatternFormatter.SIMPLE;
                RootParseContext rootCtx = new RootParseContext(symbolFormatter, patternFormatter, MOCK_RECOGNIZER);

                String textToParse = "Rule: rule1";
                int startPosition = 0;

                int newPosition = PP.parse(rootCtx, textToParse, startPosition);

                assertTrue(newPosition > startPosition);
                assertEquals(textToParse.length(), newPosition);

                var node = rootCtx.resolve(null);
                var ruleNode = assertInstanceOf(DerivationTree.Rule.class, node);
                assertEquals(0, ruleNode.index()); // rule1 is at index 0
            }
        }

    }
}