package io.github.airbag.tree;

import io.github.airbag.gen.ExpressionLexer;
import io.github.airbag.gen.ExpressionParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreeFormatterTest {

    @Nested
    class AntlrTreeFormatterTest {

        @Test
        void testFormat() {
            Lexer lexer = new ExpressionLexer(CharStreams.fromString("x = 5\nx * 8\n"));
            ExpressionParser parser = new ExpressionParser(new CommonTokenStream(lexer));
            ParseTree tree = parser.prog();
            assertEquals(
                    "(prog (stat x = (expr 5) \\n) (stat (expr (expr x) * (expr 8)) \\n) <EOF>)",
                    tree.toStringTree(parser));
            DerivationTree dTree = DerivationTree.from(tree);
            TreeFormatter formatter = TreeFormatter.ANTLR.withRecognizer(parser);
            assertEquals(tree.toStringTree(parser), formatter.format(dTree));
        }
    }
}