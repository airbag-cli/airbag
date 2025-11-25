package io.github.airbag.symbol;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SymbolTest {

    @Test
    void createSymbolWithBuilder() {
        Symbol symbol = assertDoesNotThrow(() -> Symbol.of()
                .index(0)
                .start(1)
                .stop(2)
                .text("test")
                .type(3)
                .channel(4)
                .line(5)
                .position(6)
                .get());
        assertEquals(0, symbol.index());
        assertEquals(1, symbol.start());
        assertEquals(2, symbol.stop());
        assertEquals("test", symbol.text());
        assertEquals(3, symbol.type());
        assertEquals(4, symbol.channel());
        assertEquals(5, symbol.line());
        assertEquals(6, symbol.position());
    }

    @Test
    void createSymbolWithConstructor() {
        Symbol symbol = assertDoesNotThrow(() -> new Symbol(0, 1, 2, "test", 3, 4, 5, 6));
        assertEquals(0, symbol.index());
        assertEquals(1, symbol.start());
        assertEquals(2, symbol.stop());
        assertEquals("test", symbol.text());
        assertEquals(3, symbol.type());
        assertEquals(4, symbol.channel());
        assertEquals(5, symbol.line());
        assertEquals(6, symbol.position());
    }

    @Test
    void createSymbolFromToken() {
        CommonToken token = new CommonToken(3, "test");
        token.setTokenIndex(0);
        token.setStartIndex(1);
        token.setStopIndex(2);
        token.setChannel(4);
        token.setLine(5);
        token.setCharPositionInLine(6);

        Symbol symbol = new Symbol(token);

        assertEquals(token.getTokenIndex(), symbol.index());
        assertEquals(token.getStartIndex(), symbol.start());
        assertEquals(token.getStopIndex(), symbol.stop());
        assertEquals(token.getText(), symbol.text());
        assertEquals(token.getType(), symbol.type());
        assertEquals(token.getChannel(), symbol.channel());
        assertEquals(token.getLine(), symbol.line());
        assertEquals(token.getCharPositionInLine(), symbol.position());
    }

    @Test
    void createWithResolve() {
        Symbol.Builder builder = Symbol.of();
        builder.resolve(SymbolField.INDEX, 0);
        builder.resolve(SymbolField.START, 1);
        builder.resolve(SymbolField.STOP, 2);
        builder.resolve(SymbolField.TEXT, "test");
        builder.resolve(SymbolField.TYPE, 3);
        builder.resolve(SymbolField.CHANNEL, 4);
        builder.resolve(SymbolField.LINE, 5);
        builder.resolve(SymbolField.POSITION, 6);
        Symbol symbol = builder.get();

        assertEquals(0, symbol.index());
        assertEquals(1, symbol.start());
        assertEquals(2, symbol.stop());
        assertEquals("test", symbol.text());
        assertEquals(3, symbol.type());
        assertEquals(4, symbol.channel());
        assertEquals(5, symbol.line());
        assertEquals(6, symbol.position());
    }

    @Test
    void convertToToken() {
        Symbol symbol = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);
        Token token = symbol.toToken();

        assertEquals(symbol.index(), token.getTokenIndex());
        assertEquals(symbol.start(), token.getStartIndex());
        assertEquals(symbol.stop(), token.getStopIndex());
        assertEquals(symbol.text(), token.getText());
        assertEquals(symbol.type(), token.getType());
        assertEquals(symbol.channel(), token.getChannel());
        assertEquals(symbol.line(), token.getLine());
        assertEquals(symbol.position(), token.getCharPositionInLine());
    }

    @Test
    void testEqualsAndHashCode() {
        Symbol symbol1 = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);
        Symbol symbol2 = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);

        assertEquals(symbol1, symbol2);
        assertEquals(symbol1.hashCode(), symbol2.hashCode());

        assertNotEquals(new Symbol(99, 1, 2, "test", 3, 4, 5, 6), symbol1); // Different index
        assertNotEquals(new Symbol(0, 99, 2, "test", 3, 4, 5, 6), symbol1); // Different start
        assertNotEquals(new Symbol(0, 1, 99, "test", 3, 4, 5, 6), symbol1); // Different stop
        assertNotEquals(new Symbol(0, 1, 2, "different", 3, 4, 5, 6), symbol1); // Different text
        assertNotEquals(new Symbol(0, 1, 2, "test", 99, 4, 5, 6), symbol1); // Different type
        assertNotEquals(new Symbol(0, 1, 2, "test", 3, 99, 5, 6), symbol1); // Different channel
        assertNotEquals(new Symbol(0, 1, 2, "test", 3, 4, 99, 6), symbol1); // Different line
        assertNotEquals(new Symbol(0, 1, 2, "test", 3, 4, 5, 99), symbol1); // Different position
        assertNotEquals(null, symbol1);
        assertNotEquals(new Object(), symbol1);
    }

    @Test
    void createSymbolWithBuilderDefaults() {
        Symbol symbol = Symbol.of().get();
        assertEquals(SymbolField.INDEX.getDefault(), symbol.index());
        assertEquals(SymbolField.START.getDefault(), symbol.start());
        assertEquals(SymbolField.STOP.getDefault(), symbol.stop());
        assertEquals(SymbolField.TEXT.getDefault(), symbol.text());
        assertEquals(SymbolField.TYPE.getDefault(), symbol.type());
        assertEquals(SymbolField.CHANNEL.getDefault(), symbol.channel());
        assertEquals(SymbolField.LINE.getDefault(), symbol.line());
        assertEquals(SymbolField.POSITION.getDefault(), symbol.position());
    }

    @Test
    void testToString() {
        Symbol symbol = new Symbol(0, 1, 2, "test", 3, 4, 5, 6);
        String toString = assertDoesNotThrow(symbol::toString);
        assertNotNull(toString);
        // Assumes ANTLR's default format e.g., "[@0,1:2='test',<3>,5:6]"
        assertTrue(toString.contains("'test'"));
        assertTrue(toString.contains("<3>"));
        assertTrue(toString.contains("@0"));
        assertTrue(toString.contains("5:6"));
    }
}