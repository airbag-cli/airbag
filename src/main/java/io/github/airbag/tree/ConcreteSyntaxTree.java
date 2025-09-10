package io.github.airbag.tree;

/**
 * A concrete syntax tree (CST) is a tree that represents the syntactic structure of a string according to some formal
 * grammar. It is a representation of the source code that is close to the original text, including all of the
 * details of the syntax, such as punctuation and whitespace.
 */
public sealed interface ConcreteSyntaxTree permits Node.Rule, Node.Terminal, Node.Error {
}