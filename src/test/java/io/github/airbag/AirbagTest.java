package io.github.airbag;

import io.github.airbag.token.TokenProvider;
import io.github.airbag.tree.TreeProvider;
import io.github.airbag.tree.ValidationTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AirbagTest {

    private Airbag airbag;
    private TokenProvider tokenProvider;
    private TreeProvider treeProvider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        tokenProvider = airbag.getProvider();
        treeProvider = airbag.getTreeProvider();
    }

    @Test
    void testEqualTokenList() {
        List<Token> expected = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
        List<Token> actual = tokenProvider.fromInput("x = 5");
        assertDoesNotThrow(() -> airbag.assertTokenList(expected, actual));
    }

    @Test
    void testAssertionErrorThrown() {
        List<Token> expected = tokenProvider.fromSpec("(ID 'x') (INT '5') (NEWLINE '\n') EOF");
        List<Token> actual = tokenProvider.fromInput("x = 5\n");
        assertThrows(AssertionFailedError.class, () -> airbag.assertTokenList(expected, actual));
    }

    @Test
    void testValidationTree() {
        ValidationTree expected = treeProvider.fromSpec("""
                (prog
                    (stat
                        (ID 'x') '=' (expr (INT '5')) (NEWLINE '%n')
                    )
                    EOF
                )""".formatted());
        ParseTree actual = treeProvider.fromSource(tokenProvider.fromInput("x = 5\n"), "prog");
        assertDoesNotThrow(() -> airbag.assertParseTree(expected, actual));
    }
}
