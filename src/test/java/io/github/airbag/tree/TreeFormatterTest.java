package io.github.airbag.tree;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TreeFormatterTest {

    private static final Symbol SYMBOL = SymbolFormatter.SIMPLE.parse("(4 'My text')");
    private static final TreeFormatter FORMATTER = TreeFormatter.SIMPLE;

    @Test
    void testFormatTerminalNode() {
        DerivationTree terminalNode = Node.Terminal.root(SYMBOL);
        assertEquals("(4 'My text')", FORMATTER.format(terminalNode));
    }

    @Test
    void testParseTerminalNode() {
        DerivationTree actual = FORMATTER.parse("(4 'My text')");
        DerivationTree.Terminal terminal = assertInstanceOf(DerivationTree.Terminal.class, actual);
        Symbol symbol = terminal.symbol();
        assertEquals(4, symbol.type());
        assertEquals("My text", symbol.text());
    }

    @Test
    void testFormatErrorNode() {
        DerivationTree terminalNode = Node.Error.root(SYMBOL);
        assertEquals("(<error> (4 'My text'))", FORMATTER.format(terminalNode));
    }

    @Test
    void testParseErrorNode() {
        DerivationTree actual = FORMATTER.parse("(<error> (4 'My text'))");
        DerivationTree.Error error = assertInstanceOf(DerivationTree.Error.class, actual);
        Symbol symbol = error.symbol();
        assertEquals(4, symbol.type());
        assertEquals("My text", symbol.text());
    }

    @Test
    void testRuleNodeFormat() {
        DerivationTree ruleNode = Node.Rule.root(1);
        assertEquals("(1 )", FORMATTER.format(ruleNode));
    }

    @Test
    void testRuleNodeParse() {
        DerivationTree actual = FORMATTER.parse("(1 (4 'My text'))");
        DerivationTree.Rule ruleNode = assertInstanceOf(DerivationTree.Rule.class, actual);
        assertEquals(1, ruleNode.index());
        assertEquals(1, ruleNode.size());
        DerivationTree.Terminal child = assertInstanceOf(DerivationTree.Terminal.class,
                ruleNode.getChild(0));
        assertEquals(4, child.index());
    }

    @Test
    void testFormatMultipleChildren() {
        DerivationTree tree = Node.Rule.root(1);
        Node.Terminal.attachTo(tree, SYMBOL);
        Node.Terminal.attachTo(tree, SYMBOL);
        assertEquals("(1 (4 'My text') (4 'My text'))", FORMATTER.format(tree));
    }

    @Test
    void testParseMultipleChildren() {
        DerivationTree tree = FORMATTER.parse("(1 (4 'My text') (4 'My text'))");
        assertEquals(2, tree.size());
    }

    @Test
    void testFormatMultipleChildrenWithSeparator() {
        TreeFormatter formatter = new TreeFormatterBuilder().onRule(onRule -> onRule.appendLiteral(
                        "(").appendRule().appendLiteral(" ").appendChildren(", ").appendLiteral(")"))
                .onTerminal(
                        NodeFormatterBuilder::appendSymbol)
                .toFormatter();
        DerivationTree tree = Node.Rule.root(1);
        Node.Terminal.attachTo(tree, SYMBOL);
        Node.Terminal.attachTo(tree, SYMBOL);
        Node.Terminal.attachTo(tree, SYMBOL);
        assertEquals("(1 (4 'My text'), (4 'My text'), (4 'My text'))", formatter.format(tree));
    }

    @Test
    void testParseMultipleChildrenWithSeparator() {
        TreeFormatter formatter = new TreeFormatterBuilder().onRule(onRule -> onRule.appendLiteral(
                        "(").appendRule().appendLiteral(" ").appendChildren(", ").appendLiteral(")"))
                .onTerminal(
                        NodeFormatterBuilder::appendSymbol)
                .toFormatter();
        DerivationTree tree = formatter.parse("(1 (4 'My text'), (4 'My text'), (4 'My text'))");
        assertEquals(3, tree.size());
    }

    @Test
    void testFormatterMultipleBranches() {
        DerivationTree tree = Node.Rule.root(1);
        Node.Rule.attachTo(tree, 2);
        Node.Rule.attachTo(tree, 3);
        Node.Terminal.attachTo(tree.getChild(0), SYMBOL);
        Node.Terminal.attachTo(tree.getChild(0), SYMBOL);
        Node.Terminal.attachTo(tree.getChild(1), SYMBOL);
        assertEquals("(1 (2 (4 'My text') (4 'My text')) (3 (4 'My text')))", FORMATTER.format(tree));
        DerivationTree actual = FORMATTER.parse("(1 (2 (4 'My text') (4 'My text')) (3 (4 'My text')))");
        assertEquals(2, actual.getChild(0).size());
        assertEquals(1, actual.getChild(1).size());
    }

    @Test
    void testExceptionParse() {
        var e = assertThrows(TreeParseException.class, () -> FORMATTER.parse("(1 (2 (4 'My text') (4 'My text')) a(3 (4 'My text')))"));
        assertEquals("""
                Parse failed at index 35:
                Expected 'EOF' but found 'a(3'
                No vocabulary set
                Expected literal '(' but found 'a'
                Expected literal '(' but found 'a(3 (4 'My'
                Expected literal '(<error> ' but found 'a(3 (4 'My'
                Expected literal ')' but found 'a(3 (4 'My'
                
                (1 (2 (4 'My text') (4 'My text')) >>a(3 (4 'My text')))
                """, e.getMessage());
    }

    @Test
    void testAntlrFormatter() {
        ExpressionLexer lexer = new ExpressionLexer(CharStreams.fromString("x = 5\n"));
        ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.prog();
        assertEquals("(prog (stat x = (expr 5) \\n) <EOF>)", tree.toStringTree(parser));
        TreeFormatter formatter = TreeFormatter.ANTLR.withRecognizer(parser);
        assertEquals("(prog (stat x = (expr 5) \\n) <EOF>)", formatter.format(DerivationTree.from(tree)));
    }

}