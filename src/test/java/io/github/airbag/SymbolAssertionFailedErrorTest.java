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
                
                +-------+-------+----------+---------+
                | index | delta | expected | actual  |
                +-------+-------+----------+---------+
                |   0   |   ~   | (1 'a')  | (1 'b') |
                +-------+-------+----------+---------+
                """, error.getMessage());
    }

    @Test
    void testConstructorWithMultipleDeltas() {
        List<Symbol> expectedList = List.of(
                Symbol.of().text("a").type(1).index(0).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("b").type(1).index(1).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("c").type(1).index(2).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("d").type(1).index(3).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("e").type(1).index(4).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("f").type(1).index(5).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("g").type(1).index(6).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("h").type(1).index(7).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("i").type(1).index(8).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("j").type(1).index(9).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("k").type(1).index(10).start(0).stop(0).line(0).position(0).get()
        );

        List<Symbol> actualList = List.of(
                Symbol.of().text("a").type(1).index(0).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("x").type(1).index(1).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("c").type(1).index(2).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("y").type(1).index(3).start(0).stop(0).line(0).position(0).get(),
                Symbol.of().text("e").type(1).index(4).start(0).stop(0).line(0).position(0).get()
        );

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(SymbolFormatter.ANTLR, expectedList, actualList);

        String expectedMessage = """
                The expected symbol list does not match the actual:
                
                +-------+-------+-----------------------+----------------------+
                | index | delta |       expected        |        actual        |
                +-------+-------+-----------------------+----------------------+
                |   0   |       | [@0,0:0='a',<1>,0:0]  | [@0,0:0='a',<1>,0:0] |
                |   1   |   ~   | [@1,0:0='b',<1>,0:0]  | [@1,0:0='x',<1>,0:0] |
                |   2   |       | [@2,0:0='c',<1>,0:0]  | [@2,0:0='c',<1>,0:0] |
                |   3   |   ~   | [@3,0:0='d',<1>,0:0]  | [@3,0:0='y',<1>,0:0] |
                |   4   |       | [@4,0:0='e',<1>,0:0]  | [@4,0:0='e',<1>,0:0] |
                |   5   |   -   | [@5,0:0='f',<1>,0:0]  |                      |
                |   6   |   -   | [@6,0:0='g',<1>,0:0]  |                      |
                |   7   |   -   | [@7,0:0='h',<1>,0:0]  |                      |
                |   8   |   -   | [@8,0:0='i',<1>,0:0]  |                      |
                |   9   |   -   | [@9,0:0='j',<1>,0:0]  |                      |
                |  10   |   -   | [@10,0:0='k',<1>,0:0] |                      |
                +-------+-------+-----------------------+----------------------+
                """;
        assertEquals(expectedMessage, error.getMessage());
    }

    @Test
    void testConstructorWithInsertion() {
        List<Symbol> expectedList = List.of(
                Symbol.of().text("a").type(1).get()
        );

        List<Symbol> actualList = List.of(
                Symbol.of().text("a").type(1).get(),
                Symbol.of().text("b").type(1).get()
        );

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(SymbolFormatter.SIMPLE, expectedList, actualList);

        String expectedMessage = """
                The expected symbol list does not match the actual:

                +-------+-------+----------+---------+
                | index | delta | expected | actual  |
                +-------+-------+----------+---------+
                |   0   |       | (1 'a')  | (1 'a') |
                |   1   |   +   |          | (1 'b') |
                +-------+-------+----------+---------+
                """;
        assertEquals(expectedMessage, error.getMessage());
    }

    @Test
    void testConstructorWithDeletion() {
        List<Symbol> expectedList = List.of(
                Symbol.of().text("a").type(1).get(),
                Symbol.of().text("b").type(1).get()
        );

        List<Symbol> actualList = List.of(
                Symbol.of().text("a").type(1).get()
        );

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(SymbolFormatter.SIMPLE, expectedList, actualList);

        String expectedMessage = """
                The expected symbol list does not match the actual:

                +-------+-------+----------+---------+
                | index | delta | expected | actual  |
                +-------+-------+----------+---------+
                |   0   |       | (1 'a')  | (1 'a') |
                |   1   |   -   | (1 'b')  |         |
                +-------+-------+----------+---------+
                """;
        assertEquals(expectedMessage, error.getMessage());
    }

    @Test
    void testConstructorWithChange() {
        List<Symbol> expectedList = List.of(
                Symbol.of().text("a").type(1).get()
        );

        List<Symbol> actualList = List.of(
                Symbol.of().text("b").type(1).get()
        );

        SymbolAssertionFailedError error = new SymbolAssertionFailedError(SymbolFormatter.SIMPLE, expectedList, actualList);

        String expectedMessage = """
                The expected symbol list does not match the actual:

                +-------+-------+----------+---------+
                | index | delta | expected | actual  |
                +-------+-------+----------+---------+
                |   0   |   ~   | (1 'a')  | (1 'b') |
                +-------+-------+----------+---------+
                """;
        assertEquals(expectedMessage, error.getMessage());
    }
}