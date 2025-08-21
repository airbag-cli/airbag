package io.github.airbag;

import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.tree.*;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TreeProvider fromSpec")
class TreeProviderTest {

    private TreeProvider treeProvider;

    @BeforeEach
    void setUp() {
        treeProvider = new TreeProvider(ExpressionParser.class);
    }

    @Test
    @DisplayName("should create a single rule node")
    void fromSpec_shouldCreateSingleRuleNode() {
        ValidationTree tree = treeProvider.fromSpec("(prog EOF)");

        assertNotNull(tree);
        var ruleNode = assertInstanceOf(RuleValidationNode.class,
                tree,
                "Tree should be a RuleValidationNode");
        assertEquals(ExpressionParser.RULE_prog, ruleNode.getPayload());
        assertEquals(1, ruleNode.getChildCount(), "Rule node should have no children except EOF");
    }

    @Test
    @DisplayName("should create a single token node (full form)")
    void fromSpec_shouldCreateSingleTokenNode_fullForm() {
        ValidationTree tree = treeProvider.fromSpec("(ID 'x')");

        assertNotNull(tree);
        var tokenNode = assertInstanceOf(TerminalValidationNode.class,
                tree,
                "Tree should be a TerminalValidationNode");
        Token symbol = tokenNode.getPayload();
        assertEquals(ExpressionParser.ID, symbol.getType());
        assertEquals("x", symbol.getText());
        assertEquals(0, symbol.getTokenIndex());
    }

    @Test
    @DisplayName("should create a single token node (shorthand)")
    void fromSpec_shouldCreateSingleTokenNode_shorthand() {
        ValidationTree tree = treeProvider.fromSpec("'='");

        assertNotNull(tree);
        var tokenNode = assertInstanceOf(TerminalValidationNode.class,
                tree,
                "Tree should be a TerminalValidationNode");
        Token symbol = tokenNode.getPayload();
        assertEquals(ExpressionParser.T__0, symbol.getType());
        assertEquals("=", symbol.getText());
    }

    @Test
    @DisplayName("should create a single error node")
    void fromSpec_shouldCreateSingleErrorNode() {
        ValidationTree tree = treeProvider.fromSpec("(<error> (ID 'x'))");

        assertNotNull(tree);
        var errorNode = assertInstanceOf(ErrorValidationNode.class,
                tree,
                "Tree should be an ErrorValidationNode");
        Token symbol = errorNode.getPayload();
        assertEquals(ExpressionParser.ID, symbol.getType());
        assertEquals("x", symbol.getText());
    }

    @Test
    @DisplayName("should create a single EOF node")
    void fromSpec_shouldCreateEofNode() {
        ValidationTree tree = treeProvider.fromSpec("EOF");

        assertNotNull(tree);
        var tokenNode = assertInstanceOf(TerminalValidationNode.class,
                tree,
                "Tree should be a TerminalValidationNode");
        assertEquals(Token.EOF, tokenNode.getPayload().getType());
    }


    @Test
    @DisplayName("should create a tree with correct structure and nodes")
    void fromSpec_shouldCreateNestedTree() {
        String spec = "(stat (ID 'x') '=' (expr (INT '42')))";
        ValidationTree tree = treeProvider.fromSpec(spec);

        // Root: (stat ...)
        assertNotNull(tree);
        var statNode = assertInstanceOf(RuleValidationNode.class,
                tree,
                "Root should be a RuleValidationNode");
        assertEquals(ExpressionParser.RULE_stat, statNode.getPayload());
        assertEquals(3, statNode.getChildCount(), "Stat node should have 3 children");

        // Child 0: (ID 'x')
        var idChild = statNode.getChild(0);
        var idNode = assertInstanceOf(TerminalValidationNode.class,
                idChild,
                "Child 0 should be a TerminalValidationNode");
        var idToken = idNode.getPayload();
        assertEquals(ExpressionParser.ID, idToken.getType());
        assertEquals("x", idToken.getText());

        // Child 1: '='
        var eqChild = statNode.getChild(1);
        var eqNode = assertInstanceOf(TerminalValidationNode.class,
                eqChild,
                "Child 1 should be a TerminalValidationNode");
        var eqToken = eqNode.getPayload();
        assertEquals(ExpressionParser.T__0, eqToken.getType());
        assertEquals("=", eqToken.getText());
        assertEquals(1, eqToken.getTokenIndex());

        // Child 2: (expr ...)
        var exprChild = statNode.getChild(2);
        var exprNode = assertInstanceOf(RuleValidationNode.class,
                exprChild,
                "Child 2 should be a RuleValidationNode");
        assertEquals(ExpressionParser.RULE_expr, exprNode.getPayload());
        assertEquals(1, exprNode.getChildCount(), "Expr node should have 1 child");

        // Grandchild 0: (INT '42')
        var intGrandchild = exprNode.getChild(0);
        var intNode = assertInstanceOf(TerminalValidationNode.class,
                intGrandchild,
                "Grandchild should be a TerminalValidationNode");
        var intToken = intNode.getPayload();
        assertEquals(ExpressionParser.INT, intToken.getType());
        assertEquals("42", intToken.getText());
    }
}