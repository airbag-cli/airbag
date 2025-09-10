package io.github.airbag.tree;

/**
 * A derivation tree is a data structure that represents the derivation of a string from a grammar.
 * It shows how the rules of the grammar are applied to derive the string.
 * This interface is the base interface for all derivation trees.
 */
public interface DerivationTree {

    int index();

}