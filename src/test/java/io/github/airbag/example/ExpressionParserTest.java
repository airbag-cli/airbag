package io.github.airbag.example;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionParserTest {

    private Airbag airbag;
    private TreeProvider treeProvider;
    private SymbolFormatter symbolFormatter;

    @BeforeEach
    void setup() {
        airbag = Airbag.testParser(ExpressionParser.class);
        treeProvider = airbag.getTreeProvider();
        symbolFormatter = treeProvider.getFormatter().getSymbolFormatter();
    }

    @Test
    void testIntExpr() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("(expr (INT '10'))");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(INT '10')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testIdExpr() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("(expr (ID 'var'))");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(ID 'var')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testExprInParenthesis() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
            (expr
                '('
                 (expr (ID 'var'))
                 ')'
            )""");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("'(' (ID 'var') ')'");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testMultiplicationExpr() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
            (expr
                (expr (INT '5'))
                '*'
                (expr (ID 'x'))
            )""");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(INT '5') '*' (ID 'x')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testAdditionExpr() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
            (expr
                (expr (INT '5'))
                '+'
                (expr (ID 'x'))
            )""");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(INT '5') '+' (ID 'x')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "expr");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testNewlineStatement() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("(stat (NEWLINE '\\n'))");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(NEWLINE '\\n')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testAssignmentStatement() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("(stat (ID 'x') '=' (expr (INT '5')) (NEWLINE '\\n'))");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(ID 'x') '=' (INT '5') (NEWLINE '\\n')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testExpressionStatement() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
            (stat
                (expr
                    (expr (INT '5'))
                    '+'
                    (expr (ID 'x'))
                )
                (NEWLINE '\\n')
            )""");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("(INT '5') '+' (ID 'x') (NEWLINE '\\n')");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "stat");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }

    @Test
    void testProg() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
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
        List<Symbol> symbolList = symbolFormatter.parseList("""
                (ID 'x')
                '='
                (INT '10')
                (NEWLINE '\\n')
                (INT '5')
                '+'
                (ID 'x')
                (NEWLINE '\\n')
                EOF""");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "prog");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }


}