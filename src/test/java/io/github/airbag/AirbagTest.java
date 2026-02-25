package io.github.airbag;

import io.github.airbag.token.TokenParseException;
import io.github.airbag.token.TokenProvider;
import io.github.airbag.tree.TreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirbagTest {

    private Airbag airbag;
    private TokenProvider symbolProvider;
    private TreeProvider treeProvider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        symbolProvider = airbag.getSymbolProvider();
        treeProvider = airbag.getTreeProvider();
    }

    @Test
    void testFailureOnSyntaxError() {
        var e = assertThrows(TokenParseException.class, () -> symbolProvider.actual("$Failure$"));
        assertEquals("""
            Parse failed at line 1 with position 0:
            token recognition error at: '$'

            $Failure$
            """, e.getMessage());
    }
}