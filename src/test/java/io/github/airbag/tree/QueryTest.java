package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.tree.query.Query;
import io.github.airbag.tree.query.QueryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryTest {

    private Airbag airbag;
    private QueryProvider queryProvider;
    private DerivationTree tree;
    private TreeProvider treeProvider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        queryProvider = new QueryProvider(new ExpressionParser(null));
        treeProvider = airbag.getTreeProvider();
        var symbolList = airbag.getSymbolProvider().fromInput("5 * (6 + x)");
        //(expr (expr (INT '5')) '*' (expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')'))
        tree = treeProvider.fromInput(symbolList, "expr");
    }

    @Test
    void testSimpleQuery() {
        Query query = queryProvider.compile("/expr/expr/INT");
        var trees = query.evaluate(tree);
        assertEquals(1, trees.size());
        var expected = treeProvider.fromSpec("(INT '5')");
        airbag.assertTree(expected, trees.getFirst());
    }

    @Test
    void testMultipleMatches() {
        Query query = queryProvider.compile("/expr/expr");
        var trees = query.evaluate(tree);
        assertEquals(2, trees.size());
        airbag.assertTree(treeProvider.fromSpec("(expr (INT '5'))"), trees.getFirst());
        airbag.assertTree(treeProvider.fromSpec(
                "(expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')')"), trees.getLast());
    }

    @Test
    void testAnywhereMatches() {
        Query query = queryProvider.compile("//expr/expr");
        var trees = query.evaluate(tree);
        assertEquals(5, trees.size());
        airbag.assertTree(treeProvider.fromSpec("(expr (INT '5'))"), trees.getFirst());
        airbag.assertTree(treeProvider.fromSpec(
                "(expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')')"), trees.get(1));
        airbag.assertTree(treeProvider.fromSpec("(expr (expr (INT '6')) '+' (expr (ID 'x')))"), trees.get(2));
        airbag.assertTree(treeProvider.fromSpec("(expr (INT '6'))"), trees.get(3));
        airbag.assertTree(treeProvider.fromSpec("(expr (ID 'x'))"), trees.get(4));
    }

    @Test
    void testIntegerRules() {
        Query query = queryProvider.compile("/%d/%d/".formatted(ExpressionParser.RULE_expr, ExpressionParser.RULE_expr));
        var trees = query.evaluate(tree);
        assertEquals(2, trees.size());
        airbag.assertTree(treeProvider.fromSpec("(expr (INT '5'))"), trees.getFirst());
        airbag.assertTree(treeProvider.fromSpec(
                "(expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')')"), trees.getLast());
    }

    @Test
    void testIntegerToken() {
        Query query = queryProvider.compile("/2/2/9");
        var trees = query.evaluate(tree);
        assertEquals(1, trees.size());
        var expected = treeProvider.fromSpec("(INT '5')");
        airbag.assertTree(expected, trees.getFirst());
    }

    @Test
    void testWildcardQuery() {
        Query query = queryProvider.compile("/expr/*");
        var trees = query.evaluate(tree);
        assertEquals(3, trees.size());
        airbag.assertTree(treeProvider.fromSpec("(expr (INT '5'))"), trees.get(0));
        airbag.assertTree(treeProvider.fromSpec("'*'"), trees.get(1));
        airbag.assertTree(treeProvider.fromSpec("(expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')')"), trees.get(2));
    }

    @Test
    void testAnywhereWildcardQuery() {
        Query query = queryProvider.compile("//*/INT");
        var trees = query.evaluate(tree);
        assertEquals(2, trees.size());
        airbag.assertTree(treeProvider.fromSpec("(INT '5')"), trees.get(0));
        airbag.assertTree(treeProvider.fromSpec("(INT '6')"), trees.get(1));
    }

    @Test
    void testLiteralQuery() {
        Query query = queryProvider.compile("/expr/expr/expr/'+'");
        var trees = query.evaluate(tree);
        assertEquals(1, trees.size());
        airbag.assertTree(treeProvider.fromSpec("'+'"), trees.getFirst());
    }

    @Test
    void testAnywhereLiteralQuery() {
        Query query = queryProvider.compile("//'*'");
        var trees = query.evaluate(tree);
        assertEquals(1, trees.size());
        airbag.assertTree(treeProvider.fromSpec("'*'"), trees.getFirst());
    }
}