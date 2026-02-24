package io.github.airbag.tree.query;

import io.github.airbag.Airbag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class QueryLexerTest {

    private static Airbag airbag;

    @BeforeAll
    static void setup() {
        airbag = Airbag.testLexer("io.github.airbag.tree.query.QueryLexer");
    }

    @Test
    void testLiterals() {
        airbag.assertTokens(" '/' '*' '//' '*' '!' '//' '/' EOF", "/*//*!///");
    }

    @Test
    void testFullQuery() {
        airbag.assertTokens("""
                '!'
                '//'
                (RULE 'rule1')
                '/'
                (RULE 'rule2')
                '/'
                (INDEX '1')
                '/'
                (TYPE '2')
                EOF
                """, "!//rule1/rule2/1/2");
    }

    @Test
    void testQueryWithRuleIndexAtEnd() {
        airbag.assertTokens("'/' (RULE 'rule') '/' (INDEX '15') '/' EOF", "/rule/15/");
    }

}