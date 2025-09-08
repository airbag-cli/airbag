package io.github.airbag;

import io.github.airbag.token.Tokens;
import io.github.airbag.token.format.TokenFormatter;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AntlrFormatterTest {

    @Test
    void testChannelPrint() {
        TokenFormatter formatter = TokenFormatter.ANTLR;
        Token token = Tokens.singleTokenOf().index(1).startIndex(0).stopIndex(15).line(1).charPositionInLine(2).type(2).channel(3).text("On another channel").get();
        assertEquals("[@1,0:15='On another channel',<2>,channel=3,1:2]", formatter.format(token));
    }


    @Test
    void testChannelNotPrintedForDefault() {
        TokenFormatter formatter = TokenFormatter.ANTLR;
        Token token = Tokens.singleTokenOf()
                .index(1)
                .startIndex(0)
                .stopIndex(15)
                .line(1)
                .charPositionInLine(2)
                .type(2)
                .channel(0)
                .text("On default channel")
                .get();
        assertEquals("[@1,0:15='On default channel',<2>,1:2]", formatter.format(token));
    }

    @Test
    void testAntlrPattern() {
        TokenFormatter formatter = TokenFormatter.ofPattern("\\[@N,B:E='X',<L>[,%channel%=c],R:P\\]");
        Token token1 = Tokens.singleTokenOf()
                .index(1)
                .startIndex(0)
                .stopIndex(15)
                .line(1)
                .charPositionInLine(2)
                .type(2)
                .channel(0)
                .text("On default channel")
                .get();
        Token token2 = Tokens.singleTokenOf().index(1).startIndex(0).stopIndex(15).line(1).charPositionInLine(2).type(2).channel(3).text("On another channel").get();
        assertEquals("[@1,0:15='On default channel',<2,1:2]", formatter.format(token1));
        assertEquals("[@1,0:15='On another channel',<2>,channel=3,1:2]", formatter.format(token2));
    }
}
