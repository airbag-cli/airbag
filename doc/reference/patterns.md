# Patterns

The pattern matching feature provides a powerful way to define and search for specific structural compositions within a `DerivationTree`. It allows you to create flexible, reusable patterns and then use them to check for matches, extract subtrees, or find all occurrences of a structure.

## Creating Patterns

Patterns are constructed using the `PatternBuilder` class, which offers a fluent API to append various matching elements. Once built, a `Pattern` object is immutable and thread-safe.

To define a pattern, instantiate a `PatternBuilder` and use its `append...` methods. Each `append` call adds a `TreePatternElement` that specifies a condition for a child node at a particular position relative to the node being matched.

### Pattern Elements

*   **`appendRuleTag(int ruleIndex)`**: Matches a `DerivationTree.Rule` node child by its ANTLR rule index.
*   **`appendRuleTag(int ruleIndex, String label)`**: Matches a `DerivationTree.Rule` node child by index and assigns a `label` to the matched subtree. This label can then be used to retrieve the specific subtree from the `MatchResult`.
*   **`appendSymbolTag(int symbolIndex)`**: Matches a `DerivationTree.Terminal` node child by its ANTLR token type index. This is a convenience for matching specific token types regardless of their text.
*   **`appendSymbolTag(int symbolIndex, String label)`**: Matches a `DerivationTree.Terminal` node child by token type and labels the matched terminal node.
*   **`appendSymbol(Symbol symbol)`**: Matches a `DerivationTree.Terminal` node child based on a full `Symbol` object. By default, it compares the `TEXT`, `TYPE`, `LINE`, and `POSITION` fields of the symbols.
*   **`appendSymbol(Symbol symbol, BiPredicate<Symbol, Symbol> equalizer)`**: Provides fine-grained control for matching `DerivationTree.Terminal` node children. It matches if the custom `BiPredicate` returns `true` when comparing the child node's symbol with the provided reference `symbol`. This is useful for matching only specific fields, like just the `TYPE`.
*   **`appendWildcard()`**: Matches any single child node (rule, terminal, error, or pattern node). It acts as a flexible placeholder when the exact node type or content is not important.

After appending all desired elements, call `toPattern()` to compile and retrieve the immutable `Pattern` object.

## Matching Patterns

Once a `Pattern` is created, you can use its methods to match against `DerivationTree` instances. It's crucial to understand that a pattern describes the *children* of the `DerivationTree` node it is applied to.

### Methods for Matching

*   **`isMatch(DerivationTree t)`**: Checks if the children of the given `DerivationTree t` match the pattern. Returns `true` or `false`. This method does not capture labels.
*   **`match(DerivationTree t)`**: Attempts to match the children of `DerivationTree t` against the pattern. Returns a `MatchResult` object, which indicates success and includes any subtrees captured by labels.
*   **`findAll(DerivationTree t)`**: Traverses the given `DerivationTree t` (including `t` itself and all its descendants) and collects all `DerivationTree` nodes whose children fully match the pattern. Returns a `List` of matching `DerivationTree` instances.

### `MatchResult`

The `MatchResult` record (`isSuccess`, `tree`, `labels`) encapsulates the outcome of a `match()` operation.
*   `isSuccess()`: `true` if the pattern matched, `false` otherwise.
*   `tree()`: The `DerivationTree` node that was successfully matched (i.e., whose children matched the pattern).
*   `labels()`: A `Map<String, DerivationTree>` containing all subtrees that were captured using labels in the pattern definition.
*   `getLabel(String label)`: A convenience method to retrieve a captured subtree by its label.