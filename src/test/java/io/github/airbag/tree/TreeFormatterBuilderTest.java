package io.github.airbag.tree;

import io.github.airbag.tree.NodeFormatterBuilder.NodePrinterParser;
import io.github.airbag.tree.TreeFormatterBuilder.CompositePrinterParser;
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


    }
}