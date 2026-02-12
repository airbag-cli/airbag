# Queries

Queries provide a powerful and flexible way to navigate and extract specific nodes from a `DerivationTree` using a concise, path-like syntax. They are particularly useful for testing, refactoring, or analyzing ANTLR parse trees.

## Query Syntax

A query is constructed as a sequence of one or more *query elements*, each consisting of an optional inversion operator, a navigator, and a filter.

### Components of a Query Element

1.  **Inversion Operator (`!`)**:
    *   `!`: Preceding a filter inverts its matching logic. For example, `!ruleName` would match nodes that are *not* `ruleName`.
    *   **Important Note on Wildcard Inversion (`!*`)**: If `!` is applied to a wildcard filter (`*`), it effectively matches nothing. This is because the `Wildcard` element's filter returns `true` for any node (if not inverted) and `false` for any node (if inverted).
    *   **Inversion Logic vs. ANTLR Queries**: Note that the inversion logic in Airbag queries may differ from how negation is handled in standard ANTLR XPath-like queries. In Airbag, `!` directly inverts the filter for the *current* `QueryElement`.

2.  **Navigators**:
    Navigators define how to traverse the `DerivationTree` to find candidate nodes for filtering.

    *   `//` (Anywhere/Descendant):
        *   If `//` is the *first* part of the query, it selects all nodes in the entire `DerivationTree` (including the starting node) as candidates (`ALL` traversal).
        *   If `//` follows another filter, it selects all *descendants* (children, grandchildren, etc.) of the previously matched nodes as candidates (`DESCENDANTS` traversal).
    *   `/` (Root/Child):
        *   If `/` is the *first* part of the query, it considers only the starting node itself as a candidate (`ROOT` traversal).
        *   If `/` follows another filter, it selects only the *direct children* of the previously matched nodes as candidates (`CHILDREN` traversal).

3.  **Filters**:
    Filters specify the criteria for selecting nodes among the candidates provided by the navigator.

    *   `*` (Wildcard): Matches any single `DerivationTree` node (rule node, terminal node, error node, or pattern node).
    *   `ruleName` (e.g., `expr`, `stat`, `atom`): Matches `DerivationTree.Rule` nodes whose rule name corresponds to the given identifier. Rule names are typically lowercase and case-sensitive.
    *   `TOKEN_NAME` (e.g., `INT`, `ID`, `PLUS`): Matches `DerivationTree.Terminal` nodes whose token name corresponds to the given identifier. Token names are typically uppercase and case-sensitive.
    *   `integerIndex` (e.g., `0`, `42`): Matches `DerivationTree.Rule` nodes whose rule index matches the specified integer.
    *   `tokenTypeIndex` (e.g., `3`, `-1`): Matches `DerivationTree.Terminal` nodes whose token type index matches the specified integer.
    *   `'string_literal'` (e.g., `'if'`, `'while'`): Interpreted as a symbolic token name. This will match `DerivationTree.Terminal` nodes whose token type corresponds to the symbolic name represented by the string literal. For example, `'if'` would match a token of type `IF` (assuming the lexer defines an `IF` token for the "if" keyword).

### Examples of Query Syntax

| Query                     | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| :------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `//atom`                  | Selects all `atom` rule nodes present anywhere in the `DerivationTree`.                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `/expr/atom`              | Selects all `atom` rule nodes that are direct children of an `expr` rule node. The query starts by considering the root node (first `/`), then its direct children that are `expr`s, and then from those `expr` nodes, their direct children that are `atom`s.                                                                                                                                                                                                                                          |
| `//INT`                   | Selects all `INT` token nodes present anywhere in the `DerivationTree`.                                                                                                                                                                                                                                                                                                                                                                                                                               |
| `/!term`                  | From the starting node, selects all direct children that are *not* `term` rule nodes.                                                                                                                                                                                                                                                                                                                                                                                                               |
| `//expr//ID`              | Finds all `expr` rule nodes anywhere in the tree, and then from each of those `expr` nodes, finds any `ID` token nodes among their descendants.                                                                                                                                                                                                                                                                                                                                                       |
| `//0`                     | Selects all rule nodes with rule index `0` anywhere in the tree. (e.g., if rule `expr` has index `0`).                                                                                                                                                                                                                                                                                                                                                                                               |
| `//4`                     | Selects all terminal nodes with token type index `4` anywhere in the tree. (e.g., if token `PLUS` has type index `4`).                                                                                                                                                                                                                                                                                                                                                                               |
| `//'if'`                  | Selects all terminal nodes whose token type corresponds to the symbolic name `'if'` anywhere in the tree. This would match the token type for the keyword `if`.                                                                                                                                                                                                                                                                                                                                      |
| `!*`                      | This query will always return an empty set of results. The `Wildcard` filter (when inverted) will never match any node.                                                                                                                                                                                                                                                                                                                                                                              |