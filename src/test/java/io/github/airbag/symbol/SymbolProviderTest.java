package io.github.airbag.symbol;

import io.github.airbag.gen.ExpressionLexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SymbolProviderTest {

    private SymbolProvider symbolProvider;

    @BeforeEach
    void setUp() {
        symbolProvider = new SymbolProvider(ExpressionLexer.class);
    }

    @Test
    void fromInput() {
        final List<Symbol> symbolList1 = symbolProvider.fromInput("1+2");
        assertEquals(4, symbolList1.size());
        final List<Symbol> symbolList2 = symbolProvider.fromInput("3*4+5");
        assertEquals(6, symbolList2.size());
        assertNotEquals(symbolList1, symbolList2);
        //Assert that the first list is unaffected by the second call
        assertEquals(4, symbolList1.size());
    }

    @Test
    void fromInputSymbols() {
        final List<Symbol> symbols = symbolProvider.fromInput("1+2");
        assertEquals(4, symbols.size());
        assertEquals(ExpressionLexer.INT, symbols.get(0).type());
        assertEquals("1", symbols.get(0).text());
        assertEquals(ExpressionLexer.T__3, symbols.get(1).type());
        assertEquals("+", symbols.get(1).text());
        assertEquals(ExpressionLexer.INT, symbols.get(2).type());
        assertEquals("2", symbols.get(2).text());
        assertEquals(Symbol.EOF, symbols.get(3).type());
        assertEquals("<EOF>", symbols.get(3).text());
    }

    @Test
    void fromSpec_emptyInput() {
        List<Symbol> symbols = symbolProvider.fromSpec("");
        assertTrue(symbols.isEmpty());
    }

    @Test
    void fromSpec_simpleLiterals() {
        List<Symbol> symbols = symbolProvider.fromSpec("'+' '-'");
        assertEquals(2, symbols.size()); // + -
        assertEquals(ExpressionLexer.T__3, symbols.get(0).type());
        assertEquals("+", symbols.get(0).text());
        assertEquals(ExpressionLexer.T__4, symbols.get(1).type());
        assertEquals("-", symbols.get(1).text());
    }

    @Test
    void fromSpec_compoundSymbols() {
        List<Symbol> symbols = symbolProvider.fromSpec("(ID 'a') (INT '123')");
        assertEquals(2, symbols.size()); // ID INT
        assertEquals(ExpressionLexer.ID, symbols.get(0).type());
        assertEquals("a", symbols.get(0).text());
        assertEquals(ExpressionLexer.INT, symbols.get(1).type());
        assertEquals("123", symbols.get(1).text());
    }

    @Test
    void fromSpec_mixedSymbols_javadocExample() {
        List<Symbol> symbols = symbolProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
        assertEquals(4, symbols.size()); // ID = INT

        assertEquals(ExpressionLexer.ID, symbols.get(0).type());
        assertEquals("x", symbols.get(0).text());

        assertEquals(ExpressionLexer.T__0, symbols.get(1).type()); // '=' is T__0
        assertEquals("=", symbols.get(1).text());

        assertEquals(ExpressionLexer.INT, symbols.get(2).type());
        assertEquals("5", symbols.get(2).text());

        assertEquals(ExpressionLexer.EOF, symbols.get(3).type());
        assertEquals("<EOF>", symbols.get(3).text());
    }
}