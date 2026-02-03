package io.github.airbag.example;

import io.github.airbag.Airbag;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionLexerTest {

    private Airbag airbag;
    private SymbolProvider provider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testLexer("io.github.airbag.gen.ExpressionLexer");
        provider = airbag.getSymbolProvider();
    }

    @Test
    void testID() {
        //Create an expected list of symbols
        List<Symbol> expected = provider.fromSpec("""
                (ID 'x')
                (ID 'myVariable')
                (ID 'y')
                EOF
                """);

        //Let the lexer tokenize an actual input string
        List<Symbol> actual = provider.fromInput("x myVariable y");

        //Compare the expected and actual list
        airbag.assertSymbolList(expected, actual);
    }

    @Test
    void testINT() {
        //Expected string representation and actual tokenized output can also be compared directly
        airbag.assertSymbols("(INT '15') (INT '-10') EOF", "15 -10");
    }

    @Test
    void testNEWLINE() {
        //It is possible to use a different format for parsing symbols/tokens to capture more details
        //if needed
        provider.setFormatter(SymbolFormatter.ANTLR);

        //Expected
        List<Symbol> expected = provider.fromSpec("""
                [@0,0:0='\\n',<NEWLINE>,1:0]
                [@1,2:3='\\r\\n',<NEWLINE>,2:1]
                [@2,4:3='<EOF>',<EOF>,3:0]
                """);

        //Actual
        List<Symbol> actual = provider.fromInput("\n \r\n");

        //Compare results
        airbag.assertSymbolList(expected, actual);
    }

    @Test
    void testLiterals() {
        //It is also possible to define a custom pattern for parsing symbols/tokens
        SymbolFormatter formatter = SymbolFormatter.ofPattern("s: \"X\"|'LITERAL': \"l\"");
        provider.setFormatter(formatter);

        //Expected
        List<Symbol> expected = provider.fromSpec("""
                ID: "x"
                INT: "10"
                LITERAL: "'-'"
                LITERAL: "'+'"
                EOF: "<EOF>\"""");

        //Actual
        List<Symbol> actual = provider.fromInput("x 10 - +");

        //Compare results
        airbag.assertSymbolList(expected, actual);
    }
}