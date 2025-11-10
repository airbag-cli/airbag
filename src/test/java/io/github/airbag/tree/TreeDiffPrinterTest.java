package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.tree.query.Query;
import io.github.airbag.tree.query.QueryProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeDiffPrinterTest {

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
    void testDiffWithSubtree() {
        Query query = queryProvider.compile("/expr/expr");
        var trees = query.evaluate(tree);
        assertEquals(2, trees.size());
        TreeDiffPrinter diffPrinter = new TreeDiffPrinter(treeProvider.getFormatter());
        assertEquals("""
                +-------+-------+-------------------+-------------------+
                | index | delta |     expected      |      actual       |
                +-------+-------+-------------------+-------------------+
                |   0   |   -   | (expr)            |                   |
                |   0   |       |   (expr)          |   (expr)          |
                |   2   |   -   |     (INT '5')     |                   |
                |   3   |   -   |   '*'             |                   |
                |   4   |   -   |   (expr)          |                   |
                |   1   |       |     '('           |     '('           |
                |   2   |       |     (expr)        |     (expr)        |
                |   3   |       |       (expr)      |       (expr)      |
                |   4   |       |         (INT '6') |         (INT '6') |
                |   5   |       |       '+'         |       '+'         |
                |   6   |       |       (expr)      |       (expr)      |
                |   7   |       |         (ID 'x')  |         (ID 'x')  |
                |   8   |       |     ')'           |     ')'           |
                +-------+-------+-------------------+-------------------+""", diffPrinter.printDiff(tree, trees.get(1)));
    }

    @Test
    void testDiffIncompleteTree() {
        DerivationTree other = treeProvider.fromSpec("(expr (expr (INT '5')) (expr '(' (expr (expr (INT '6')) '+' (expr (ID 'x'))) ')') '*' )");
        TreeDiffPrinter diffPrinter = new TreeDiffPrinter(treeProvider.getFormatter());
        assertEquals("""
                +-------+-------+-------------------+-------------------+
                | index | delta |     expected      |      actual       |
                +-------+-------+-------------------+-------------------+
                |   0   |       | (expr)            | (expr)            |
                |   1   |       |   (expr)          |   (expr)          |
                |   2   |       |     (INT '5')     |     (INT '5')     |
                |   3   |   -   |   '*'             |                   |
                |   3   |       |   (expr)          |   (expr)          |
                |   4   |       |     '('           |     '('           |
                |   5   |       |     (expr)        |     (expr)        |
                |   6   |       |       (expr)      |       (expr)      |
                |   7   |       |         (INT '6') |         (INT '6') |
                |   8   |       |       '+'         |       '+'         |
                |   9   |       |       (expr)      |       (expr)      |
                |  10   |       |         (ID 'x')  |         (ID 'x')  |
                |  11   |       |     ')'           |     ')'           |
                |  12   |   +   |                   |   '*'             |
                +-------+-------+-------------------+-------------------+""", diffPrinter.printDiff(tree, other));
    }
}