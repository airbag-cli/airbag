# 3. Testing a Parser

Once you've confirmed your lexer is producing the correct token streams, the next step is to ensure your parser correctly builds the expected parse tree from those tokens. Airbag provides tools to define and assert complex tree structures with ease.

## Setup

To start testing your parser, you need an `Airbag` instance configured for parser testing. Use the `Airbag.testParser()` static factory method, providing the class of your ANTLR-generated parser.

```java
import io.github.airbag.Airbag;
import org.junit.jupiter.api.BeforeEach;
// Import your generated parser class
import io.github.airbag.gen.ExpressionParser; 

class MyParserTest {

    private Airbag airbag;

    @BeforeEach
    void setup() {
        // Replace ExpressionParser.class with your actual parser class
        airbag = Airbag.testParser(ExpressionParser.class);
    }
    // ... your tests
}
```

The `Airbag` instance provides access to a `TreeProvider` via `airbag.getTreeProvider()`. This `TreeProvider` is responsible for building `DerivationTree` objects both from string specifications and by running your actual ANTLR parser. It also gives you access to the `TokenFormatter` used for parsing symbol lists.

## Writing a Parser Test

Let's continue with our expression grammar example and test that the parser correctly builds a tree for the input `10 + 5`.

### 1. Define the Grammar (Example `Expression.g4`)

(The same grammar as in Lexer Testing section)

```g4
grammar Expression;

prog: stat+ EOF;

stat: expr NEWLINE
    | ID '=' expr NEWLINE
    | NEWLINE
    ;

expr: expr ('*'|'/') expr
    | expr ('+'|'-') expr
    | INT
    | ID
    | '(' expr ')'
    ;

ID: [a-zA-Z]+;
INT: '-'?[1-9][0-9]*;
NEWLINE: '\r'?'\n';
WS: [ \t]+ -> skip;
```

### 2. Build the Expected Tree from a Specification

Airbag allows you to define your expected parse tree using a readable, LISP-style string specification. The `TreeProvider` can then parse this string into a `DerivationTree` object.

```java
// Inside your test method:
import io.github.airbag.tree.DerivationTree;

// 1. Build the expected tree from a LISP-style specification
DerivationTree expectedTree = airbag.getTreeProvider().fromSpec("""
        (expr
            (expr (INT '10'))
            '+'
            (expr (INT '5'))
        )""");
```

In this specification:
*   `(...)` denotes a rule node. The first element inside is the rule name.
*   `'text'` denotes a terminal node (token), where `'text'` is the token's literal value.
*   `(INT '10')` is a terminal node where `INT` is the symbolic type and `'10'` is its text.

### 3. Create the List of Tokens for the Parser to Consume

Your parser expects a stream of tokens (symbols) as its input. You can create this list using the `TokenFormatter` from the `TreeProvider`:

```java
import io.github.airbag.token.Token;

import java.util.List;

// 2. Create the list of symbols for the parser to consume
List<Token> symbolList = airbag.getTreeProvider()
        .getFormatter()
        .getTokenFormatter()
        .parseList("(INT '10') '+' (INT '5')");
```

### 4. Parse the Token List to Get the Actual Tree

Now, feed the generated symbol list to your parser, specifying the entry rule (e.g., `"expr"`):

```java
// 3. Parse the symbol list to get the actual tree.
// The second argument is the getStartIndex rule.
DerivationTree actualTree = airbag.getTreeProvider().fromInput(symbolList, "expr");
```

### 5. Assert and Compare

Finally, use `airbag.assertTree()` to compare your `expectedTree` with the `actualTree`. If they don't match, Airbag provides a clear diff of the tree structures.

```java
// 4. Compare the expected and actual trees
airbag.assertTree(expectedTree, actualTree);
```

## Complete Parser Test Example

```java
import io.github.airbag.Airbag;
import io.github.airbag.gen.ExpressionParser;
import io.github.airbag.token.Token;
import io.github.airbag.tree.DerivationTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

class ExpressionParserTest {

    private Airbag airbag;

    @BeforeEach
    void setup() {
        // Replace ExpressionParser.class with your actual parser class
        airbag = Airbag.testParser(ExpressionParser.class);
    }

    @Test
    void testAdditionExpression() {
        // 1. Build the expected tree from a LISP-style specification
        DerivationTree expectedTree = airbag.getTreeProvider().fromSpec("""
                (expr
                    (expr (INT '10'))
                    '+'
                    (expr (INT '5'))
                )""");

        // 2. Create the list of symbols for the parser to consume
        List<Token> symbolList = airbag.getTreeProvider()
                .getFormatter()
                .getTokenFormatter()
                .parseList("(INT '10') '+' (INT '5')");

        // 3. Parse the symbol list to get the actual tree.
        // The second argument is the getStartIndex rule.
        DerivationTree actualTree = airbag.getTreeProvider().fromInput(symbolList, "expr");

        // 4. Compare the expected and actual trees
        airbag.assertTree(expectedTree, actualTree);
    }
}
```

This declarative approach to parser testing makes it easy to visualize and verify your grammar's output. For more advanced customization of tree formatting or for matching complex tree structures, explore the [Customizing Tree Formats](./reference/trees.md) and [Matching Tree Patterns](./reference/patterns.md) sections.