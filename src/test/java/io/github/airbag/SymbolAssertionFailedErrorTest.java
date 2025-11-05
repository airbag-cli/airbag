package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SymbolAssertionFailedErrorTest {

    @Test
    void testConstructorWithTwoSymbols() {
        Symbol expected = Symbol.of().text("a").type(1).get();
        Symbol actual = Symbol.of().text("b").type(1).get();

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(expected, actual);

        assertEquals("""
                The expected symbol list does not match the actual:
                
                +---+---+---------------------------+---------------------------+
                | i | d | Expected                  | Actual                    |
                +---+---+---------------------------+---------------------------+
                | 0 | ~ | [@-1,-1:-1='a',<1>,-1:-1] | [@-1,-1:-1='b',<1>,-1:-1] |
                +---+---+---------------------------+---------------------------+
                """, error.getMessage());
    }

    @Test
    void testConstructorWithMultipleDeltas() {
        List<Symbol> expectedList = List.of(
                Symbol.of().text("a").type(1).index(0).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("b").type(1).index(1).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("c").type(1).index(2).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("d").type(1).index(3).start(0).stop(0).line(0).position(0).get()
        );

        List<Symbol> actualList = List.of(
                Symbol.of().text("a").type(1).index(0).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("x").type(1).index(1).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("c").type(1).index(2).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("y").type(1).index(3).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("z").type(1).index(4).start(0).stop(0).line(0).position(0).get()
        );

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(SymbolFormatter.ANTLR, expectedList, actualList);

        String expectedMessage = """
                The expected symbol list does not match the actual:
                
                +---+---+----------------------+----------------------+
                | i | d | Expected             | Actual               |
                +---+---+----------------------+----------------------+
                | 0 |   | [@0,0:0='a',<1>,0:0] | [@0,0:0='a',<1>,0:0] |
                | 1 | ~ | [@1,0:0='b',<1>,0:0] | [@1,0:0='x',<1>,0:0] |
                | 2 |   | [@2,0:0='c',<1>,0:0] | [@2,0:0='c',<1>,0:0] |
                | 3 | ~ | [@3,0:0='d',<1>,0:0] | [@3,0:0='y',<1>,0:0] |
                | 4 | ~ |                      | [@4,0:0='z',<1>,0:0] |
                +---+---+----------------------+----------------------+
                """;
        assertEquals(expectedMessage, error.getMessage());
    }
}