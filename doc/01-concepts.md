# 1. Core Concepts: Lexing and Parsing

Before diving into writing tests, it's important to understand the two-stage process that ANTLR uses to understand text,
and why testing each stage independently is so powerful.

## The Two-Stage Process: Lexing and Parsing

When ANTLR processes an input string, it does so in two main steps: lexical analysis (lexing) and syntax analysis (
parsing).

### The Lexer: The Tokenizer

The first stage is **lexical analysis**, performed by a **lexer** (also known as a tokenizer). The lexer's job is to
scan the raw input string and break it down into a sequence of meaningful chunks called **tokens**. Each token has a
type (like `ID`, `INTEGER`, or `'+'`) and the text it represents.

For example, given the input string for a simple configuration:
`host = "localhost" port = 8080`

The lexer would produce a flat stream of tokens like this:

|   Text   | `"localhost"` | `port` |  `=`  | `8080` |
|:--------:|:-------------:|:------:|:-----:|:------:|
| **Type** |     `ID`      |  `EQ`  | `STR` |  `ID`  | 

This token stream is much easier to work with than the original raw string.

### The Parser: The Structure Builder

The second stage is **syntax analysis**, performed by a **parser**. The parser takes the flat list of tokens from the
lexer and builds a hierarchical structure called a **Parse Tree** (or Abstract Syntax Tree). This tree represents the
grammatical relationships between the tokens, as defined by your grammar rules.

Continuing the example, the parser would take the token stream and build a tree that understands that `"localhost"` is
the value for the `host` key:

```mermaid
graph TD
    A(config)
    A --> B(pair)
    A --> C(pair)
    B --> D(KEY 'host')
    B --> E(EQUALS '=')
    B --> F(STRING '"localhost"')
    C --> I(KEY 'port')
    C --> J(EQUALS '=')
    C --> K(INTEGER '8080')
```

## Why Test Them Separately?

A bug in your grammar can originate in either the lexer or the parser:

* **Lexer Bug**: The lexer might fail to tokenize the input correctly. For example, it might incorrectly split `8080`
  into two tokens, `80` and `80`.
* **Parser Bug**: The lexer might produce correct tokens, but the parser might fail to assemble them into the correct
  tree structure.

By testing the lexer and parser in **isolation**, you can pinpoint the exact source of a failure. If you give the lexer
an input string and it produces the wrong tokens, you know the problem is in your lexer rules. If the lexer produces the
*correct* tokens, but the parser builds the wrong tree from them, you know the problem is in your parser rules.

## The Airbag Workflow

Airbag is designed around this principle of isolation. The testing workflow for both stages follows a similar pattern:

1. **Define the Expected Output**: You write a simple, readable string that specifies what the correct output should be.
    * For a lexer, this is a sequence of symbols (tokens).
    * For a parser, this is a LISP-style representation of the parse tree.
2. **Generate the Actual Output**: You use an Airbag provider to run the lexer or parser on your input.
3. **Assert and Compare**: Airbag compares the actual output to your expected specification and gives you a clear diff
   if they don't match.

This approach makes your tests clear, declarative, and resilient to changes.