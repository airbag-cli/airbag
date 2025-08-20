package io.github.airbag;

import io.github.airbag.gen.ExpressionLexer;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTest {

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(ExpressionLexer.class);
    }

    @Test
    void fromInput() {
        final List<Token> tokenList1 = tokenProvider.fromInput("1+2");
        assertEquals(4, tokenList1.size());
        final List<Token> tokenList2 = tokenProvider.fromInput("3*4+5");
        assertEquals(6, tokenList2.size());
        assertNotEquals(tokenList1, tokenList2);
        //Assert that the first list is unaffected by the second call
        assertEquals(4, tokenList1.size());
    }

    @Test
    void fromInputTokens() {
        final List<Token> tokens = tokenProvider.fromInput("1+2");
        assertEquals(4, tokens.size());
        assertEquals(ExpressionLexer.INT, tokens.get(0).getType());
        assertEquals("1", tokens.get(0).getText());
        assertEquals(ExpressionLexer.T__3, tokens.get(1).getType());
        assertEquals("+", tokens.get(1).getText());
        assertEquals(ExpressionLexer.INT, tokens.get(2).getType());
        assertEquals("2", tokens.get(2).getText());
        assertEquals(Token.EOF, tokens.get(3).getType());
        assertEquals("<EOF>", tokens.get(3).getText());
    }

    @Test
    void fromSpec_emptyInput() {
        List<Token> tokens = tokenProvider.fromSpec("");
        assertTrue(tokens.isEmpty());
    }

    @Test
    void fromSpec_simpleLiterals() {
        List<Token> tokens = tokenProvider.fromSpec("'+' '-'");
        assertEquals(2, tokens.size()); // + -
        assertEquals(ExpressionLexer.T__3, tokens.get(0).getType());
        assertEquals("+", tokens.get(0).getText());
        assertEquals(ExpressionLexer.T__4, tokens.get(1).getType());
        assertEquals("-", tokens.get(1).getText());
    }

    @Test
    void fromSpec_compoundTokens() {
        List<Token> tokens = tokenProvider.fromSpec("(ID 'a') (INT '123')");
        assertEquals(2, tokens.size()); // ID INT
        assertEquals(ExpressionLexer.ID, tokens.get(0).getType());
        assertEquals("a", tokens.get(0).getText());
        assertEquals(ExpressionLexer.INT, tokens.get(1).getType());
        assertEquals("123", tokens.get(1).getText());
    }

    @Test
    void fromSpec_mixedTokens_javadocExample() {
        List<Token> tokens = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
        assertEquals(4, tokens.size()); // ID = INT

        assertEquals(ExpressionLexer.ID, tokens.get(0).getType());
        assertEquals("x", tokens.get(0).getText());

        assertEquals(ExpressionLexer.T__0, tokens.get(1).getType()); // '=' is T__0
        assertEquals("=", tokens.get(1).getText());

        assertEquals(ExpressionLexer.INT, tokens.get(2).getType());
        assertEquals("5", tokens.get(2).getText());

        assertEquals(ExpressionLexer.EOF, tokens.get(3).getType());
        assertEquals("<EOF>", tokens.get(3).getText());
    }
}
