# Symbols and SymbolFormatters

This section describes `Symbol`s, which represent the terminal nodes of a tree, and `SymbolFormatter`s, which control how they are converted to and from text.

## 1. The `Symbol` Concept

A `Symbol` represents a token from the input, such as a keyword, identifier, or literal. In Airbag, a `Symbol` (defined in `io.github.airbag.symbol.Symbol`) is an **immutable, lightweight representation of a lexical symbol**, designed to be a fundamental building block for tree structures. It models ANTLR's `Token` but decouples from the underlying `CharStream` or `TokenSource`, making it long-lived and suitable for various contexts, including reconstruction from specifications.

### 1.1. Symbol Fields (`SymbolField`)

Each `Symbol` encapsulates several key attributes, accessible and modifiable in a type-safe manner through `SymbolField` (`io.github.airbag.symbol.SymbolField`). These fields mirror standard ANTLR token properties:

*   **`TYPE`**: The integer type of the symbol (e.g., `MyLexer.ID`).
*   **`TEXT`**: The actual text matched from the input (e.g., "myVar").
*   **`INDEX`**: The zero-based index of the symbol in the token stream.
*   **`LINE`**: The line number where the symbol starts (1-based).
*   **`POSITION`**: The character position within its line (0-based).
*   **`CHANNEL`**: The channel on which the symbol was emitted (e.g., default channel 0, hidden channel for comments).
*   **`START`**: The starting character index in the input stream.
*   **`STOP`**: The stopping character index in the input stream (inclusive).

`SymbolField` also provides utility methods:
*   `access(Symbol)`: Extracts the value of a specific field from a `Symbol`.
*   `resolve(Symbol.Builder, T)`: Sets the value of a field in a `Symbol.Builder`.
*   `getDefault()`: Returns the default value for a given field (e.g., `""` for text, `-1` for indices).
*   `equalizer(Set<SymbolField<?>>)`: Creates a `BiPredicate` to compare two `Symbol`s based only on a specified subset of fields. This is particularly useful for assertion and diffing, as seen in `SymbolListDiffPrinter`.
*   `all()`: Returns a set of all `SymbolField` instances.
*   `simple()`: Returns a predefined set of common fields (`TYPE`, `TEXT`, `INDEX`, `CHANNEL`) for basic identification.

### 1.2. Creating Symbols

`Symbol` instances can be created in several ways:

*   **Using the `Builder` (`Symbol.of()`):**
    The primary way to programmatically construct a `Symbol` is using its fluent builder API.
    ```java
    import io.github.airbag.symbol.Symbol;
    import io.github.airbag.symbol.SymbolField;

    Symbol symbol = Symbol.of()
            .type(MyLexer.ID)
            .text("myVariable")
            .index(0)
            .line(1)
            .position(0)
            .channel(Symbol.DEFAULT_CHANNEL)
            .start(0)
            .stop(9)
            .get();
    ```
    Fields not explicitly set will default to their predefined values (e.g., `index` defaults to -1).

*   **From an ANTLR `Token` (`new Symbol(Token)`):**
    Existing ANTLR `Token` objects can be converted into `Symbol`s.
    ```java
    import org.antlr.v4.runtime.CommonToken;
    import org.antlr.v4.runtime.Token;
    import io.github.airbag.symbol.Symbol;

    Token antlrToken = new CommonToken(MyLexer.ID, "anotherVar");
    // Set other token properties if needed
    Symbol symbol = new Symbol(antlrToken);
    ```

*   **From a String using a `SymbolFormatter` (`Symbol.of(String, SymbolFormatter)`):**
    `Symbol` instances can also be parsed directly from their string representation using a `SymbolFormatter`. The default `Symbol.of(String)` uses `SymbolFormatter.ANTLR`.
    ```java
    import io.github.airbag.symbol.Symbol;
    import io.github.airbag.symbol.SymbolFormatter;

    Symbol parsedSymbol = Symbol.of("[@0,0:3='text',<ID>,1:0]", SymbolFormatter.ANTLR.withVocabulary(myLexerVocabulary));
    ```

### 1.3. Converting to an ANTLR `Token` (`toToken()`)

A `Symbol` can be converted back into an ANTLR `Token` (specifically `CommonToken`) using `toToken()`. This is useful for interoperability with parts of the ANTLR runtime that expect `Token` objects.

## 2. Core Concepts of `SymbolFormatter`

The `SymbolFormatter` (`io.github.airbag.symbol.SymbolFormatter`) is a powerful tool designed for **bidirectional transformation** of `Symbol` objects to and from their string representations. Think of it as analogous to `java.time.format.DateTimeFormatter` for `Symbol`s: it not only defines how a `Symbol` is printed as a string but also how a string can be parsed back into a `Symbol`.

### 2.1. Bidirectional Nature: Formatting and Parsing

*   **Formatting (Printing):** Converts a `Symbol` object into a human-readable or machine-parsable string according to a defined format.
*   **Parsing:** Takes a string and attempts to extract the components to reconstruct a `Symbol` object, adhering to the same format.

This dual capability is central to its utility, enabling:
*   Creating test specifications for lexers (e.g., `SymbolProvider.fromSpec()`).
*   Generating consistent output for debugging or logging.
*   Deserializing `Symbol`s from persisted states or custom input.

### 2.2. Contextual Information: `Vocabulary`

To correctly handle symbolic and literal names (e.g., converting the integer type `8` to `"ID"` or `'+'`), a `SymbolFormatter` often requires an ANTLR `Vocabulary`. This `Vocabulary` maps token types to their string representations. You can associate a `Vocabulary` using `withVocabulary(Vocabulary)`.

### 2.3. Immutability and Thread-Safety

`SymbolFormatter` instances are **immutable and thread-safe**. Any method that appears to modify a formatter (like `withVocabulary()` or `withAlternative()`) actually returns a *new* `SymbolFormatter` instance with the specified changes, leaving the original unchanged. This design ensures safe usage in concurrent environments.

### 2.4. Predefined Formatters

Airbag provides two convenient predefined `SymbolFormatter` instances:

*   **`SymbolFormatter.ANTLR`**:
    This formatter aims to precisely mimic the default `toString()` output of ANTLR's `CommonToken`. It produces a highly detailed representation, useful for debugging and logging the full context of a symbol.

    **Format:** `[@{index},{start}:{stop}='{text}',<{type}>(,channel={channel}),{line}:{pos}]`

    **Example Output (with vocabulary):**
    ```
    [@0,0:3='ID',<ID>,1:0]
    [@1,4:4='=',<=>,1:4]
    ```

*   **`SymbolFormatter.SIMPLE`**:
    A more human-readable and versatile formatter that intelligently chooses the best representation based on the `Symbol`'s properties and the available `Vocabulary`. It checks for symbol type in the following order:
    1.  **EOF Symbol**: Formatted as `"EOF"`.
    2.  **Literal Name**: If the symbol type has a literal name (e.g., `'+'`), it formats as `'+'`.
    3.  **Symbolic Name and Text**: If no literal name, it formats as `(ID 'text')`.
    If a symbol is on a non-default channel, the channel number is appended (e.g., `'+':1` or `(ID:1 'text')`).

    **Example Output (with vocabulary):**
    ```
    EOF
    '+'
    (ID 'myVar')
    (COMMENT:1 '//comment')
    ```

## 3. Parsing and Formatting in Detail

The core functionality of `SymbolFormatter` revolves around its `format()` and `parse()` methods.

### 3.1. Formatting a `Symbol`

The `format(Symbol symbol)` method converts a `Symbol` into its string representation. It uses an internal chain of "printer-parsers" to build the output string. If the formatter has alternatives (see `withAlternative()`), it tries each alternative until one successfully formats the symbol.

```java
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Vocabulary;
// Assume myLexerVocabulary is a configured Vocabulary instance
Vocabulary myLexerVocabulary = ...;

Symbol idSymbol = Symbol.of().type(MyLexer.ID).text("myIdentifier").get();
String formattedId = SymbolFormatter.SIMPLE.withVocabulary(myLexerVocabulary).format(idSymbol);
// formattedId might be "(ID 'myIdentifier')"
```

### 3.2. Parsing a String to a `Symbol`

`SymbolFormatter` provides two main parsing methods:

*   **`parse(CharSequence input)` (Strict Parsing):**
    This method attempts to parse the *entire* input `CharSequence` into a single `Symbol`. If the parsing fails or if there is any unconsumed trailing text, a `SymbolParseException` is thrown. This is suitable when you expect the input to be a complete and isolated symbol representation.

*   **`parse(CharSequence input, FormatterParsePosition position)` (Lenient Parsing):**
    This method attempts to parse a `Symbol` from the `input` starting at `position.getIndex()`. It does **not** require the entire input to be consumed.
    *   **On Success:** Returns the parsed `Symbol`, and `position.setIndex()` is updated to point to the character immediately after the parsed text.
    *   **On Failure:** Returns `null`, `position.getIndex()` remains unchanged, but `position.setErrorIndex()` is set to the position of the failure, and `position.getMessage()` provides error details.

This lenient approach is crucial for iteratively parsing multiple symbols from a larger string or when dealing with potentially malformed input without immediate exceptions.

#### `FormatterParsePosition` (`io.github.airbag.symbol.FormatterParsePosition`)
This class extends `java.text.ParsePosition` and enriches it with:
*   **`messages`**: A collection to accumulate multiple error or warning messages during complex parsing operations.
*   **`symbolIndex`**: An optional index to track the position of the current symbol within a list of symbols being parsed, useful for context in batch parsing.

#### Exceptions (`SymbolFormatterException`, `SymbolParseException`)
*   `SymbolFormatterException`: A generic runtime exception for any formatting or parsing errors.
*   `SymbolParseException`: A specialized subclass of `SymbolFormatterException`, specifically for parsing errors. It includes the original `input` string, the `position` of the error, and a detailed message with the error location marked (e.g., `>>` in the input).

#### Internal Context Objects (`SymbolFormatContext`, `SymbolParseContext`)
*   `SymbolFormatContext` (`io.github.airbag.symbol.SymbolFormatContext`): An internal record holding the `Symbol` being formatted and the `Vocabulary`.
*   `SymbolParseContext` (`io.github.airbag.symbol.SymbolParseContext`): An internal class used during parsing to accumulate parsed field values into a map before constructing the final `Symbol.Builder`. It also manages error messages.

## 4. Building Custom Formatters with `SymbolFormatterBuilder`

For scenarios requiring custom or complex `Symbol` representations, the `SymbolFormatterBuilder` (`io.github.airbag.symbol.SymbolFormatterBuilder`) offers a fluent API to construct `SymbolFormatter` instances piece by piece. This allows for precise control over the order and format of each `Symbol` attribute, as well as literal text.

### 4.1. Basic Usage: Appending Components

The builder works by appending various "printer-parsers", each responsible for a specific part of the `Symbol`'s format.

```java
import io.github.airbag.symbol.SymbolFormatterBuilder;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.TextOption;
import io.github.airbag.symbol.TypeFormat;

// Example: (TYPE:CHANNEL 'TEXT' [INDEX])
SymbolFormatter customFormatter = new SymbolFormatterBuilder()
    .appendLiteral("(")
    .appendType(TypeFormat.SYMBOLIC_FIRST) // Symbolic name first, then literal, then integer
    .appendLiteral(":")
    .appendInteger(SymbolField.CHANNEL) // Always print channel
    .appendLiteral(" '")
    .appendText(TextOption.ESCAPED) // Text with escaping
    .appendLiteral("'")
    .appendWhitespace(" ") // Optional space during formatting, consumes any during parsing
    .startOptional() // Start optional section
        .appendLiteral("[")
        .appendInteger(SymbolField.INDEX) // Index, but only if not default -1
        .appendLiteral("]")
    .endOptional() // End optional section
    .appendLiteral(")")
    .toFormatter();
```

Here's a breakdown of commonly used `append` methods:

*   **`appendInteger(SymbolField<Integer> field, boolean isStrict)`**: Appends an integer field. If `isStrict` is `true`, it will only format if the field's value is not its default.
    *   `appendInteger(SymbolField.INDEX)`: Appends the symbol's index.
    *   `appendInteger(SymbolField.CHANNEL, true)`: Appends the channel, but only if it's not the default `0`.

*   **`appendLiteral(String literal)`**: Appends a fixed literal string (e.g., `"("`, `", "`).

*   **`appendText()` / `appendText(TextOption option)`**: Appends the `Symbol`'s `text` field.
    *   `appendText()`: Uses `TextOption.NOTHING` (no escaping).
    *   `appendText(TextOption.ESCAPED)`: Uses `TextOption.ESCAPED` (common backslash escaping).
    *   **`TextOption` (`io.github.airbag.symbol.TextOption`)**: Configures text handling, including `escapeChar`, `escapeMap`, `unescapeMap`, `defaultValue`, and `failOnDefault` (whether formatting fails if text is default).

*   **`appendType(TypeFormat format)`**: Appends the `Symbol`'s `type` field using a specified format.
    *   `TypeFormat` (`io.github.airbag.symbol.TypeFormat`):
        *   `INTEGER_ONLY`: Formats/parses only the raw integer type.
        *   `SYMBOLIC_ONLY`: Strict symbolic name (e.g., `ID`).
        *   `LITERAL_ONLY`: Strict literal name (e.g., `'+'`).
        *   `SYMBOLIC_FIRST`: Tries symbolic, then literal, then integer.
        *   `LITERAL_FIRST`: Tries literal, then symbolic, then integer.
    *   Shortcuts: `appendSymbolicType()` (same as `SYMBOLIC_ONLY`), `appendLiteralType()` (same as `LITERAL_ONLY`).

*   **`appendEOF()`**: Specifically handles the End-Of-File symbol, formatting it as `"EOF"`.

*   **`appendWhitespace()` / `appendWhitespace(String whitespace)`**:
    *   `appendWhitespace("")`: Formats no space, but greedily parses any amount of whitespace.
    *   `appendWhitespace(" ")`: Formats a single space, but greedily parses any amount of whitespace. This is useful for flexible parsing but consistent formatting.

### 4.2. Optional Sections (`startOptional()`, `endOptional()`)

Square brackets (`[]`) in a pattern string (or `startOptional()`/`endOptional()` with the builder) define sections that are optional.
*   **Formatting:** If all components within an optional section can be successfully formatted (e.g., none are strict fields with default values), the section is printed. Otherwise, the entire section is skipped.
*   **Parsing:** The parser attempts to match the optional section. If it fails, the parser backtracks and continues as if the optional section was not present.
*   **Important:** Optional sections cannot be nested.

### 4.3. Alternatives (`withAlternative(SymbolFormatter)`)

You can combine multiple `SymbolFormatter` instances as alternatives using `withAlternative(SymbolFormatter)`. When formatting or parsing, the formatter will try each alternative in the order they were added until one succeeds. This is how `SymbolFormatter.SIMPLE` works, by trying EOF, then Literal, then Symbolic formats.

```java
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolFormatterBuilder;
import io.github.airbag.symbol.TypeFormat;

// Formatter for "TYPE"
SymbolFormatter typeOnlyFormatter = new SymbolFormatterBuilder()
    .appendType(TypeFormat.SYMBOLIC_FIRST)
    .toFormatter();

// Formatter for "'TEXT'"
SymbolFormatter textOnlyFormatter = new SymbolFormatterBuilder()
    .appendLiteral("'")
    .appendText()
    .appendLiteral("'")
    .toFormatter();

// Combine them: try "TYPE" first, then "'TEXT'"
SymbolFormatter combined = typeOnlyFormatter.withAlternative(textOnlyFormatter);
```

## 5. Defining Formatters with Pattern Strings (`SymbolFormatter.ofPattern`)

The `SymbolFormatter.ofPattern(String pattern)` method provides a concise, string-based way to define complex `SymbolFormatter`s. It's especially useful for quickly specifying a format without needing to use the fluent builder API directly. The syntax is inspired by date/time formatting patterns.

### 5.1. Pattern Syntax: Letters, Literals, Escaping, and Quoting

The pattern string allows you to specify symbol fields, literal text, and optional sections.

#### Pattern Letters

The following table summarizes the available pattern letters for different `SymbolField`s and `TypeFormat`s:

| Letter(s) | Component               | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| :-------- | :---------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **I**     | Symbol Type (Integer)   | Always formats the symbol's integer type. Parses an integer and sets it as the symbol type.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **s / S** | Symbol Type (Symbolic)  | **s (Strict):** Formats the symbolic name of the symbol (e.g., "ID"). Fails if no symbolic name is available. Parses a symbolic name and resolves it to a symbol type.<br>**S (Lenient):** Formats the symbolic name if available; otherwise, formats the literal name and lastly the integer type if both fail. Parses either a symbolic or literal name or the integer type.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **l / L** | Symbol Type (Literal)   | **l (Strict):** Formats the literal name of the symbol (e.g., "'='" ). Fails if no literal name is available. Parses a literal name and resolves it to a symbol type.<br>**L (Lenient):** Formats the literal name if available; otherwise, formats the symbolic name and lastly the integer type. Parses either a literal or symbolic name or the integer type.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **x / X** | Symbol Text             | **x (Strict):** Formats the symbol's text without any escaping. Parses text until the next component.<br>**X (Lenient):** Formats the symbol's text with escaping for special characters. Parses escaped text.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| **n / N** | Symbol Index            | **n (Strict):** Formats the symbol's index. Fails if the index is the default value (-1). Parses an integer for the symbol index.<br>**N (Lenient):** Always formats the symbol's index. Parses an integer for the symbol index.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **b / B** | Start Index             | **b (Strict):** Formats the start index. Fails if the index is the default value (-1). Parses an integer for the start index.<br>**B (Lenient):** Always formats the start index. Parses an integer for the start index.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **e / E** | Stop Index              | **e (Strict):** Formats the stop index. Fails if the index is the default value (-1). Parses an integer for the stop index.<br>**E (Lenient):** Always formats the stop index. Parses an integer for the stop index.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **c / C** | Channel                 | **c (Strict):** Formats the channel number. Fails if the channel is the default channel (0). Parses a non-zero integer for the channel.<br>**C (Lenient):** Always formats the channel number, including the default channel. Parses any integer for the channel.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **p / P** | Char Position in Line   | **p (Strict):** Formats the character position in line. Fails if the position is the default value (-1). Parses an integer for the position.<br>**P (Lenient):** Always formats the character position in line. Parses an integer for the position.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **r / R** | Line Number             | **r (Strict):** Formats the line number. Fails if the line number is the default value (-1). Parses an integer for the line number.<br>**R (Lenient):** Always formats the line number. Parses an integer for the line number.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |

#### Literals, Escaping, and Quoting

*   **Unquoted Text:** Any character that is not a recognized pattern letter or a special character (`[]'`) is treated as a literal. E.g., in `s:x`, the colon is a literal.
*   **Escaping:** Use `\` to escape the next character, forcing it to be a literal. E.g., `\s` for literal 's', `\\` for literal '\', `\'` for literal single quote.
*   **Quoting:** Use single quotes (`'`) to quote a sequence of characters as a single literal block. E.g., `'s'` produces "s", `'section[]'` produces "section[]". To include a literal single quote within a quoted block, escape it: `\'`.

#### Optional Sections

Square brackets `[]` create an optional section.
*   **Formatting:** If all components inside can print, they do. Otherwise, the whole section is skipped.
*   **Parsing:** The parser attempts the section; if it fails, it skips it and continues.
A component "fails" if it's strict with a default value, or a `TextOption` has `failOnDefault(true)`.

#### Alternatives

Patterns can specify alternatives using `|`. The formatter will try each alternative until one successfully formats/parses.
```java
// Try formatting as "TYPE" or "TYPE:'TEXT'"
SymbolFormatter formatter = SymbolFormatter.ofPattern("s | s:'X'");
```

### 5.2. Example Pattern (`SymbolFormatter.ANTLR` re-creation)

The `SymbolFormatter.ANTLR` constant is internally defined using a pattern string that showcases many of these features:

```java
public static final SymbolFormatter ANTLR = new SymbolFormatterBuilder().appendLiteral("[@")
        .appendInteger(SymbolField.INDEX)
        .appendLiteral(",")
        .appendInteger(SymbolField.START)
        .appendLiteral(":")
        .appendInteger(SymbolField.STOP)
        .appendLiteral("='")
        .appendText(TextOption.ESCAPED)
        .appendLiteral("',<")
        .appendType(TypeFormat.LITERAL_FIRST)
        .appendLiteral(">")
        .startOptional() // Channel is optional
            .appendLiteral(",channel=")
            .appendInteger(SymbolField.CHANNEL, true) // Strict channel: only print if not default
        .endOptional()
        .appendLiteral(",")
        .appendInteger(SymbolField.LINE)
        .appendLiteral(":")
        .appendInteger(SymbolField.POSITION)
        .appendLiteral("]")
        .toFormatter();

// This is equivalent to pattern (simplified for illustration):
// "[@N,B:E='X',<L>[,channel=c],R:P]"
```

## 6. `SymbolProvider`

The `SymbolProvider` (`io.github.airbag.symbol.SymbolProvider`) is a utility class that simplifies the creation and formatting of `Symbol` lists from ANTLR lexers or string specifications. It acts as an abstraction layer over the ANTLR runtime to produce `Symbol` objects.

### 6.1. Key Functionality

*   **`fromInput(String input)`**: Tokenizes a raw input string using the configured ANTLR `Lexer` and returns a `List<Symbol>`.
*   **`fromSpec(String input)`**: Parses a structured string specification (formatted by an associated `SymbolFormatter`) into a `List<Symbol>`. This is how you define expected token streams in tests.
*   **`format(Symbol symbol)`**: Formats a single `Symbol` into a string using the currently configured `SymbolFormatter`.
*   **`setFormatter(SymbolFormatter formatter)`**: Allows you to override the default `SymbolFormatter` (`SymbolFormatter.SIMPLE`) with a custom one. The provided formatter will be automatically configured with the lexer's `Vocabulary`.

## 7. `SymbolListDiffPrinter` (for comparing Symbol Lists)

The `SymbolListDiffPrinter` (`io.github.airbag.symbol.SymbolListDiffPrinter`) is a utility class for generating human-readable differences between two `List<Symbol>` objects. It leverages `java-diff-utils` and `AsciiTable` to present a clear comparison, highlighting added, deleted, or changed symbols. It uses the `equalizer` provided by `SymbolField` and the formatting capabilities of `SymbolFormatter` to make the diff output meaningful.

This is particularly useful in testing scenarios where you want to compare an actual list of `Symbol`s against an expected list and see precise mismatches.