package io.github.airbag.tree.pattern;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RuleVocabularyTest {

    @Test
    void testConstructorAndMaxTokenType() {
        String[] ruleNames = {"RULE1", "RULE2", "RULE3"};
        RuleVocabulary vocabulary = new RuleVocabulary(ruleNames);
        assertEquals(ruleNames.length, vocabulary.getMaxTokenType());

        String[] emptyRuleNames = {};
        RuleVocabulary emptyVocabulary = new RuleVocabulary(emptyRuleNames);
        assertEquals(0, emptyVocabulary.getMaxTokenType());
    }

    @Test
    void testLiteralName() {
        String[] ruleNames = {"RULE1"};
        RuleVocabulary vocabulary = new RuleVocabulary(ruleNames);
        assertNull(vocabulary.getLiteralName(0));
        assertNull(vocabulary.getLiteralName(100)); // Out of bounds
        assertNull(vocabulary.getLiteralName(-1)); // Special value
    }
    @Test
    void testSymbolicName() {
        String[] ruleNames = {"RULE0", "RULE1", "RULE2"};
        RuleVocabulary vocabulary = new RuleVocabulary(ruleNames);

        // Test valid token types
        assertEquals("RULE0", vocabulary.getSymbolicName(0));
        assertEquals("RULE1", vocabulary.getSymbolicName(1));
        assertEquals("RULE2", vocabulary.getSymbolicName(2));

        // Test out of bounds token types
        assertNull(vocabulary.getSymbolicName(3)); // Just outside bounds
        assertNull(vocabulary.getSymbolicName(100)); // Far out of bounds

        // Test -1 token type
        assertNull(vocabulary.getSymbolicName(-1));
    }

    @Test
    void testDisplayName() {
        String[] ruleNames = {"RULEA", "RULEB"};
        RuleVocabulary vocabulary = new RuleVocabulary(ruleNames);

        // Test valid token types
        assertEquals("RULEA", vocabulary.getDisplayName(0));
        assertEquals("RULEB", vocabulary.getDisplayName(1));

        // Test out of bounds token types
        assertNull(vocabulary.getDisplayName(2));
        assertNull(vocabulary.getDisplayName(99));

        // Test -1 token type
        assertNull(vocabulary.getDisplayName(-1));
    }
}
