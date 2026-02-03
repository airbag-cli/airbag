package io.github.airbag.tree.pattern;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Node;
import io.github.airbag.symbol.SymbolField;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PatternBuilderTest {


    @Nested
    class CompositePatternElementTest {


    }

    @Nested
    class SymbolPatternElementTest {

        private final Symbol SYMBOL = Symbol.of("[@1,2:3='My text',<4>,5:6]");

        @Test
        void testMatch() {
            var element = new PatternBuilder.SymbolPatternElement(SYMBOL,
                    SymbolField.equalizer(SymbolField.simple()));
            var elementStrict = new PatternBuilder.SymbolPatternElement(SYMBOL,
                    SymbolField.equalizer(SymbolField.all()));
            DerivationTree terminal = Node.Terminal.root(Symbol.of("[@1,4:3='My text',<4>,10:11]"));
            var ctx = new PatternContext(terminal);
            assertTrue(element.isMatch(ctx));
            assertFalse(elementStrict.isMatch(ctx));
        }

        @Test
        void testMatchSuccessfulNestedNode() {
            var element = new PatternBuilder.SymbolPatternElement(SYMBOL,
                    SymbolField.equalizer(SymbolField.simple()));
            DerivationTree rule = Node.Rule.root(4);
            DerivationTree terminal = Node.Terminal.attachTo(rule,Symbol.of("[@1,4:3='My text',<4>,10:11]"));
            var ctx = new PatternContext(terminal);
            assertTrue(element.isMatch(ctx));
        }

    }

    @Nested
    class RuleTagPatternElementTest {

        @Test
        void testSuccessfulIsMatch() {
            var element = new PatternBuilder.RuleTagPatternElement(5);
            DerivationTree rule = Node.Rule.root(5);
            var ctx = new PatternContext(rule);
            assertTrue(element.isMatch(ctx));
        }

        @Test
        void testFailureIsMatch() {
            var element = new PatternBuilder.RuleTagPatternElement(5);
            DerivationTree rule = Node.Rule.root(7);
            var ctx = new PatternContext(rule);
            assertFalse(element.isMatch(ctx));
        }

        @Test
        void testWrongNode() {
            var element = new PatternBuilder.RuleTagPatternElement(5);
            DerivationTree terminal = Node.Terminal.root(Symbol.of("(5 'text')", SymbolFormatter.SIMPLE));
            var ctx = new PatternContext(terminal);
            assertFalse(element.isMatch(ctx));
        }

        @Test
        void testLabel() {
            var element = new PatternBuilder.RuleTagPatternElement(5, "result");
            DerivationTree rule = Node.Rule.root(5);
            var ctx = new PatternContext(rule);
            assertTrue(element.isMatch(ctx));
            assertNotNull(ctx.getLabel("result"));
        }

    }


    @Nested
    class SymbolTagPatternElementTest {

        @Test
        void testSuccessfulMatch() {
            var element = new PatternBuilder.SymbolTagPatternElement(5);
            DerivationTree terminal = Node.Terminal.root(Symbol.of().type(5).get());
            var ctx = new PatternContext(terminal);
            assertTrue(element.isMatch(ctx));
        }

        @Test
        void testFailureMatch() {
            var element = new PatternBuilder.SymbolTagPatternElement(5);
            DerivationTree terminal = Node.Terminal.root(Symbol.of().type(6).get());
            var ctx = new PatternContext(terminal);
            assertFalse(element.isMatch(ctx));
        }

        @Test
        void testWrongNode() {
            var element = new PatternBuilder.SymbolTagPatternElement(5);
            DerivationTree terminal = Node.Rule.root(5);
            var ctx = new PatternContext(terminal);
            assertFalse(element.isMatch(ctx));
        }

        @Test
        void testLabel() {
            var element = new PatternBuilder.SymbolTagPatternElement(5, "result");
            DerivationTree rule = Node.Terminal.root(Symbol.of().type(5).get());
            var ctx = new PatternContext(rule);
            assertTrue(element.isMatch(ctx));
            assertNotNull(ctx.getLabel("result"));
        }

    }

    @Nested
    class PatternBuilderMethodTest {

        @Test
        void testAppendSymbolDefaultEqualizer() {
            // Given
            Symbol expectedSymbol = Symbol.of().text("hello").type(1).line(1).position(0).get();
            Pattern pattern = new PatternBuilder().appendSymbol(expectedSymbol).toPattern();

            DerivationTree parentMatching = Node.Rule.root(0); // Dummy parent
            Node.Terminal.attachTo(parentMatching, Symbol.of().text("hello").type(1).line(1).position(0).get());

            DerivationTree parentNonMatching = Node.Rule.root(0); // Dummy parent
            Node.Terminal.attachTo(parentNonMatching, Symbol.of().text("world").type(1).line(1).position(0).get());

            // When
            MatchResult successResult = pattern.match(parentMatching); // Pass parent
            MatchResult failureResult = pattern.match(parentNonMatching); // Pass parent

            // Then
            assertTrue(successResult.isSuccess());
            assertFalse(failureResult.isSuccess());
        }

        @Test
        void testAppendSymbolCustomEqualizer() {
            // Given
            Symbol expectedSymbol = Symbol.of().type(2).get(); // Only type matters
            Pattern pattern = new PatternBuilder()
                    .appendSymbol(expectedSymbol, SymbolField.equalizer(Set.of(SymbolField.TYPE)))
                    .toPattern();

            DerivationTree parentMatching = Node.Rule.root(0); // Dummy parent
            Node.Terminal.attachTo(parentMatching, Symbol.of().text("anything").type(2).line(5).position(10).get());

            DerivationTree parentNonMatching = Node.Rule.root(0); // Dummy parent
            Node.Terminal.attachTo(parentNonMatching, Symbol.of().text("anything").type(3).line(5).position(10).get());

            // When
            MatchResult successResult = pattern.match(parentMatching);
            MatchResult failureResult = pattern.match(parentNonMatching);

            // Then
            assertTrue(successResult.isSuccess());
            assertFalse(failureResult.isSuccess());
        }

        @Test
        void testAppendRuleTag() {
            // Given
            int ruleIndex = 10;
            Pattern pattern = new PatternBuilder().appendRuleTag(ruleIndex).toPattern();

            DerivationTree parentMatching = Node.Rule.root(0);
            Node.Rule.attachTo(parentMatching, ruleIndex);

            DerivationTree parentNonMatching = Node.Rule.root(0);
            Node.Rule.attachTo(parentNonMatching, ruleIndex + 1);

            DerivationTree parentTerminal = Node.Rule.root(0);
            Node.Terminal.attachTo(parentTerminal, Symbol.of().type(1).get());

            // When
            MatchResult successResult = pattern.match(parentMatching);
            MatchResult failureResultRule = pattern.match(parentNonMatching);
            MatchResult failureResultTerminal = pattern.match(parentTerminal);

            // Then
            assertTrue(successResult.isSuccess());
            assertFalse(failureResultRule.isSuccess());
            assertFalse(failureResultTerminal.isSuccess());
        }

        @Test
        void testAppendRuleTagWithLabel() {
            // Given
            int ruleIndex = 20;
            String label = "myRule";
            Pattern pattern = new PatternBuilder().appendRuleTag(ruleIndex, label).toPattern();

            DerivationTree parentMatching = Node.Rule.root(0);
            DerivationTree ruleNode = Node.Rule.attachTo(parentMatching, ruleIndex);

            // When
            MatchResult successResult = pattern.match(parentMatching);

            // Then
            assertTrue(successResult.isSuccess());
            assertNotNull(successResult.getLabel(label));
            assertEquals(ruleNode, successResult.getLabel(label));
        }

        @Test
        void testAppendSymbolTag() {
            // Given
            int symbolIndex = 30;
            Pattern pattern = new PatternBuilder().appendSymbolTag(symbolIndex).toPattern();

            DerivationTree parentMatching = Node.Rule.root(0);
            Node.Terminal.attachTo(parentMatching, Symbol.of().type(symbolIndex).get());

            DerivationTree parentNonMatching = Node.Rule.root(0);
            Node.Terminal.attachTo(parentNonMatching, Symbol.of().type(symbolIndex + 1).get());

            DerivationTree parentRule = Node.Rule.root(0);
            Node.Rule.attachTo(parentRule, 1);

            // When
            MatchResult successResult = pattern.match(parentMatching);
            MatchResult failureResultSymbol = pattern.match(parentNonMatching);
            MatchResult failureResultRule = pattern.match(parentRule);

            // Then
            assertTrue(successResult.isSuccess());
            assertFalse(failureResultSymbol.isSuccess());
            assertFalse(failureResultRule.isSuccess());
        }

        @Test
        void testAppendSymbolTagWithLabel() {
            // Given
            int symbolIndex = 40;
            String label = "mySymbol";
            Pattern pattern = new PatternBuilder().appendSymbolTag(symbolIndex, label).toPattern();

            DerivationTree parentMatching = Node.Rule.root(0);
            DerivationTree terminalNode = Node.Terminal.attachTo(parentMatching, Symbol.of().type(symbolIndex).get());

            // When
            MatchResult successResult = pattern.match(parentMatching);

            // Then
            assertTrue(successResult.isSuccess());
            assertNotNull(successResult.getLabel(label));
            assertEquals(terminalNode, successResult.getLabel(label));
        }


        @Test
        void testAppendWildcard() {
            // Given
            Pattern pattern = new PatternBuilder().appendWildcard().toPattern();

            DerivationTree ruleTree = Node.Rule.root(2);
            Node.Terminal.attachTo(ruleTree, Symbol.of().get());

            // When
            MatchResult successResultRule = pattern.match(ruleTree);

            // Then
            assertTrue(successResultRule.isSuccess());
        }
    }
}