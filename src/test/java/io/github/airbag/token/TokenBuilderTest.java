package io.github.airbag.token;

import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenBuilderTest {

    @Test
    void createSymbolWithBuilder() {
        Token symbol = assertDoesNotThrow(() -> new TokenBuilder()
                .index(0)
                .start(1)
                .stop(2)
                .text("test")
                .type(3)
                .channel(4)
                .line(5)
                .position(6)
                .get());
        assertEquals(0, symbol.getTokenIndex());
        assertEquals(1, symbol.getStartIndex());
        assertEquals(2, symbol.getStopIndex());
        assertEquals("test", symbol.getText());
        assertEquals(3, symbol.getType());
        assertEquals(4, symbol.getChannel());
        assertEquals(5, symbol.getLine());
        assertEquals(6, symbol.getCharPositionInLine());
    }

    @Test
    void createWithResolve() {
        TokenBuilder builder = new TokenBuilder();
        builder.resolve(TokenField.INDEX, 0);
        builder.resolve(TokenField.START, 1);
        builder.resolve(TokenField.STOP, 2);
        builder.resolve(TokenField.TEXT, "test");
        builder.resolve(TokenField.TYPE, 3);
        builder.resolve(TokenField.CHANNEL, 4);
        builder.resolve(TokenField.LINE, 5);
        builder.resolve(TokenField.POSITION, 6);
        Token symbol = builder.get();

        assertEquals(0, symbol.getTokenIndex());
        assertEquals(1, symbol.getStartIndex());
        assertEquals(2, symbol.getStopIndex());
        assertEquals("test", symbol.getText());
        assertEquals(3, symbol.getType());
        assertEquals(4, symbol.getChannel());
        assertEquals(5, symbol.getLine());
        assertEquals(6, symbol.getCharPositionInLine());
    }

}