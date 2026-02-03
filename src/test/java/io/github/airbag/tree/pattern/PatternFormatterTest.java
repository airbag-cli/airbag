package io.github.airbag.tree.pattern;

import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternFormatterTest {

    private PatternFormatter formatter;

    @BeforeEach
    void setup() {
        formatter = PatternFormatter.SIMPLE.withRecognizer(new ExpressionParser(null));
    }

    @Test
    void testFormatSymbol() {
        var symbol = Symbol.of("[@5,1:2='my text',<10>,channel=2,3:4]");
        var pattern = new PatternBuilder().appendSymbol(symbol, SymbolField.equalizer(SymbolField.simple())).toPattern();
        assertEquals("(NEWLINE:2 'my text')", formatter.format(pattern));
    }

    @Test
    void testFormatSymbolTag() {
        var pattern = new PatternBuilder().appendSymbolTag(8).toPattern();
        assertEquals("<ID>", formatter.format(pattern));
    }

    @Test
    void testFormatRuleTagWithLabel() {
        var pattern = new PatternBuilder().appendRuleTag(ExpressionParser.RULE_expr, "myRule").toPattern();
        assertEquals("<myRule:expr>", formatter.format(pattern));
    }

    @Test
    void testFormatRuleTagWithoutLabel() {
        var pattern = new PatternBuilder().appendRuleTag(ExpressionParser.RULE_expr).toPattern();
        assertEquals("<expr>", formatter.format(pattern));
    }

    @Test
    void testFormatSymbolTagWithLabel() {
        var pattern = new PatternBuilder().appendSymbolTag(ExpressionParser.ID, "myID").toPattern();
        assertEquals("<myID:ID>", formatter.format(pattern));
    }

    @Test
    void testFormatComplexPattern() {
        var symbol = Symbol.of("[@5,1:2='var',<8>,3:4]");
        var pattern = new PatternBuilder()
                .appendSymbol(symbol, SymbolField.equalizer(SymbolField.simple()))
                .appendRuleTag(ExpressionParser.RULE_expr, "anotherRule")
                .appendSymbolTag(ExpressionParser.T__3)
                .toPattern();
        assertEquals("(ID 'var') <anotherRule:expr> <'+'>", formatter.format(pattern));
    }

    @Test
    void testParseSimplePattern() {
        var patternString = "(ID 'var') <expr>";
        var parsedPattern = formatter.parse(patternString);
        var parsedElements = parsedPattern.getElements();

        assertEquals(2, parsedElements.length);

        // Verify first element: SymbolPatternElement for (ID 'var')
        assertInstanceOf(PatternBuilder.SymbolPatternElement.class, parsedElements[0]);
        var parsedSymbolElement = (PatternBuilder.SymbolPatternElement) parsedElements[0];
        assertEquals(ExpressionParser.ID, parsedSymbolElement.getSymbol().type());
        assertEquals("var", parsedSymbolElement.getSymbol().text());
        // Comparing BiPredicate is hard, so we'll trust the formatter's internal logic for now.

        // Verify second element: RuleTagPatternElement for <expr>
        assertInstanceOf(PatternBuilder.RuleTagPatternElement.class, parsedElements[1]);
        var parsedRuleElement = (PatternBuilder.RuleTagPatternElement) parsedElements[1];
        assertEquals(ExpressionParser.RULE_expr, parsedRuleElement.type());
        assertNull(parsedRuleElement.label());
    }
}