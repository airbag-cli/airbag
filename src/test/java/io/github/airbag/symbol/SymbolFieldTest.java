package io.github.airbag.symbol;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolFieldTest {

    private static final Symbol SYMBOL = Symbol.of().index(0)
            .start(1)
            .stop(2)
            .text("test")
            .type(3)
            .channel(4)
            .line(5)
            .position(6)
            .get();

    @Test
    void accessType() {
        assertEquals(3, SymbolField.TYPE.access(SYMBOL));
    }

    @Test
    void accessText() {
        assertEquals("test", SymbolField.TEXT.access(SYMBOL));
    }

    @Test
    void accessIndex() {
        assertEquals(0, SymbolField.INDEX.access(SYMBOL));
    }

    @Test
    void accessLine() {
        assertEquals(5, SymbolField.LINE.access(SYMBOL));
    }

    @Test
    void accessPosition() {
        assertEquals(6, SymbolField.POSITION.access(SYMBOL));
    }

    @Test
    void accessChannel() {
        assertEquals(4, SymbolField.CHANNEL.access(SYMBOL));
    }

    @Test
    void accessStart() {
        assertEquals(1, SymbolField.START.access(SYMBOL));
    }

    @Test
    void accessStop() {
        assertEquals(2, SymbolField.STOP.access(SYMBOL));
    }

    @Test
    void resolveType() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.TYPE.resolve(builder, 10);
        Symbol symbol = builder.get();
        assertEquals(10, symbol.type());
    }

    @Test
    void resolveText() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.TEXT.resolve(builder, "resolved text");
        Symbol symbol = builder.get();
        assertEquals("resolved text", symbol.text());
    }

    @Test
    void resolveIndex() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.INDEX.resolve(builder, 100);
        Symbol symbol = builder.get();
        assertEquals(100, symbol.index());
    }

    @Test
    void resolveLine() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.LINE.resolve(builder, 101);
        Symbol symbol = builder.get();
        assertEquals(101, symbol.line());
    }

    @Test
    void resolvePosition() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.POSITION.resolve(builder, 102);
        Symbol symbol = builder.get();
        assertEquals(102, symbol.position());
    }

    @Test
    void resolveChannel() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.CHANNEL.resolve(builder, 103);
        Symbol symbol = builder.get();
        assertEquals(103, symbol.channel());
    }

    @Test
    void resolveStart() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.START.resolve(builder, 104);
        Symbol symbol = builder.get();
        assertEquals(104, symbol.start());
    }

    @Test
    void resolveStop() {
        Symbol.Builder builder = Symbol.of();
        SymbolField.STOP.resolve(builder, 105);
        Symbol symbol = builder.get();
        assertEquals(105, symbol.stop());
    }

    @Test
    void getDefaultType() {
        assertEquals(Symbol.INVALID_TYPE, SymbolField.TYPE.getDefault());
    }

    @Test
    void getDefaultText() {
        assertEquals("", SymbolField.TEXT.getDefault());
    }

    @Test
    void getDefaultIndex() {
        assertEquals(-1, SymbolField.INDEX.getDefault());
    }

    @Test
    void getDefaultLine() {
        assertEquals(-1, SymbolField.LINE.getDefault());
    }

    @Test
    void getDefaultPosition() {
        assertEquals(-1, SymbolField.POSITION.getDefault());
    }

    @Test
    void getDefaultChannel() {
        assertEquals(Symbol.DEFAULT_CHANNEL, SymbolField.CHANNEL.getDefault());
    }

    @Test
    void getDefaultStart() {
        assertEquals(-1, SymbolField.START.getDefault());
    }

    @Test
    void getDefaultStop() {
        assertEquals(-1, SymbolField.STOP.getDefault());
    }

    @Test
    void testFieldName() {
        assertEquals("type", SymbolField.TYPE.name());
        assertEquals("text", SymbolField.TEXT.name());
        assertEquals("index", SymbolField.INDEX.name());
        assertEquals("line", SymbolField.LINE.name());
        assertEquals("position", SymbolField.POSITION.name());
        assertEquals("channel", SymbolField.CHANNEL.name());
        assertEquals("start", SymbolField.START.name());
        assertEquals("stop", SymbolField.STOP.name());
    }

    @Test
    void testAllFields() {
        assertEquals(8, SymbolField.all().size());
        assertTrue(SymbolField.all().contains(SymbolField.TYPE));
        assertTrue(SymbolField.all().contains(SymbolField.TEXT));
        assertTrue(SymbolField.all().contains(SymbolField.INDEX));
        assertTrue(SymbolField.all().contains(SymbolField.LINE));
        assertTrue(SymbolField.all().contains(SymbolField.POSITION));
        assertTrue(SymbolField.all().contains(SymbolField.CHANNEL));
        assertTrue(SymbolField.all().contains(SymbolField.START));
        assertTrue(SymbolField.all().contains(SymbolField.STOP));
    }

    @Test
    void testSimpleFields() {
        assertEquals(4, SymbolField.simple().size());
        assertTrue(SymbolField.simple().contains(SymbolField.TYPE));
        assertTrue(SymbolField.simple().contains(SymbolField.TEXT));
        assertTrue(SymbolField.simple().contains(SymbolField.INDEX));
        assertTrue(SymbolField.simple().contains(SymbolField.CHANNEL));
        assertFalse(SymbolField.simple().contains(SymbolField.LINE)); // Ensure it does not contain
    }

    @Test
    void testEqualizer() {
        Symbol symbol1 = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);
        Symbol symbol2 = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);
        Symbol symbol3 = new Symbol(99, 1, 2, "test", 3, 4, 5, 6); // Different index

        BiPredicate<Symbol, Symbol> fullEqualizer = SymbolField.equalizer(SymbolField.all());
        assertTrue(fullEqualizer.test(symbol1, symbol2));
        assertFalse(fullEqualizer.test(symbol1, symbol3));

        BiPredicate<Symbol, Symbol> partialEqualizer = SymbolField.equalizer(Set.of(SymbolField.TEXT, SymbolField.TYPE));
        assertTrue(partialEqualizer.test(symbol1, symbol2));
        assertTrue(partialEqualizer.test(symbol1, symbol3)); // Should still be true as index is not compared

        Symbol symbol4 = new Symbol(0, 1, 2, "different", 3, 4, 5, 6); // Different text
        assertFalse(partialEqualizer.test(symbol1, symbol4));
    }

    @Test
    void testName() {
        assertAll(
                () -> assertEquals("index", SymbolField.INDEX.name()),
                () -> assertEquals("start", SymbolField.START.name()),
                () -> assertEquals("stop", SymbolField.STOP.name()),
                () -> assertEquals("text", SymbolField.TEXT.name()),
                () -> assertEquals("type", SymbolField.TYPE.name()),
                () -> assertEquals("channel", SymbolField.CHANNEL.name()),
                () -> assertEquals("line", SymbolField.LINE.name()),
                () -> assertEquals("position", SymbolField.POSITION.name())
                );
    }
}