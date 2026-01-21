package io.github.airbag.tree.query;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Node;
import io.github.airbag.tree.TreeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class QueryTest {

    private DerivationTree root;
    private DerivationTree childToken1;
    private DerivationTree childRule2;
    private DerivationTree grandchildToken3;
    private DerivationTree grandchildToken1;

    @BeforeEach
    void setUp() {
        // Create a tree structure for testing:
        //       rule(0)
        //       /      \
        //   token(1)   rule(2)
        //              /      \
        //          token(3)  token(1)
        root = TreeFormatter.SIMPLE.parse("(0 (1 'child_token_1') (2 (3 'grandchild_token_3') (1 'child_token_1')))");
        childToken1 = root.getChild(0);
        childRule2 = root.getChild(1);
        grandchildToken3 = childRule2.getChild(0);
        grandchildToken1 = childRule2.getChild(1);
    }

    @Test
    void testEvaluateEmptyQuery() {
        Query query = new Query(new QueryElement[]{});
        List<DerivationTree> result = query.evaluate(root);
        assertEquals(1, result.size());
        assertTrue(result.contains(root));
    }

    @Test
    void testSelectRootNode() {
        QueryElement element = new QueryElement.Rule(QueryElement.Navigator.ROOT, false, 0);
        Query query = new Query(new QueryElement[]{element});
        List<DerivationTree> result = query.evaluate(root);
        assertEquals(1, result.size());
        assertTrue(result.contains(root));
    }

    @Test
    void testSelectChildrenByRule() {
        QueryElement element = new QueryElement.Rule(QueryElement.Navigator.CHILDREN, false, 2);
        Query query = new Query(new QueryElement[]{element});
        List<DerivationTree> result = query.evaluate(root);
        assertEquals(1, result.size());
        assertTrue(result.contains(childRule2));
    }

    @Test
    void testSelectAllDescendantsByToken() {
        QueryElement element = new QueryElement.Token(QueryElement.Navigator.ALL, false, 1);
        Query query = new Query(new QueryElement[]{element});
        List<DerivationTree> result = query.evaluate(root);
        assertEquals(2, result.size(), "Should find two nodes with token type 1");
        assertTrue(result.contains(childToken1));
        assertTrue(result.contains(grandchildToken1));
    }

    @Test
    void testSelectOnlyDescendantsByToken() {
        // 'DESCENDANTS' excludes the root node, 'ALL' includes it.
        // In this case the root is rule(0), so the result is the same.
        QueryElement element = new QueryElement.Token(QueryElement.Navigator.DESCENDANTS, false, 1);
        Query query = new Query(new QueryElement[]{element});
        List<DerivationTree> result = query.evaluate(root);
        assertEquals(2, result.size(), "Should find two nodes with token type 1");
        assertTrue(result.contains(childToken1));
        assertTrue(result.contains(grandchildToken1));
    }

    @Test
    void testMultiStepQuery() {
        // Simulates a query like "//rule(2)/token(1)"
        QueryElement element1 = new QueryElement.Rule(QueryElement.Navigator.ALL, false, 2);
        QueryElement element2 = new QueryElement.Token(QueryElement.Navigator.CHILDREN, false, 1);
        Query query = new Query(new QueryElement[]{element1, element2});

        List<DerivationTree> result = query.evaluate(root);

        assertEquals(1, result.size());
        assertTrue(result.contains(grandchildToken1));
    }

    @Test
    void testWildcardQueryOnChildren() {
        QueryElement element = new QueryElement.Wildcard(QueryElement.Navigator.CHILDREN, false);
        Query query = new Query(new QueryElement[]{element});

        List<DerivationTree> result = query.evaluate(root);

        assertEquals(2, result.size());
        assertTrue(result.contains(childToken1));
        assertTrue(result.contains(childRule2));
    }

    @Test
    void testInvertedQuery() {
        // Find all children that are NOT token(1)
        QueryElement element = new QueryElement.Token(QueryElement.Navigator.CHILDREN, true, 1);
        Query query = new Query(new QueryElement[]{element});

        List<DerivationTree> result = query.evaluate(root);

        assertEquals(1, result.size());
        assertTrue(result.contains(childRule2));
    }

    @Test
    void testNoMatchQuery() {
        // Look for a token that doesn't exist as a child
        QueryElement element = new QueryElement.Token(QueryElement.Navigator.CHILDREN, false, 99);
        Query query = new Query(new QueryElement[]{element});

        List<DerivationTree> result = query.evaluate(root);

        assertTrue(result.isEmpty());
    }
}