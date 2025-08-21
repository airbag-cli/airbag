package io.github.airbag;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.token.Tokens;
import io.github.airbag.tree.RuleValidationNode;
import io.github.airbag.tree.TerminalValidationNode;
import io.github.airbag.tree.ValidationTree;
import io.github.airbag.tree.Validator;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    @Test
    void matchSimpleRule() {
        final ExpressionLexer lexer = new ExpressionLexer(CharStreams.fromString("1"));
        final ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
        final ParseTree tree = parser.expr();
        final ValidationTree schema = RuleValidationNode.root(ExpressionParser.RULE_expr);
        TerminalValidationNode.attachTo(schema,
                Tokens.singleTokenOf().type(ExpressionLexer.INT).text("1").index(0).get());
        assertTrue(Validator.matches(schema, tree));
    }
}
