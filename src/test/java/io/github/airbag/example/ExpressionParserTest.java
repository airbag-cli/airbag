package io.github.airbag.example;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.token.TokenFormatter;
import io.github.airbag.tree.TreeProvider;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionParserTest {

    private Airbag airbag;
    private TreeProvider treeProvider;
    private TokenFormatter symbolFormatter;

    @BeforeEach
    void setup() {
        airbag = Airbag.testParser(ExpressionParser.class);
        treeProvider = airbag.getTreeProvider();
        symbolFormatter = treeProvider.getFormatter().getSymbolFormatter();
    }

    @Test
    void testIntExpr() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("(expr (INT '10'))");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(INT '10')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testIdExpr() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("(expr (ID 'var'))");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(ID 'var')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testExprInParenthesis() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
            (expr
                '('
                 (expr (ID 'var'))
                 ')'
            )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("'(' (ID 'var') ')'");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testMultiplicationExpr() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
            (expr
                (expr (INT '5'))
                '*'
                (expr (ID 'x'))
            )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(INT '5') '*' (ID 'x')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testAdditionExpr() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
            (expr
                (expr (INT '5'))
                '+'
                (expr (ID 'x'))
            )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(INT '5') '+' (ID 'x')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testNewlineStatement() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("(stat (NEWLINE '\\n'))");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(NEWLINE '\\n')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testAssignmentStatement() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("(stat (ID 'x') '=' (expr (INT '5')) (NEWLINE '\\n'))");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(ID 'x') '=' (INT '5') (NEWLINE '\\n')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testExpressionStatement() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
            (stat
                (expr
                    (expr (INT '5'))
                    '+'
                    (expr (ID 'x'))
                )
                (NEWLINE '\\n')
            )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("(INT '5') '+' (ID 'x') (NEWLINE '\\n')");

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testProg() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
                (prog
                    (stat
                        (ID 'x')
                        '='
                        (expr (INT '10'))
                        (NEWLINE '\\n')
                    )
                    (stat
                        (expr (expr (INT '5'))
                        '+'
                        (expr (ID 'x')))
                        (NEWLINE '\\n')
                    )
                    EOF
                )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("""
                (ID 'x')
                '='
                (INT '10')
                (NEWLINE '\\n')
                (INT '5')
                '+'
                (ID 'x')
                (NEWLINE '\\n')
                EOF
                """);

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "prog");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testProgWithPattern() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
                (prog
                    (<stat>
                        (
                            <ID>
                            '='
                            <expr>
                            (NEWLINE '\\n')
                        )
                    )
                    (stat
                        (expr (expr (INT '5'))
                        '+'
                        (expr (ID 'x')))
                        (NEWLINE '\\n')
                    )
                    EOF
                )""");

        // Create a derivation tree from symbol list.
        List<Token> symbolList = symbolFormatter.parseList("""
                (ID 'x')
                '='
                (INT '10')
                (NEWLINE '\\n')
                (INT '5')
                '+'
                (ID 'x')
                (NEWLINE '\\n')
                EOF
                """);

        // Pass the symbol list to the parser
        Tree actualTree = treeProvider.actual(symbolList, "prog");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }



}