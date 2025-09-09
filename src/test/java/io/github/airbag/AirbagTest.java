package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirbagTest {

    private Airbag airbag;
    private SymbolProvider symbolProvider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        symbolProvider = airbag.getSymbolProvider();
    }

    @Test
    void testEqualSymbolList() {
        List<Symbol> expected = symbolProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
        List<Symbol> actual = symbolProvider.fromInput("x = 5");
        assertDoesNotThrow(() -> airbag.assertSymbolList(expected, actual));
    }

    @Test
    void testAssertionErrorThrown() {
        List<Symbol> expected = symbolProvider.fromSpec("(ID 'x') (INT '5') (NEWLINE '\n') EOF");
        List<Symbol> actual = symbolProvider.fromInput("x = 5\n");
        assertThrows(AssertionFailedError.class, () -> airbag.assertSymbolList(expected, actual));
    }

}
