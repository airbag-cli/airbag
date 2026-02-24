I recently became interested in how to make developing ANTLR grammars easier and more testable. I decided to write some code that I believe could be helpful. I think the ideas used here could be interesting for others and might even be suitable for the ANTLR runtime if others see merit in this [ANTLR testing framework](https://github.com/airbag-cli/airbag).

My primary goal was to create a framework for unit testing lexers and parsers in isolation. While the runtime library offers support with `XPath` and `ParseTreePattern`, the latter's dependency on both lexer and parser is, in my opinion, a drawback. I also believe it would be beneficial to have tests that capture the full structure of the parse tree without relying solely on a specific string serialization format.

I envision tests looking like this:

```
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

Here, code generation is handled by the ANTLR Maven plugin, followed by a comparison of expected and actual lists of symbols/tokens. This approach allows you to build a custom list of expected tokens, either as a multiline string or from a file, and compare it to the actual tokens with the exact detail provided by the string representation.

Crucially, I aimed to avoid tying the library and user to a specific string representation. I explored ways to abstract over serializing and deserializing tokens/symbols with ease and propose a solution similar to `DateTimeFormatter`.

# SymbolFormatter

I build a formatter class with `DateTimeFormatter` as an inspiration that makes it possible to define bidirectional parser and formatter in a custum format. The testing libary is configured to use a predefined 'simple' format, but I a user would prefer to use the antlr string representation this would just require to change a formatter object like in this example:

```
public class ExpressionLexerTest {

    private Airbag airbag;
    private SymbolProvider provider;

    @BeforeEach
    void setup() {
        airbag = Airbag.testLexer("io.github.airbag.expression.ExpressionLexer");
        provider = airbag.getSymbolProvider();
    }

    @Test
    void testNEWLINE() {
        //It is possible to use a different format for parsing symbols/tokens to capture more details
        //if needed
        provider.setFormatter(SymbolFormatter.ANTLR);

        //Expected
        List<Symbol> expected = provider.fromSpec("""
                [@0,0:0='\\n',<NEWLINE>,1:0]
                [@1,2:3='\\r\\n',<NEWLINE>,2:1]
                [@2,4:3='<EOF>',<EOF>,3:0]
                """);

        //Actual
        List<Symbol> actual = provider.fromInput("\n \r\n");

        //Compare results
        airbag.assertSymbolList(expected, actual);
    }
}
```

It is relatively easy to define a custom formatter in almost any format (e.g., JSON, YAML, XML) simply by defining and setting a custom `Formatter`. The framework then recognizes this new format and uses it for message output and parsing. Additionally, only token fields actually captured in the format are used in comparison with the actual lexer output.

## ParseTrees

I applied the same principle to `ParseTree`, creating a `TreeFormatter` class. This allows for testing parser rules in a proposed manner, as shown here:

```
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

The same principle applies here. I set a simple default format similar to ANTLR's `toStringTree()` behavior, but designed it so that you don't need to rely on any lexer to build an expected and actual `ParseTree` for testing. In this example, a lexer grammar might not even exist, yet you could still test a parse grammar, provided tokens are declared with a tokens file.
Admittedly, defining a valid `TreeFormatter` that is bidirectional (for both parsing and formatting) is more complex due to the recursive/nested nature of parse trees, but it works in principle. I've also tested it on relatively larger `ParseTrees` with a few thousand rule invocations, and serialization/deserialization took less than a second, indicating the idea is sound.
My idea was to also incorporate `ParseTreePattern` or a similar construct which does look like this:

```
DerivationTree expectedTree = treeProvider.fromSpec("""
                (prog
                    (<stat>
                        (
                            <ID>
                            '='
                            <expr>
                            (NEWLINE '\\n')
                        )
                    )
                    (stat
                        (expr (expr (INT '5'))
                        '+'
                        (expr (ID 'x')))
                        (NEWLINE '\\n')
                    )
                    EOF
                )""");
``` 

I invested considerable time in writing and designing this library. While I believe it is relatively clean, some concepts are still in an early stage. An example setup is available in the repository for anyone who wishes to explore it.

Now my question is:

Does anyone see value in this approach and would be willing to provide feedback? Would it be a good idea to propose some of these features for the ANTLR runtime?
Currently, I'm using my own type structure for various reasons, but it would be relatively simple to use actual ANTLR types, and the classes also offer transformer methods.
Thanks for reading this far! 