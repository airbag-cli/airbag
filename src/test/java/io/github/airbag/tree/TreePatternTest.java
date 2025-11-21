package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.FormatterParsePosition;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolProvider;
import io.github.airbag.tree.pattern.TreeMatchResult;
import io.github.airbag.tree.pattern.TreePattern;
import io.github.airbag.tree.pattern.TreePatternBuilder;
import io.github.airbag.tree.pattern.TreePatternFormatter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.antlr.v4.runtime.tree.pattern.ParseTreePatternMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TreePatternTest {

    private TreeFormatter treeFormatter;
    private TreePatternFormatter patternFormatter;
    private TreeProvider treeProvider;
    private SymbolProvider symbolProvider;

    @BeforeEach
    void setup() {
        var airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        symbolProvider = airbag.getSymbolProvider();
        treeProvider = airbag.getTreeProvider();
        treeFormatter = treeProvider.getFormatter();
        var symbolFormatter = airbag.getSymbolProvider().getFormatter();
        patternFormatter = new TreePatternFormatter().withSymbolFormatter(symbolFormatter).withRecognizer(new ExpressionParser(null));
    }

    @Test
    void testMatchIntLiteral() {
        DerivationTree t = treeFormatter.parse("(expr (INT '123'))");
        TreePattern pattern = new TreePatternBuilder().appendSymbolTag(ExpressionParser.INT)
                .toPattern();
        TreeMatchResult result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(expr (INT '123'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchId() {
        DerivationTree t = treeFormatter.parse("(expr (ID 'abc'))");
        TreePattern pattern = new TreePatternBuilder().appendSymbolTag(ExpressionParser.ID)
                .toPattern();
        TreeMatchResult result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(expr (ID 'abc'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchAssignment() {
        DerivationTree t = treeFormatter.parse(
                "(stat (ID 'a') '=' (expr (INT '5')) (NEWLINE '\\n'))");
        TreePattern pattern = patternFormatter.parse("<ID> '=' <INT> (NEWLINE '\\n')", new FormatterParsePosition(0));
        var result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(stat (ID 'a') '=' (expr (INT '5')) (NEWLINE '\\n'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchAddition() {
        DerivationTree t = treeProvider.fromInput(symbolProvider.fromInput("a + b"), "expr");
        TreePattern pattern = patternFormatter.parse("<expr> '+' <expr>");
        TreeMatchResult match = pattern.match(t);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchSubtraction() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a - b"), "expr");
        TreePattern pattern = patternFormatter.parse("<expr> '-' <expr>");
        TreeMatchResult match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchMultiplication() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a * b"), "expr");
        TreePattern pattern = patternFormatter.parse("<expr> '*' <expr>");
        TreeMatchResult match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchDivision() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a / b"), "expr");
        TreePattern pattern = patternFormatter.parse("<expr> '/' <expr>");
        TreeMatchResult match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchParentheses() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("(a + b)"), "expr");
        TreePattern pattern = patternFormatter.parse("'(' <expr> ')'");
        TreeMatchResult match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchWithLabels() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a = 5\n"), "stat");
        TreePattern pattern = patternFormatter.parse("<lhs:ID> '=' <rhs:INT> (NEWLINE '\\n')");
        TreeMatchResult match = pattern.match(tree);
        assertTrue(match.succeeded());
        assertEquals("(ID 'a')", treeFormatter.format(match.getLabel("lhs")));
        assertEquals("(expr (INT '5'))", treeFormatter.format(match.getLabel("rhs")));
    }

    @Test
    void testFindAll() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a = 5\nb = 10\n"), "prog");
        TreePattern pattern = patternFormatter.parse("<ID> '=' <INT> (NEWLINE '\\n')");
        List<DerivationTree> matches = pattern.findAll(tree);
        assertEquals(2, matches.size());
    }

    @Test
    void testNoMatch() {
        DerivationTree tree = treeProvider.fromInput(symbolProvider.fromInput("a = 5\n"), "stat");
        TreePattern pattern = patternFormatter.parse("<ID> '=' <ID> (NEWLINE '\\n')");
        TreeMatchResult match = pattern.match(tree);
        assertFalse(match.succeeded());
    }
}