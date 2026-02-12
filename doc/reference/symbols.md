# Customizing Symbol Formats

This guide explains how to control the conversion of `Symbol` objects to and from text using `SymbolFormatter`. A `Symbol` is Airbag's representation of a token (like a keyword or identifier), and a `SymbolFormatter` is the tool that defines its text format.

Mastering formatters is key to writing clear test specifications and debugging your grammar.

## 1. The Basics: `Symbol` and `SymbolFormatter`

*   **`Symbol`**: An immutable representation of a lexical token. It decouples from the ANTLR runtime, making it ideal for use in tests. It contains fields like `TYPE` (e.g., `MyLexer.ID`), `TEXT` (e.g., `"myVar"`), `LINE`, and `CHANNEL`.

*   **`SymbolFormatter`**: A bidirectional tool that converts a `Symbol` to a `String` (formatting) and a `String` back to a `Symbol` (parsing). This is how you can write an expected token stream in a human-readable string and have Airbag parse it into a list of `Symbol` objects for comparison.

## 2. Using Predefined Formatters

Airbag provides two ready-to-use formatters that cover most common scenarios.

### `SymbolFormatter.SIMPLE`

This is the default formatter used in most Airbag testing APIs like `airbag.getSymbolProvider().fromSpec()`. It's designed to be human-readable and concise.

It intelligently formats a symbol based on its properties:
1.  **EOF**: The end-of-file symbol is simply `EOF`.
2.  **Literal Token**: A token with a literal name (like `+` or `=`) is formatted as that literal (e.g., `'+'`).
3.  **Symbolic Token**: A token with a symbolic name (like an identifier) is formatted as `(NAME 'text')`.

**Examples:**
```
(ID 'a')
'='
(INT '10')
'+'
(ID 'b')
EOF
```

If a symbol is on a non-default channel, the channel is appended:
```
(COMMENT:1 '// a comment')
```

### `SymbolFormatter.ANTLR`

This formatter mimics the verbose `toString()` output of ANTLR's `CommonToken`. It's useful for deep debugging when you need to see every detail of a symbol.

**Format:** `[@{index},{start}:{stop}='{text}',<{type}>,channel={channel},{line}:{pos}]`

**Example:**
```
[@0,0:0='a',<ID>,1:0]
```

## 3. Creating Custom Formatters

While the predefined formatters are often sufficient, you may need a custom format for specialized logging or to handle unique grammar conventions. Airbag offers two powerful ways to create your own formatters.

### Method 1: Pattern Strings (Recommended)

The easiest way to define a custom formatter is with a pattern string, similar to `DateTimeFormatter`. You provide a string where special letters represent `Symbol` fields.

**Example: A `log-friendly` format**

Let's say you want a format `[LINE:POS] TYPE 'TEXT'`. You can achieve this with the pattern string: `"[R:P] s \\'X\\'"`.

```java
import io.github.airbag.symbol.SymbolFormatter;

// Create a formatter from a pattern
SymbolFormatter logFormatter = SymbolFormatter.ofPattern("[R:P] s \\'X\\'");

// Assume 'symbol' is a Symbol object for an ID "myVar" on line 1, pos 5
String output = logFormatter.format(symbol); 
// output will be: "[1:5] ID 'myVar'"
```

#### Pattern Syntax

| Letter(s) | Component      | Description                                                                 |
|:----------|:---------------|:----------------------------------------------------------------------------|
| `I`       | Type (Integer) | Integer value of the token type.                                            |
| `s` / `S` | Type (Symbolic)| **s**: Strict symbolic name (e.g., `ID`).<br/>**S**: Lenient, falls back to literal/integer. |
| `l` / `L` | Type (Literal) | **l**: Strict literal name (e.g., `'+'`).<br/>**L**: Lenient, falls back to symbolic/integer. |
| `x` / `X` | Text           | **x**: Raw text.<br/>**X**: Text with escaping.                              |
| `n` / `N` | Index          | **n**: Strict index (fails if default).<br/>**N**: Always formats index.    |
| `c` / `C` | Channel        | **c**: Strict channel (fails if default).<br/>**C**: Always formats channel.|
| `r` / `R` | Line Number    | **r**: Strict line (fails if default).<br/>**R**: Always formats line.      |
| `p` / `P` | Position       | **p**: Strict position (fails if default).<br/>**P**: Always formats position.|

*   **Literals**: Any character that isn't a pattern letter is treated as a literal (e.g., `:`, `'`, ` `). Use single quotes `'...'` to enforce a block of text as a literal.
*   **Optional Sections `[...]`**: Parts of the pattern inside square brackets are optional. They are only rendered if all fields within them have non-default values.
*   **Alternatives `|`**: A pipe character separates alternative patterns. The formatter tries them in order until one succeeds.

### Method 2: The `SymbolFormatterBuilder`

For programmatic construction or very complex formats, you can use the `SymbolFormatterBuilder`. It gives you fine-grained control by letting you append each component step-by-step.

Here is the same `log-friendly` formatter from before, built with the builder:

```java
import io.github.airbag.symbol.SymbolFormatterBuilder;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.TypeFormat;
import io.github.airbag.symbol.TextOption;

SymbolFormatter logFormatter = new SymbolFormatterBuilder()
    .appendLiteral("[")
    .appendInteger(SymbolField.LINE)
    .appendLiteral(":")
    .appendInteger(SymbolField.POSITION)
    .appendLiteral("]")
    .appendWhitespace(" ")
    .appendType(TypeFormat.SYMBOLIC_FIRST) // 's'
    .appendWhitespace(" ")
    .appendLiteral("'")
    .appendText(TextOption.ESCAPED) // 'X'
    .appendLiteral("'")
    .toFormatter();
```

## 4. Advanced Topics

### The Role of `Vocabulary`

To map a `Symbol`'s integer `TYPE` to a meaningful name like `"ID"` or `'+'`, the formatter needs an ANTLR `Vocabulary`. The `Airbag` test harness automatically configures this for you. When using formatters manually, you can attach a vocabulary like this:

```java
// Get the vocabulary from your generated lexer
Vocabulary lexerVocabulary = MyLexer.VOCABULARY;

// Create a new formatter instance with the vocabulary attached
SymbolFormatter configuredFormatter = SymbolFormatter.SIMPLE.withVocabulary(lexerVocabulary);
```

### Parsing Symbol Lists

When parsing a string containing multiple symbols, you typically use `SymbolProvider`, which handles the process internally. The provider uses a lenient parsing mode that doesn't fail on the first error, allowing it to parse an entire list and report any issues collectively. This process relies on `FormatterParsePosition` to track the current position and any errors encountered in the input string.

## 5. Related Utilities

*   **`SymbolProvider`**: A helper class obtained from `airbag.getSymbolProvider()` that acts as a factory. It uses the configured `SymbolFormatter` to tokenize raw input (`fromInput`) or parse specification strings (`fromSpec`) into `List<Symbol>` objects.

*   **`SymbolListDiffPrinter`**: A utility that generates a clear, colored diff between two lists of symbols. This is what powers `airbag.assertSymbolList()` to show you exactly what's different between your expected and actual token streams.