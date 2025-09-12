package io.github.airbag.tree;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, cst);
        assertEquals(3, cst.children().size());

        ConcreteSyntaxTree child1 = cst.children().get(0);
        ConcreteSyntaxTree child2 = cst.children().get(1);
        ConcreteSyntaxTree child3 = cst.children().get(2);

        assertInstanceOf(ConcreteSyntaxTree.Rule.class, child1);
        assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child2);
        assertInstanceOf(ConcreteSyntaxTree.Rule.class, child3);

        ConcreteSyntaxTree.Terminal terminal1 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child1.children().get(0));
        assertEquals("1", terminal1.getSymbol().text());

        ConcreteSyntaxTree.Terminal terminal2 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child2);
        assertEquals("+", terminal2.getSymbol().text());

        ConcreteSyntaxTree.Terminal terminal3 = assertInstanceOf(ConcreteSyntaxTree.Terminal.class, child3.children().get(0));
        assertEquals("2", terminal3.getSymbol().text());
    }
}
