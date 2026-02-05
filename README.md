# Airbag

Airbag is a lightweight testing framework for ANTLR grammars in Java. It provides a simple and intuitive API to test both the lexer (tokenization) and the parser (tree structure) of your grammar, making it an ideal companion for grammar development and validation.

**Airbag**: For the **I**ntegrity, **R**egression, and **B**ehavioral **A**nalysis of your **A**ntlr **G**rammars.

## Installation

To use Airbag in your Maven project, add the following dependency to your `pom.xml`.

```xml
<dependency>
    <groupId>io.github.airbag-cli</groupId>
    <artifactId>airbag</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Testing the Lexer

You can easily verify that your lexer correctly tokenizes an input string by comparing it against a well-defined token specification.

```java
import io.github.airbag.Airbag;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

public class LexerTest {

    @Test
    void testTokenization() {
        // 1. Initialize Airbag for your grammar
        Airbag airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        SymbolProvider tokenProvider = airbag.getSymbolProvider();

        // 2. Define the expected token stream using a clear specification
        List<Symbol> expected = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");

        // 3. Tokenize the actual input string
        List<Symbol> actual = tokenProvider.fromInput("x = 5");

        // 4. Assert that the actual tokens match the expected specification
        airbag.assertSymbolList(expected, actual);
    }
}
```

### Testing the Parser

Airbag allows you to validate the structure of the generated parse tree against a simple, Lisp-style tree specification. This ensures your parser rules are producing the correct hierarchy.

```java
import io.github.airbag.Airbag;
import io.github.airbag.symbol.SymbolProvider;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeProvider;
import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    void testParseTreeStructure() {
        // 1. Initialize Airbag for your grammar
        Airbag airbag = Airbag.testGrammar("io.github.airbag.gen.Expression");
        SymbolProvider symbolProvider = airbag.getSymbolProvider();
        TreeProvider treeProvider = airbag.getTreeProvider();

        // 2. Define the expected tree structure using a validation tree
        DerivationTree expected = treeProvider.fromSpec("""
                (prog
                    (stat
                        (ID 'x')
                        '='
                        (expr (INT '5'))
                        (NEWLINE '\\n')
                    )
                    EOF
                )""");

        // 3. Generate the actual parse tree from an input string
        DerivationTree actual = treeProvider.fromSource(symbolProvider.fromInput("x = 5\n"), "prog");

        // 4. Assert that the generated tree has the expected structure
        airbag.assertTree(expected, actual);
    }
}
```