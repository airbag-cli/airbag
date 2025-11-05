package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymbolAssertionFailedErrorTest {

    @Test
    void testConstructorWithTwoSymbols() {
        Symbol expected = Symbol.of().text("a").type(1).get();
        Symbol actual = Symbol.of().text("b").type(1).get();

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(expected, actual);

        String expectedMessage = """
                +--------------------------------------------------------------------+------------------------------------------------------------------+
                | Expected                                                           | Actual                                                           |
                +--------------------------------------------------------------------+------------------------------------------------------------------+
                | [@0,0:0='a',<1>,0:0]                                                | [@0,0:0='b',<1>,0:0]                                            |
                +--------------------------------------------------------------------+------------------------------------------------------------------+""";
        assertEquals(expectedMessage, error.getMessage());
    }
}