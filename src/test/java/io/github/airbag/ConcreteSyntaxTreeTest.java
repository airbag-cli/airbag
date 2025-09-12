package io.github.airbag;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.tree.ConcreteSyntaxTree;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcreteSyntaxTreeTest {

    private ParseTree createParseTree(String expression) {
        ExpressionLexer lexer = new ExpressionLexer(CharStreams.fromString(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        return parser.expr();
    }

    @Test
    void testFromParseTree() {
        ParseTree parseTree = createParseTree("1 + 2");
        ConcreteSyntaxTree cst = ConcreteSyntaxTree.from(parseTree);

        assertNotNull(cst);
        ConcreteSyntaxTree.Rule rule = assertInstanceOf(ConcreteSyntaxTree.Rule.class, cst);
        assertEquals(3, cst.children().size());

        var child1 = assertInstanceOf(ConcreteSyntaxTree.Rule.class ,cst.getChild(0));
        var child2 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, cst.getChild(1));
        var child3 = assertInstanceOf(ConcreteSyntaxTree.Rule.class ,cst.getChild(2));
        var symbol1 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child1.getChild(0)).getSymbol();
        var symbol2 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child3.getChild(0)).getSymbol();

        assertEquals("1", symbol1.text());
        assertEquals("+", child2.getSymbol().text());
        assertEquals("2", symbol2.text());
    }
}
