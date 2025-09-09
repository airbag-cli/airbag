package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AntlrFormatterTest {

    @Test
    void testChannelPrint() {
        SymbolFormatter formatter = SymbolFormatter.ANTLR;
        Symbol symbol = Symbol.of().index(1).start(0).stop(15).line(1).position(2).type(2).channel(3).text("On another channel").get();
        assertEquals("[@1,0:15='On another channel',<2>,channel=3,1:2]", formatter.format(symbol));
    }


    @Test
    void testChannelNotPrintedForDefault() {
        SymbolFormatter formatter = SymbolFormatter.ANTLR;
        Symbol symbol = Symbol.of()
                .index(1)
                .start(0)
                .stop(15)
                .line(1)
                .position(2)
                .type(2)
                .channel(0)
                .text("On default channel")
                .get();
        assertEquals("[@1,0:15='On default channel',<2>,1:2]", formatter.format(symbol));
    }

    @Test
    void testAntlrPattern() {
        SymbolFormatter formatter = SymbolFormatter.ofPattern("\\[@N,B:E='X',<L>[,%channel%=c],R:P\\]");
        Symbol symbol1 = Symbol.of()
                .index(1)
                .start(0)
                .stop(15)
                .line(1)
                .position(2)
                .type(2)
                .channel(0)
                .text("On default channel")
                .get();
        Symbol symbol2 = Symbol.of().index(1).start(0).stop(15).line(1).position(2).type(2).channel(3).text("On another channel").get();
        assertEquals("[@1,0:15='On default channel',<2,1:2]", formatter.format(symbol1));
        assertEquals("[@1,0:15='On another channel',<2>,channel=3,1:2]", formatter.format(symbol2));
    }
}
