package io.github.airbag.tree;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test TreeFormatter")
public class TreeFormatterTest {

    @Test
    @DisplayName("should format a simple tree with SIMPLE formatter")
    void testSimpleFormatter() {
        // Create a simple tree: a root rule with one terminal child
        ConcreteSyntaxTree root = ConcreteSyntaxTree.Rule.root(ExpressionParser.RULE_expr);
        Symbol symbol = Symbol.of().type(ExpressionLexer.INT).text("123").get();
        ConcreteSyntaxTree.Terminal.attachTo(root, symbol);

        // Format it
        String formatted = TreeFormatter.SIMPLE.format(root);

        // Check the result
        assertEquals("(2 (9 '123'))", formatted);
    }

    @Test
    @DisplayName("should parse a simple tree with SIMPLE formatter")
    void testSimpleParser() {
        // The string to pars
        String formattedTree = "(2 (9 '123'))";

        // Parse it
        ConcreteSyntaxTree tree = TreeFormatter.SIMPLE.parseCST(formattedTree);

        // Check the result
        assertNotNull(tree);
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, tree);
        assertEquals(ExpressionParser.RULE_expr, tree.index());
        assertEquals(1, tree.children().size());

        Node<?> child = tree.children().getFirst();
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child);
        Symbol expectedSymbol = Symbol.of().type(ExpressionLexer.INT).text("123").get();
        assertEquals(expectedSymbol, ((ConcreteSyntaxTree.Terminal) child).getSymbol());
    }

    @Test
    @DisplayName("should correctly parse sibling nodes")
    void testSiblingNodeParsing() {
        String stringTree = "(2 (9 '123') (10 'my text'))";

        // Parse the string
        ConcreteSyntaxTree tree = TreeFormatter.SIMPLE.parseCST(stringTree);

        // Assertions
        assertNotNull(tree);
        assertEquals(2, tree.children().size());

        Node<?> child1 = tree.children().get(0);
        Node<?> child2 = tree.children().get(1);

        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child1);
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child2);

        assertEquals(9, child1.index());
        assertEquals(10, child2.index());
    }

    @Test
    void testExpressionTree() {
        var lexer = new ExpressionLexer(CharStreams.fromString("""
                x = 5
                """));
        var parser = new ExpressionParser(new CommonTokenStream(lexer));
        TreeFormatter formatter = TreeFormatter.SIMPLE.withRecognizer(parser);
        ConcreteSyntaxTree tree = ConcreteSyntaxTree.from(parser.prog());
        assertEquals("(prog (stat (ID 'x') '=' (expr (INT '5')) (NEWLINE '\\n')) EOF)",
                formatter.format(tree));

        ConcreteSyntaxTree parsedTree = formatter.parseCST(
                "(prog (stat (ID 'x') '=' (expr (INT '5')) (NEWLINE '\\n')) EOF)");
        assertEquals("(prog (stat (ID 'x') '=' (expr (INT '5')) (NEWLINE '\\n')) EOF)",
                formatter.format(parsedTree));

        assertNotNull(parsedTree);
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, parsedTree);
        assertEquals(ExpressionParser.RULE_prog, parsedTree.index());
        assertEquals(2, parsedTree.children().size());

        Node<?> statNode = parsedTree.children().getFirst();
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, statNode);
        assertEquals(ExpressionParser.RULE_stat, statNode.index());
        assertEquals(4, ((ConcreteSyntaxTree.Rule) statNode).children().size());

        Node<?> idNode = ((ConcreteSyntaxTree.Rule) statNode).children().getFirst();
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, idNode);
        assertEquals(ExpressionLexer.ID, idNode.index());
        assertEquals("x", ((ConcreteSyntaxTree.Terminal) idNode).getSymbol().text());

        int assignTokenType = -1;
        for (int i = 1; i <= parser.getVocabulary().getMaxTokenType(); i++) {
            if ("'='".equals(parser.getVocabulary().getLiteralName(i))) {
                assignTokenType = i;
                break;
            }
        }
        Node<?> eqNode = ((ConcreteSyntaxTree.Rule) statNode).children().get(1);
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, eqNode);
        assertEquals(assignTokenType, eqNode.index());
        assertEquals("=", ((ConcreteSyntaxTree.Terminal) eqNode).getSymbol().text());

        Node<?> exprNode = ((ConcreteSyntaxTree.Rule) statNode).children().get(2);
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, exprNode);
        assertEquals(ExpressionParser.RULE_expr, exprNode.index());
        assertEquals(1, ((ConcreteSyntaxTree.Rule) exprNode).children().size());

        Node<?> intNode = ((ConcreteSyntaxTree.Rule) exprNode).children().getFirst();
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, intNode);
        assertEquals(ExpressionLexer.INT, intNode.index());
        assertEquals("5", ((ConcreteSyntaxTree.Terminal) intNode).getSymbol().text());

        Node<?> newlineNode = ((ConcreteSyntaxTree.Rule) statNode).children().get(3);
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, newlineNode);
        assertEquals(ExpressionLexer.NEWLINE, newlineNode.index());
        assertEquals("\n", ((ConcreteSyntaxTree.Terminal) newlineNode).getSymbol().text());

        Node<?> eofNode = parsedTree.children().get(1);
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, eofNode);
        assertEquals(ExpressionLexer.EOF, eofNode.index());
        assertEquals("<EOF>", ((ConcreteSyntaxTree.Terminal) eofNode).getSymbol().text());
    }

    @Test
    void testPaddingParser() {
        var lexer = new ExpressionLexer(CharStreams.fromString("""
                x = 5
                """));
        var parser = new ExpressionParser(new CommonTokenStream(lexer));
        ConcreteSyntaxTree tree = ConcreteSyntaxTree.from(parser.prog());
        TreeFormatter formatter = new TreeFormatterBuilder().appendRule()
                .appendLiteral(":\n")
                .startChildren()
                .appendPadding(2)
                .appendChildren("\n")
                .endChildren()
                .toFormatter();
        formatter = formatter.withRecognizer(parser)
                .withTerminalFormatter(SymbolFormatter.ofPattern("S: X"))
                .withErrorFormatter(SymbolFormatter.ofPattern("<ERROR> S: X"));
        assertEquals("""
               prog:
                 stat:
                   ID: x
                   '=': =
                   expr:
                     INT: 5
                   NEWLINE: \\n
                 EOF: <EOF>""", formatter.format(tree));
    }
}