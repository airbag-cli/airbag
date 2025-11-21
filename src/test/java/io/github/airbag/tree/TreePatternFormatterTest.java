package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.FormatterParsePosition;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.pattern.TreePatternBuilder;
import io.github.airbag.tree.pattern.TreePatternFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreePatternFormatterTest {

    private SymbolFormatter symbolFormatter;
    private TreePatternFormatter treePatternFormatter;

    @BeforeEach
    void setup() {
        var airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        var symbolProvider = airbag.getSymbolProvider();
        symbolFormatter = symbolProvider.getFormatter();
        treePatternFormatter = new TreePatternFormatter().withSymbolFormatter(symbolFormatter).withRecognizer(new ExpressionParser(null));
    }

    @Test
    void formatSymbolicPattern() {
        var pattern = new TreePatternBuilder().appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr)
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();
        assertEquals("<ID> '=' <expr> (NEWLINE '\\n')", treePatternFormatter.format(pattern));
    }

    @Test
    void formatIntegerPattern() {
        var pattern = new TreePatternBuilder().appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr)
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();
        var formatter = treePatternFormatter.withRecognizer(null);
        assertEquals("<8/> (1 '=') <2> (10 '\\n')", formatter.format(pattern));
    }

    @Test
    void formatLabeledPattern() {
        var pattern = new TreePatternBuilder().appendSymbolTag(ExpressionParser.ID, "left")
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr, "right")
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();
        assertEquals("<left:ID> '=' <right:expr> (NEWLINE '\\n')", treePatternFormatter.format(pattern));
    }

    @Test
    void parseSymbolicPattern() {
        String patternString = "<ID> '=' <expr> (NEWLINE '\\n')";
        int rootIndex = ExpressionParser.RULE_stat;

        // Parse the pattern string
        FormatterParsePosition position = new FormatterParsePosition(0);
        var parsedPattern = treePatternFormatter.parse(patternString, position);

        // Build the expected pattern using TreePatternBuilder
        var expectedPattern = new TreePatternBuilder()
                .appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr)
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();

        // Compare formatted output for verification
        assertEquals(treePatternFormatter.format(expectedPattern), treePatternFormatter.format(parsedPattern));
    }

    @Test
    void parseIntegerPattern() {
        String patternString = "<8/> (1 '=') <2> (10 '\\n')";
        int rootIndex = ExpressionParser.RULE_stat;

        // Use a formatter without a recognizer
        var integerFormatter = treePatternFormatter.withRecognizer(null);

        // Parse the pattern string
        FormatterParsePosition position = new FormatterParsePosition(0);
        var parsedPattern = integerFormatter.parse(patternString, position);

        // Build the expected pattern using TreePatternBuilder
        var expectedPattern = new TreePatternBuilder()
                .appendSymbolTag(ExpressionParser.ID) // ID is 8
                .appendSymbol(symbolFormatter.parse("'='" )) // '=' is 1
                .appendRuleTag(ExpressionParser.RULE_expr) // expr is 2
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')")) // NEWLINE is 10
                .toPattern();

        // Compare formatted output for verification
        assertEquals(integerFormatter.format(expectedPattern), integerFormatter.format(parsedPattern));
    }

    @Test
    void parseLabeledPattern() {
        String patternString = "<left:ID> '=' <right:expr> (NEWLINE '\\n')";
        int rootIndex = ExpressionParser.RULE_stat;

        // Parse the pattern string
        FormatterParsePosition position = new FormatterParsePosition(0);
        var parsedPattern = treePatternFormatter.parse(patternString, position);

        // Build the expected pattern using TreePatternBuilder
        var expectedPattern = new TreePatternBuilder()
                .appendSymbolTag(ExpressionParser.ID, "left")
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr, "right")
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();

        // Compare formatted output for verification
        assertEquals(treePatternFormatter.format(expectedPattern), treePatternFormatter.format(parsedPattern));
    }

    @Test
    void parseMixedPattern() {
        String patternString = "<ID> '+' <expr> <ruleName:stat> (NEWLINE '\\n')";
        int rootIndex = ExpressionParser.RULE_stat;

        FormatterParsePosition position = new FormatterParsePosition(0);
        var parsedPattern = treePatternFormatter.parse(patternString, position);

        var expectedPattern = new TreePatternBuilder()
                .appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'+'"))
                .appendRuleTag(ExpressionParser.RULE_expr)
                .appendRuleTag(ExpressionParser.RULE_stat, "ruleName")
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();

        assertEquals(treePatternFormatter.format(expectedPattern), treePatternFormatter.format(parsedPattern));
    }

    @Test
    void parseWhitespaceVariations() {
        String patternString = " <ID>  '='   <expr> (NEWLINE   '\\n'  ) ";
        int rootIndex = ExpressionParser.RULE_stat;

        FormatterParsePosition position = new FormatterParsePosition(0);
        var parsedPattern = treePatternFormatter.parse(patternString, position);

        var expectedPattern = new TreePatternBuilder()
                .appendSymbolTag(ExpressionParser.ID)
                .appendSymbol(symbolFormatter.parse("'='" ))
                .appendRuleTag(ExpressionParser.RULE_expr)
                .appendSymbol(symbolFormatter.parse("(NEWLINE '\\n')"))
                .toPattern();

        assertEquals(treePatternFormatter.format(expectedPattern), treePatternFormatter.format(parsedPattern));
    }
}