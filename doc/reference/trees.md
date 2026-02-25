# Customizing Derivation Tree Formats

This guide explains how to work with `DerivationTree` objects and how to customize their string representations using `TreeFormatter`, `TreeFormatterBuilder`, and `NodeFormatterBuilder`.

## 1. Introduction: Derivation Trees and TreeFormatters

*   **`DerivationTree`**: The core data structure in Airbag, representing the hierarchical structure of a parsed input. It's an immutable, sealed interface that can be one of four main node types:
    *   **Rule Node (`DerivationTree.Rule`)**: Represents a non-terminal grammar rule (e.g., `expr`, `statement`).
    *   **Terminal Node (`DerivationTree.Terminal`)**: Represents a terminal symbol or token from the input (e.g., an identifier, `+`, `INT`). It wraps a `Symbol` object.
    *   **Error Node (`DerivationTree.Error`)**: Represents a point in the parse where an error occurred. It also wraps a `Symbol`.
    *   **Pattern Node (`DerivationTree.Pattern`)**: Used internally when defining tree patterns.

    `DerivationTree` instances are typically created from an ANTLR `ParseTree` using `DerivationTree.from(ParseTree)` or by parsing a string specification via a `TreeFormatter`.

*   **`TreeFormatter`**: A powerful, immutable, and thread-safe tool for converting a `DerivationTree` to a `String` (formatting) and a `String` back to a `DerivationTree` (parsing). It's essential for defining expected tree structures in tests (`TreeProvider.fromSpec()`) and for debugging.

## 2. Using Predefined TreeFormatters

Airbag offers two convenient, predefined `TreeFormatter` instances for common use cases:

### `TreeFormatter.SIMPLE`

This is the default formatter used in many Airbag testing APIs. It produces a clear, LISP-style S-expression format that is human-readable.

**Example Output:**
```
(expr
    (atom (INT '42'))
    +
    (atom (INT '1'))
)
```
Error nodes are explicitly marked:
```
(prog
    (stat
        (atom (ID 'x'))
        =
        (expr (INT '10'))
        (NEWLINE '\n')
    )
    (stat
        (<error> (ID 'invalidToken'))
        (NEWLINE '\n')
    )
    EOF
)
```

### `TreeFormatter.ANTLR`

This formatter closely mimics the `toStringTree()` output of ANTLR's `Trees` utility class. It's often used for debugging, as it matches a familiar ANTLR output style.

**Example Output:**
```
(expr (atom 42) + (atom myText))
```

## 3. Creating Custom TreeFormatters with `TreeFormatterBuilder`

When the predefined formats don't meet your needs (e.g., for custom logging, specific test specification formats, or integrating with other tools), you can create your own `TreeFormatter` using `TreeFormatterBuilder`.

The `TreeFormatterBuilder` allows you to define a distinct string representation for each type of `DerivationTree` node.

**Core Concept:** You use `onRule()`, `onTerminal()`, `onError()`, and `onPattern()` methods to specify how each node type should be formatted. Each of these methods takes a lambda that configures a `NodeFormatterBuilder` for that specific node type.

**Example: A basic LISP-style formatter (similar to `SIMPLE` but showing explicit setup)**

```java
import io.github.airbag.tree.TreeFormatter;
import io.github.airbag.tree.TreeFormatterBuilder;
import io.github.airbag.tree.NodeFormatterBuilder;

TreeFormatter customLispFormatter = new TreeFormatterBuilder()
    .onRule(ruleNodeBuilder -> ruleNodeBuilder
        .appendLiteral("(")
        .appendRule() // Append the rule name (e.g., "expr")
        .appendWhitespace(" ")
        .appendChildren(" ") // Recursively format children, separated by a space
        .appendLiteral(")")
    )
    .onTerminal(NodeFormatterBuilder::appendSymbol) // Append the symbol (e.g., (INT '42'))
    .onError(errorNodeBuilder -> errorNodeBuilder
        .appendLiteral("(<ERROR> ") // Custom error marker
        .appendSymbol()
        .appendLiteral(")")
    )
    .toFormatter();
```

## 4. Defining Node-Specific Formats with `NodeFormatterBuilder`

Inside the `onRule()`, `onTerminal()`, etc., lambdas, you use a `NodeFormatterBuilder` to construct the exact string representation for that particular node type. This builder assembles a sequence of "printer-parsers" that define how parts of the node are formatted and parsed.

Here are the key methods of `NodeFormatterBuilder`:

*   **`appendLiteral(String literal)`**: Appends a fixed string.
    ```java
    // e.g., appends "("
    nodeBuilder.appendLiteral("(");
    ```

*   **`appendRule()`**: Appends the name or index of a rule node. It prioritizes the rule name if a `Recognizer` is configured with the `TreeFormatter`.
    ```java
    // e.g., for a rule node 'expr', appends "expr"
    nodeBuilder.appendRule();
    ```

*   **`appendSymbol()`**: Appends the `Symbol` associated with a `Terminal` or `Error` node. The actual format of the symbol is determined by the `SymbolFormatter` configured with the `TreeFormatter`.
    ```java
    // e.g., for a terminal node 'INT 42', appends "(INT '42')"
    nodeBuilder.appendSymbol();
    ```

*   **`appendChildren(String separator)`**: **Crucial for traversing the tree.** This method tells the `TreeFormatter` where and how to insert the formatted string representation of the *children* of the current node.
    *   `separator`: A string inserted between each child's formatted output.
    ```java
    // Formats children separated by a space: child1 child2 child3
    nodeBuilder.appendChildren(" ");
    ```

*   **`appendChildren(Consumer<NodeFormatterBuilder> childSeparator)`**: An advanced version where the separator between children can be defined using its own `NodeFormatterBuilder`. This allows for complex separators like newlines and indentation.
    ```java
    // Formats each child on a new getLine, indented
    nodeBuilder.appendChildren(separatorBuilder -> separatorBuilder
        .appendLiteral("\n")
        .appendIndent("  ") // Indents with two spaces per level
    );
    ```

*   **`appendWhitespace()`, `appendWhitespace(String whitespace)`**:
    *   `appendWhitespace()`: Formats no space, but greedily parses any amount of whitespace.
    *   `appendWhitespace(" ")`: Formats a single space, but greedily parses any amount of whitespace. Useful for flexible parsing but consistent formatting.

*   **`appendIndent(String indent)`**: Appends indentation based on the node's depth in the tree. During formatting, it repeats the `indent` string by the node's depth. During parsing, it acts like `appendWhitespace()`.
    ```java
    // Formats indentation: (root), (  child), (    grandchild)
    nodeBuilder.appendIndent("  ");
    ```

*   **`appendPattern()`**: Used for `DerivationTree.Pattern` nodes; formats the internal pattern.

## 5. Advanced `TreeFormatter` Customization

Once a `TreeFormatter` is built using `toFormatter()`, you can further customize its behavior:

* **Configuring `SymbolFormatter` (`withSymbolFormatter(SymbolFormatter)`)**:
    You can specify how the `Symbol` objects within `Terminal` and `Error` nodes are formatted. This is particularly useful if you want a different `Symbol` representation than the default `SymbolFormatter.SIMPLE`.
    (Refer to the [Customizing Symbol Formats](./symbols.md) documentation for details on `SymbolFormatter`).

      ```java

    import io.github.airbag.token.TokenFormatterBuilder;

    // Create a custom SymbolFormatter that prints 'TEXT:TYPE'
    SymbolFormatter customSymFormatter = new SymbolFormatterBuilder()
        .appendText().appendLiteral(":").appendType(TypeFormat.SYMBOLIC_ONLY)
        .toFormatter();

    TreeFormatter customizedFormatter = yourTreeFormatter.withSymbolFormatter(customSymFormatter);
    ```TLR `Recognizer` Context (`withRecognizer(Recognizer)`)**:
    For `TreeFormatter` to display rule names (e.g., "expr") instead of just integer rule indices (e.g., "0"), you must provide an ANTLR `Recognizer` (typically your generated `Parser` instance). This gives the formatter access to the grammar's vocabulary and rule names.

    ```java
    import org.antlr.v4.runtime.Parser;
    // Assuming 'myParser' is an instance of your ANTLR-generated parser
    Parser myParser = new MyParser(null); // Or similar instantiation

    TreeFormatter contextualFormatter = yourTreeFormatter.withRecognizer(myParser);
    // Now, appendRule() will output rule names if available
    ```

## 6. Formatting and Parsing Trees

Once you have your `TreeFormatter` configured, you can use its `format()` and `parse()` methods:

*   **`format(DerivationTree tree)`**: Converts a `DerivationTree` object into its string representation.

    ```java
    String formattedTree = contextualFormatter.format(myDerivationTree);
    ```

*   **`parse(CharSequence text)`**: Parses a string representation back into a `DerivationTree`. This method expects the entire input string to be consumed. If parsing fails or trailing text remains, it throws a `TreeParseException`.

    ```java
    DerivationTree parsedTree = contextualFormatter.parse("(expr (INT '10') + (ID 'x'))");
    ```

## 7. `TreeProvider` Overview

The `TreeProvider` (`io.github.airbag.tree.TreeProvider`) is a high-level utility that simplifies the creation of `DerivationTree` objects, especially in a testing context.

*   **`fromInput(List<Symbol> symbolList, String rule)`**: Simulates an ANTLR parse by taking a list of `Symbol` objects and a starting rule name, then building a `DerivationTree`.
*   **`fromSpec(String stringTree)`**: Parses a string specification (formatted according to the `TreeFormatter` configured with the `TreeProvider`) into a `DerivationTree`. This is the primary way to define expected tree structures in Airbag tests.

You typically get a `TreeProvider` instance from your Airbag instance. You can then customize its internal `TreeFormatter` using `setFormatter(TreeFormatter)` if needed.