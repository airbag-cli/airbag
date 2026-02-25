# Airbag

Airbag is a lightweight testing framework for ANTLR grammars in Java. It provides a simple and intuitive API to test both the lexer (tokenization) and the parser (tree structure) of your grammar, making it an ideal companion for grammar development and validation.

**Airbag**: For the **I**ntegrity, **R**egression, and **B**ehavioral **A**nalysis of your **A**ntlr **G**rammars.

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

public class ExpressionLexerTest {

    private Airbag airbag;
    private TokenProvider provider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testLexer("io.github.airbag.expression.ExpressionLexer");
        provider = airbag.getTokenProvider();
    }

    @Test
    void testID() {
        //Create an expected list of symbols
        List<Token> expected = provider.expected("""
                (ID 'x')
                (ID 'myVariable')
                (ID 'y')
                EOF
                """);

        //Let the lexer tokenize an actual input string
        List<Token> actual = provider.actual("x myVariable y");

        //Compare the expected and actual list
        airbag.assertTokens(expected, actual);
    }
}
```

Assuming your IDE setup is correct you can also just refer to class literal to test like in this
example

```java

public class ExpressionParserTest {

    private Airbag airbag;
    private TreeProvider treeProvider;
    private TokenFormatter symbolFormatter;

    @BeforeEach
    void setup() {
        airbag = Airbag.testParser(ExpressionParser.class);
        treeProvider = airbag.getTreeProvider();
        // Take the symbol formatter from the tree formatter as the Lexer
        // is not instantiated.
        symbolFormatter = treeProvider.getFormatter().getTokenFormatter();
    }

    @Test
    void testProg() {
        // Build expected tree from specification
        Tree expectedTree = treeProvider.expected("""
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
        List<Token> symbolList = symbolFormatter.parseList("""
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
        Tree actualTree = treeProvider.actual(symbolList, "prog");

        //Compare the expected and actual tree
        airbag.assertTree(expectedTree, actualTree);
    }
}
```

Confirm that the setup is working with `mvn clean test`.

As we see we can test the lexer and parser in total isolation. Whenever we change the grammar we just have to execute maven
workflow to generate new classes and run the tests directly.
