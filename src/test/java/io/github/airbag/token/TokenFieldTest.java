package io.github.airbag.token;

import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenFieldTest {

    private static final Token SYMBOL = new TokenBuilder().index(0)
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
        assertEquals(3, TokenField.TYPE.access(SYMBOL));
    }

    @Test
    void accessText() {
        assertEquals("test", TokenField.TEXT.access(SYMBOL));
    }

    @Test
    void accessIndex() {
        assertEquals(0, TokenField.INDEX.access(SYMBOL));
    }

    @Test
    void accessLine() {
        assertEquals(5, TokenField.LINE.access(SYMBOL));
    }

    @Test
    void accessPosition() {
        assertEquals(6, TokenField.POSITION.access(SYMBOL));
    }

    @Test
    void accessChannel() {
        assertEquals(4, TokenField.CHANNEL.access(SYMBOL));
    }

    @Test
    void accessStart() {
        assertEquals(1, TokenField.START.access(SYMBOL));
    }

    @Test
    void accessStop() {
        assertEquals(2, TokenField.STOP.access(SYMBOL));
    }

    @Test
    void resolveType() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.TYPE.resolve(builder, 10);
        Token symbol = builder.get();
        assertEquals(10, symbol.getType());
    }

    @Test
    void resolveText() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.TEXT.resolve(builder, "resolved getText");
        Token symbol = builder.get();
        assertEquals("resolved getText", symbol.getText());
    }

    @Test
    void resolveIndex() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.INDEX.resolve(builder, 100);
        Token symbol = builder.get();
        assertEquals(100, symbol.getTokenIndex());
    }

    @Test
    void resolveLine() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.LINE.resolve(builder, 101);
        Token symbol = builder.get();
        assertEquals(101, symbol.getLine());
    }

    @Test
    void resolvePosition() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.POSITION.resolve(builder, 102);
        Token symbol = builder.get();
        assertEquals(102, symbol.getCharPositionInLine());
    }

    @Test
    void resolveChannel() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.CHANNEL.resolve(builder, 103);
        Token symbol = builder.get();
        assertEquals(103, symbol.getChannel());
    }

    @Test
    void resolveStart() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.START.resolve(builder, 104);
        Token symbol = builder.get();
        assertEquals(104, symbol.getStartIndex());
    }

    @Test
    void resolveStop() {
        TokenBuilder builder = new TokenBuilder();
        TokenField.STOP.resolve(builder, 105);
        Token symbol = builder.get();
        assertEquals(105, symbol.getStopIndex());
    }

    @Test
    void getDefaultType() {
        assertEquals(Token.INVALID_TYPE, TokenField.TYPE.getDefault());
    }

    @Test
    void getDefaultText() {
        assertEquals("", TokenField.TEXT.getDefault());
    }

    @Test
    void getDefaultIndex() {
        assertEquals(-1, TokenField.INDEX.getDefault());
    }

    @Test
    void getDefaultLine() {
        assertEquals(-1, TokenField.LINE.getDefault());
    }

    @Test
    void getDefaultPosition() {
        assertEquals(-1, TokenField.POSITION.getDefault());
    }

    @Test
    void getDefaultChannel() {
        assertEquals(Token.DEFAULT_CHANNEL, TokenField.CHANNEL.getDefault());
    }

    @Test
    void getDefaultStart() {
        assertEquals(-1, TokenField.START.getDefault());
    }

    @Test
    void getDefaultStop() {
        assertEquals(-1, TokenField.STOP.getDefault());
    }

    @Test
    void testFieldName() {
        assertEquals("type", TokenField.TYPE.name());
        assertEquals("getText", TokenField.TEXT.name());
        assertEquals("getTokenIndex", TokenField.INDEX.name());
        assertEquals("getLine", TokenField.LINE.name());
        assertEquals("getCharPositionInLine", TokenField.POSITION.name());
        assertEquals("getChannel", TokenField.CHANNEL.name());
        assertEquals("getStartIndex", TokenField.START.name());
        assertEquals("getStopIndex", TokenField.STOP.name());
    }

    @Test
    void testAllFields() {
        assertEquals(8, TokenField.all().size());
        assertTrue(TokenField.all().contains(TokenField.TYPE));
        assertTrue(TokenField.all().contains(TokenField.TEXT));
        assertTrue(TokenField.all().contains(TokenField.INDEX));
        assertTrue(TokenField.all().contains(TokenField.LINE));
        assertTrue(TokenField.all().contains(TokenField.POSITION));
        assertTrue(TokenField.all().contains(TokenField.CHANNEL));
        assertTrue(TokenField.all().contains(TokenField.START));
        assertTrue(TokenField.all().contains(TokenField.STOP));
    }

    @Test
    void testSimpleFields() {
        assertEquals(4, TokenField.simple().size());
        assertTrue(TokenField.simple().contains(TokenField.TYPE));
        assertTrue(TokenField.simple().contains(TokenField.TEXT));
        assertTrue(TokenField.simple().contains(TokenField.INDEX));
        assertTrue(TokenField.simple().contains(TokenField.CHANNEL));
        assertFalse(TokenField.simple().contains(TokenField.LINE)); // Ensure it does not contain
    }

    @Test
    void testName() {
        assertAll(
                () -> assertEquals("getTokenIndex", TokenField.INDEX.name()),
                () -> assertEquals("getStartIndex", TokenField.START.name()),
                () -> assertEquals("getStopIndex", TokenField.STOP.name()),
                () -> assertEquals("getText", TokenField.TEXT.name()),
                () -> assertEquals("type", TokenField.TYPE.name()),
                () -> assertEquals("getChannel", TokenField.CHANNEL.name()),
                () -> assertEquals("getLine", TokenField.LINE.name()),
                () -> assertEquals("getCharPositionInLine", TokenField.POSITION.name())
                );
    }
}