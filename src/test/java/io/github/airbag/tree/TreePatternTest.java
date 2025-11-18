package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.TreeMatchResult;
import io.github.airbag.tree.pattern.TreePattern;
import io.github.airbag.tree.pattern.TreePatternBuilder;
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

    private final static ParseTreePatternMatcher MATCHER = new ParseTreePatternMatcher(new ExpressionLexer(
            null), new ExpressionParser(null));
    private TreeFormatter treeFormatter;
    private SymbolFormatter symbolFormatter;

    private ExpressionParser createParser(String input) {
        ExpressionLexer lexer = new ExpressionLexer(CharStreams.fromString(input));
        return new ExpressionParser(new CommonTokenStream(lexer));
    }

    private ParseTree parseExpression(String input) {
        return createParser(input).expr();
    }

    private ParseTree parseStat(String input) {
        return createParser(input).stat();
    }

    @BeforeEach
    void setup() {
        var airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        treeFormatter = airbag.getTreeProvider().getFormatter();
        symbolFormatter = airbag.getSymbolProvider().getFormatter();
    }

    @Test
    void testMatchIntLiteral() {
        DerivationTree t = treeFormatter.parse("(expr (INT '123'))");
        TreePattern pattern = new TreePatternBuilder(ExpressionParser.RULE_expr).appendSymbolTag(ExpressionParser.INT)
                .toPattern();
        TreeMatchResult result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(expr (INT '123'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchId() {
        DerivationTree t = treeFormatter.parse("(expr (ID 'abc'))");
        TreePattern pattern = new TreePatternBuilder(ExpressionParser.RULE_expr).appendSymbolTag(ExpressionParser.ID)
                .toPattern();
        TreeMatchResult result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(expr (ID 'abc'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchAssignment() {

//        ParseTreePattern pattern = MATCHER.compile("<ID> = <INT> \n", ExpressionParser.RULE_stat);

        DerivationTree t = treeFormatter.parse(
                "(stat (ID 'a') '=' (expr (INT '5')) (NEWLINE '\\n'))");
        TreePattern pattern = new TreePatternBuilder(ExpressionParser.RULE_stat).appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'='"))
                .appendSymbolTag(ExpressionParser.INT)
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();
        var result = pattern.match(t);
        assertTrue(result.succeeded());
        assertEquals("(stat (ID 'a') '=' (expr (INT '5')) (NEWLINE '\\n'))", treeFormatter.format(result.tree()));
    }

    @Test
    void testMatchAddition() {
        ParseTree tree = parseExpression("a + b");
        ParseTreePattern pattern = MATCHER.compile("<expr> + <expr>", ExpressionParser.RULE_expr);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchSubtraction() {
        ParseTree tree = parseExpression("a - b");
        ParseTreePattern pattern = MATCHER.compile("<expr> - <expr>", ExpressionParser.RULE_expr);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchMultiplication() {
        ParseTree tree = parseExpression("a * b");
        ParseTreePattern pattern = MATCHER.compile("<expr> * <expr>", ExpressionParser.RULE_expr);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchDivision() {
        ParseTree tree = parseExpression("a / b");
        ParseTreePattern pattern = MATCHER.compile("<expr> / <expr>", ExpressionParser.RULE_expr);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchParentheses() {
        ParseTree tree = parseExpression("(a + b)");
        ParseTreePattern pattern = MATCHER.compile("( <expr> )", ExpressionParser.RULE_expr);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
    }

    @Test
    void testMatchWithLabels() {
        ParseTree tree = parseStat("a = 5\n");
        ParseTreePattern pattern = MATCHER.compile("<lhs:ID> = <rhs:INT> \n",
                ExpressionParser.RULE_stat);
        ParseTreeMatch match = pattern.match(tree);
        assertTrue(match.succeeded());
        assertEquals("a", match.get("lhs").getText());
        assertEquals("5", match.get("rhs").getText());
    }

    @Test
    void testFindAll() {
        ExpressionParser parser = createParser("a = 5\nb = 10\n");
        ParseTree tree = parser.prog(); // Still matching against prog for findAll
        ParseTreePattern pattern = MATCHER.compile("<ID> = <INT> \n", ExpressionParser.RULE_stat);
        List<ParseTreeMatch> matches = pattern.findAll(tree, "//stat");
        assertEquals(2, matches.size());
    }

    @Test
    void testNoMatch() {
        ParseTree tree = parseStat("a = 5\n");
        ParseTreePattern pattern = MATCHER.compile("<ID> = <ID> \n", ExpressionParser.RULE_stat);
        ParseTreeMatch match = pattern.match(tree);
        assertFalse(match.succeeded());
    }
}