package io.github.airbag.example;

import io.github.airbag.Airbag;
import io.github.airbag.token.TokenFormatter;
import io.github.airbag.token.TokenProvider;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionLexerTest {

    private Airbag airbag;
    private TokenProvider provider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testLexer("io.github.airbag.gen.ExpressionLexer");
        provider = airbag.getSymbolProvider();
    }

    @Test
    void testID() {
        //Create an expected list of symbols
        List<Token> expected = provider.fromSpec("""
                (ID 'x')
                (ID 'myVariable')
                (ID 'y')
                EOF
                """);

        //Let the lexer tokenize an actual input string
        List<Token> actual = provider.fromInput("x myVariable y");

        //Compare the expected and actual list
        airbag.assertTokens(expected, actual);
    }

    @Test
    void testINT() {
        //Expected string representation and actual tokenized output can also be compared directly
        airbag.assertTokens("(INT '15') (INT '-10') EOF", "15 -10");
    }

    @Test
    void testNEWLINE() {
        //It is possible to use a different format for parsing symbols/tokens to capture more details
        //if needed
        provider.setFormatter(TokenFormatter.ANTLR);

        //Expected
        List<Token> expected = provider.fromSpec("""
                [@0,0:0='\\n',<NEWLINE>,1:0]
                [@1,2:3='\\r\\n',<NEWLINE>,2:1]
                [@2,4:3='<EOF>',<EOF>,3:0]
                """);

        //Actual
        List<Token> actual = provider.fromInput("\n \r\n");

        //Compare results
        airbag.assertTokens(expected, actual);
    }

    @Test
    void testLiterals() {
        //It is also possible to define a custom pattern for parsing symbols/tokens
        TokenFormatter formatter = TokenFormatter.ofPattern("s: \"X\"|'LITERAL': \"l\"");
        provider.setFormatter(formatter);

        //Expected
        List<Token> expected = provider.fromSpec("""
                ID: "x"
                INT: "10"
                LITERAL: "'-'"
                LITERAL: "'+'"
                EOF: "<EOF>\"""");

        //Actual
        List<Token> actual = provider.fromInput("x 10 - +");

        //Compare results
        airbag.assertTokens(expected, actual);
    }
}