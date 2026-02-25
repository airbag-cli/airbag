package io.github.airbag.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

class FormatterParsePositionTest {

    private FormatterParsePosition position;

    @BeforeEach
    void setUp() {
        position = new FormatterParsePosition(0);
    }

    @Test
    void testConstructorAndInitialIndex() {
        assertEquals(0, position.getIndex(), "Initial getTokenIndex should be 0");
        assertFalse(position.isSymbolIndex(), "isSymbolIndex should be false initially");
        assertTrue(position.getMessages().isEmpty(), "Messages should be empty initially");

        FormatterParsePosition newPosition = new FormatterParsePosition(5);
        assertEquals(5, newPosition.getIndex(), "Initial getTokenIndex should be 5");
    }

    @Test
    void testAppendMessage() {
        position.appendMessage("Error 1");
        Collection<String> messages = position.getMessages();
        assertFalse(messages.isEmpty(), "Messages should not be empty after appending");
        assertEquals(1, messages.size(), "Should have one message");
        assertTrue(messages.contains("Error 1"), "Messages should contain 'Error 1'");

        position.appendMessage("Error 2");
        assertEquals(2, messages.size(), "Should have two messages");
        assertTrue(messages.contains("Error 2"), "Messages should contain 'Error 2'");

        // Test appending duplicate message, should not increase size due to TreeSet
        position.appendMessage("Error 1");
        assertEquals(2, messages.size(), "Should still have two messages after appending duplicate");
    }

    @Test
    void testSetMessage() {
        position.appendMessage("Initial Error");
        assertEquals(1, position.getMessages().size());

        position.setMessage("New Error");
        Collection<String> messages = position.getMessages();
        assertEquals(1, messages.size(), "Should have one message after setting a new one");
        assertTrue(messages.contains("New Error"), "Messages should contain 'New Error'");
        assertFalse(messages.contains("Initial Error"), "Messages should not contain old message");
    }

    @Test
    void testGetMessageAggregated() {
        position.appendMessage("First message");
        position.appendMessage("Second message");

        String expectedMessage = String.join(System.lineSeparator(), "First message", "Second message");
        // NavigableSet stores elements in natural order, so "First message" then "Second message"
        assertEquals(expectedMessage.replace("%n", System.lineSeparator()), position.getMessage(), "Aggregated message should contain all messages, sorted naturally");
    }

    @Test
    void testSymbolIndex() {
        assertEquals(-1, position.getSymbolIndex(), "Token getTokenIndex should be -1 initially");
        assertFalse(position.isSymbolIndex(), "isSymbolIndex should be false initially");

        position.setSymbolIndex(10);
        assertEquals(10, position.getSymbolIndex(), "Token getTokenIndex should be 10");
        assertTrue(position.isSymbolIndex(), "isSymbolIndex should be true after setting a valid getTokenIndex");

        position.setSymbolIndex(-1);
        assertEquals(-1, position.getSymbolIndex(), "Token getTokenIndex should be -1 after resetting");
        assertFalse(position.isSymbolIndex(), "isSymbolIndex should be false after resetting to -1");
    }

    @Test
    void testToString() {
        // Test default state
        String expectedDefault = "FormatterParsePosition{getTokenIndex=0, symbolIndex=-1, messages=[]}";
        assertEquals(expectedDefault, position.toString(), "toString should match default state");

        // Test with messages
        position.appendMessage("Error A");
        position.appendMessage("Error B");
        // Messages are stored in a TreeSet, so they will be sorted alphabetically
        String expectedWithMessages = "FormatterParsePosition{getTokenIndex=0, symbolIndex=-1, messages=[Error A, Error B]}";
        assertEquals(expectedWithMessages, position.toString(), "toString should include messages");

        // Test with symbolIndex
        position.setSymbolIndex(5);
        String expectedWithSymbolIndex = "FormatterParsePosition{getTokenIndex=0, symbolIndex=5, messages=[Error A, Error B]}";
        assertEquals(expectedWithSymbolIndex, position.toString(), "toString should include symbolIndex");

        // Test with different initial getTokenIndex
        FormatterParsePosition indexedPosition = new FormatterParsePosition(10);
        indexedPosition.appendMessage("Indexed Error");
        String expectedIndexed = "FormatterParsePosition{getTokenIndex=10, symbolIndex=-1, messages=[Indexed Error]}";
        assertEquals(expectedIndexed, indexedPosition.toString(), "toString should reflect initial getTokenIndex and messages");
    }
}