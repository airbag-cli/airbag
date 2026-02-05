# Airbag Documentation

**Airbag**: For the **I**ntegrity, **R**egression, and **B**ehavioral **A**nalysis of your **A**ntlr **G**rammars.

## Why Airbag?

Writing a grammar with ANTLR is a powerful way to process text, but testing it can be challenging. A small change to a
grammar rule can have unexpected ripple effects, and it's often difficult to pinpoint whether a bug originates in the
lexer (which groups characters into tokens) or the parser (which structures tokens into a tree).

Airbag is a testing library built to solve this problem. It provides a robust framework to validate the behavior of your
lexer and parser in **complete isolation**, ensuring that each stage of your grammar processing pipeline is reliable and
correct.

With Airbag, you can:

* **Test your lexer** by asserting that a given input string produces an exact sequence of tokens.
* **Test your parser** by asserting that a given sequence of tokens builds a precise parse tree.
* **Write clear, readable tests** using simple string specifications for both tokens and trees.
* **Prevent regressions** and gain confidence in your grammar as it evolves.

## Table of Contents

* [1. Core Concepts: Lexing and Parsing](./01-concepts.md)
* [2. Testing a Lexer](./02-testing-lexers.md)
* [3. Testing a Parser](./03-testing-parsers.md)

## Installation

The easiest way to set up Airbag is in conjunction with the antlr build plugin. The following example works for
the maven build plugin. There is also an example repository that has the full setup.

### Maven Setup

Let as assume we are in the process of constructing the following grammar

```antlrv4
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

Now we want to write some test that are fully integrated in the maven workflow. For this we set up a maven project
with JUnit 5 for testing. Set up the following dependencies

```xml

<dependencies>
    <dependency>
        <groupId>io.github.airbag-cli</groupId>
        <artifactId>airbag</artifactId>
        <version>0.1.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

In the build section we want to incorporate the `antlr4-maven-plugin`

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-maven-plugin</artifactId>
            <version>4.13.1</version>
            <configuration>
                <arguments>
                    <argument>-package</argument>
                    <argument>io.github.airbag.expression</argument>
                </arguments>
                <visitor>true</visitor>
            </configuration>
            <executions>
                <execution>
                    <id>antlr</id>
                    <goals>
                        <goal>antlr4</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

We now place the `Expression.g4` grammar file inside a specific location in `src/main/antlr4/<your.package.structure>`.
The antlr4 directory should be marked as
source root folder inside you IDE, for the IDE to correctly handle and show the generated classes. Now we create
packages (directories)
inside the antlr4 directory that mirror our package name. So in our example we place the `Expression.g4` file in
`src/main/antlr4/io/github/airbag/expression/Expression.g4`

### Write a Test

Now we can just write a simple JUnit test like this.

```java
package io.github.airbag.expression;

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
        airbag = Airbag.testLexer("io.github.airbag.expression.ExpressionLexer");
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
}
```

Assuming your IDE setup is correct you can also just refer to class literal to test like in this 
example

```java

package io.github.airbag.expression;

import io.github.airbag.Airbag;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExpressionParserTest {

    private Airbag airbag;
    private TreeProvider treeProvider;
    private SymbolFormatter symbolFormatter;

    @BeforeEach
    void setup() {
        airbag = Airbag.testParser(ExpressionParser.class);
        treeProvider = airbag.getTreeProvider();
        // Take the symbol formatter from the tree formatter as the Lexer
        // is not instantiated.
        symbolFormatter = treeProvider.getFormatter().getSymbolFormatter();
    }

    @Test
    void testProg() {
        // Build expected tree from specification
        DerivationTree expectedTree = treeProvider.fromSpec("""
                (prog
                    (stat
                        (ID 'x')
                        '='
                        (expr (INT '10'))
                        (NEWLINE '\\n')
                    )
                    (stat
                        (expr (expr (INT '5'))
                        '+'
                        (expr (ID 'x')))
                        (NEWLINE '\\n')
                    )
                    EOF
                )""");

        // Create a derivation tree from symbol list.
        List<Symbol> symbolList = symbolFormatter.parseList("""
                (ID 'x')
                '='
                (INT '10')
                (NEWLINE '\\n')
                (INT '5')
                '+'
                (ID 'x')
                (NEWLINE '\\n')
                EOF""");

        // Pass the symbol list to the parser
        DerivationTree actualTree = treeProvider.fromInput(symbolList, "prog");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }
}
```

Confirm that the setup is working with `mvn clean test`.

As we see we can test the lexer and parser in total isolation. Whenever we change the grammar we just have to execute maven 
workflow to generate new classes and run the tests directly.

### API Reference

* [Symbols and SymbolFormatters](./reference/symbols.md)
* [Derivation Trees and TreeFormatters](./reference/trees.md)
* [Patterns](./reference/patterns.md)
* [Queries](./reference/queries.md)