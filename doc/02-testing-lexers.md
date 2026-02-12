# 2. Testing a Lexer

Testing your lexer ensures that your raw input strings are correctly broken down into the expected sequence of tokens. Airbag simplifies this process by allowing you to define your expected token stream using a human-readable string specification.

## Setup

To start testing your lexer, you first need to create an `Airbag` instance configured for lexer testing. You can do this using the `Airbag.testLexer()` static factory method, providing the fully qualified name of your ANTLR-generated lexer class.

```java
import io.github.airbag.Airbag;
import org.junit.jupiter.api.BeforeEach;

class MyLexerTest {
    
    private Airbag airbag;
    
    @BeforeEach
    void setup() {
        // Replace "io.github.airbag.gen.MyLexer" with your actual lexer class
        airbag = Airbag.testLexer("io.github.airbag.gen.ExpressionLexer");
    }
    // ... your tests
}
```

The `Airbag` instance provides access to a `SymbolProvider` via `airbag.getSymbolProvider()`. The `SymbolProvider` is responsible for converting raw input strings into `Symbol` lists and for parsing your expected symbol specifications.

## Writing a Lexer Test

Let's test a simple lexer for an expression grammar, ensuring it correctly tokenizes the input `a = 10 + b`.

### 1. Define the Grammar (Example `Expression.g4`)

First, let's assume you have a simple ANTLR grammar like this:

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

### 2. Create Expected Symbols from a Specification

You define the expected list of `Symbol` objects using a string specification, which `SymbolProvider` can parse. Airbag's default formatter (`SymbolFormatter.SIMPLE`) provides a clear, LISP-like syntax:

*   `(ID 'text')`: For identifiers or keywords, where `ID` is the symbolic name and `'text'` is the matched string.
*   `'+'`, `'-'`, etc.: For single-character literal tokens.
*   `EOF`: For the end-of-file token.

```java
// Inside your test method:
import io.github.airbag.symbol.Symbol;
import java.util.List;

// 1. Create an expected list of symbols from a specification
List<Symbol> expected = airbag.getSymbolProvider().fromSpec("""
        (ID 'a')
        '='
        (INT '10')
        '+'
        (ID 'b')
        EOF
        """);
```
Notice how easy it is to read and understand the expected token stream.

### 3. Generate Actual Symbols from Input

Next, you pass the actual input string to your lexer to get the real token stream:

```java
// 2. Let the lexer tokenize the actual input string
List<Symbol> actual = airbag.getSymbolProvider().fromInput("a = 10 + b");
```

### 4. Assert and Compare

Finally, use `airbag.assertSymbolList()` to compare the expected and actual lists of symbols. If they don't match, Airbag will provide a detailed, human-readable diff.

```java
// 3. Compare the expected and actual lists of symbols
airbag.assertSymbolList(expected, actual);
```

## Complete Lexer Test Example

```java
import io.github.airbag.Airbag;
import io.github.airbag.symbol.Symbol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

class ExpressionLexerTest {
    
    private Airbag airbag;
    
    @BeforeEach
    void setup() {
        // Replace "io.github.airbag.gen.ExpressionLexer" with your actual lexer class
        airbag = Airbag.testLexer("io.github.airbag.gen.ExpressionLexer");
    }
    
    @Test
    void testAssignment() {
        // 1. Create an expected list of symbols from a specification
        List<Symbol> expected = airbag.getSymbolProvider().fromSpec("""
                (ID 'a')
                '='
                (INT '10')
                '+'
                (ID 'b')
                EOF
                """);

        // 2. Let the lexer tokenize the actual input string
        List<Symbol> actual = airbag.getSymbolProvider().fromInput("a = 10 + b");

        // 3. Compare the expected and actual lists of symbols
        airbag.assertSymbolList(expected, actual);
    }
}
```

This clear, declarative style makes your lexer tests easy to write, understand, and maintain. For more advanced control over symbol formatting and parsing, refer to the [Customizing Symbol Formats](./reference/symbols.md) section.
