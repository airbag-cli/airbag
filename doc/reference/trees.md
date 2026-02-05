# Derivation Trees and Formatters

This document covers the `DerivationTree`, the core data structure representing a parsed input, and the `TreeFormatter`, which controls how trees are converted to and from text.

## Derivation Trees

The `DerivationTree` is the core data structure representing the parsed input. It's a tree of nodes that can be either rules or terminals.

### Structure

A `DerivationTree` is composed of different types of nodes:

*   **Rule Nodes**: Represent a non-terminal symbol in the grammar (e.g., `expr`, `statement`). They have a rule index and a list of child nodes.
*   **Terminal Nodes**: Represent a terminal symbol, or token, from the input (e.g., an identifier, a keyword, an operator). They contain a `Symbol` object.
*   **Error Nodes**: Represent a point in the parse where an error occurred. They also contain a `Symbol` representing the erroneous token.

## TreeFormatters

`TreeFormatter` and `TreeFormatterBuilder` are used to define a custom string representation for an entire `DerivationTree`. This is essential for both creating expected tree specifications in tests and for printing actual trees for debugging.

### Building a Formatter

You can define different formatting rules for different types of nodes (rules, terminals, and errors) using the `TreeFormatterBuilder`.

For example, to create a simple LISP-style format:

```java
TreeFormatter lispFormatter = new TreeFormatterBuilder()
    .onRule(ruleNode -> ruleNode
        .appendLiteral("(")
        .appendRule() // Appends the rule name
        .appendLiteral(" ")
        .appendChildren(" ") // Recursively appends children, separated by a space
        .appendLiteral(")")
    )
    .onTerminal(terminalNode -> terminalNode
        .appendSymbol() // Appends the symbol using the configured SymbolFormatter
    )
    .toFormatter();
```

This allows you to serialize a tree into a format like `(expr (INT '10') + (INT '5'))` and parse it back into a `DerivationTree` object for your tests.
