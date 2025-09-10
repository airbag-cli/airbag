package io.github.airbag.tree;

/**
 * A validation syntax tree (VST) is a tree that represents the syntactic structure of a string according to some formal
 * grammar, but with additional information about the validation of the string. It is used to validate the string
 * against a set of rules.
 */
public sealed interface ValidationSyntaxTree extends DerivationTree permits Node.Rule, Node.Error, Node.Terminal, Node.Pattern {
}