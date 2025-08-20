package io.github.airbag;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * A utility class for validating ANTLR parse trees against a schema.
 * <p>
 * The Validator class provides a static method to check if a given ANTLR {@link ParseTree} conforms to a
 * user-defined {@link ValidationTree}. This is useful for writing tests for ANTLR grammars, where you want to ensure
 * that a given input produces a parse tree with a specific structure.
 * <p>
 * The matching is done recursively, node by node. The {@link #matches(ValidationTree, ParseTree)} method is the main entry point.
 * It delegates to other private methods for matching specific types of nodes.
 */
public class Validator {

    /**
     * Matches a schema against a parse tree.
     * <p>
     * This method is the main entry point for validating a parse tree against a schema. It recursively
     * traverses the schema and the parse tree, comparing them node by node.
     * <p>
     * The matching logic is as follows:
     * <ul>
     *     <li>If the schema node is a {@link RuleValidationNode}, the actual node must be a {@link RuleNode} with the same rule index.
     *     The children of the two nodes are then matched recursively.</li>
     *     <li>If the schema node is a {@link TerminalValidationNode}, the actual node must be a {@link TerminalNode}.
     *     A weak equality check is performed on the tokens of the two nodes.</li>
     *     <li>If the schema node is an {@link ErrorValidationNode}, the actual node must be an {@link ErrorNode}.
     *     A weak equality check is performed on the tokens of the two nodes.</li>
     * </ul>
     *
     * @param expected The schema to match against. Must not be null.
     * @param actual   The parse tree to match. Must not be null.
     * @return {@code true} if the parse tree matches the schema, {@code false} otherwise.
     * @throws RuntimeException if the schema contains a node that is not a {@link RuleValidationNode}, {@link TerminalValidationNode}, or {@link ErrorValidationNode}.
     */
    public static boolean matches(ValidationTree expected, ParseTree actual) {
        switch(expected) {
            case RuleValidationNode expectedRule -> {
                if (actual instanceof RuleNode actualRule) {
                    return matchRule(expectedRule, actualRule);
                } else {
                    return false;
                }
            }
            case ErrorValidationNode expectedError -> {
                if (actual instanceof ErrorNode actualError) {
                    return Tokens.isWeakEqual(expectedError.getPayload(), actualError.getSymbol());
                } else {
                    return false;
                }
            }
            case TerminalValidationNode expectedTerminal -> {
                if (actual instanceof TerminalNode actualTerminal) {
                    return Tokens.isWeakEqual(expectedTerminal.getPayload(), actualTerminal.getSymbol());
                } else {
                    return false;
                }
            }
            case null, default -> throw new RuntimeException();
        }
    }

    /**
     * Matches a rule schema against a rule node.
     * <p>
     * This method checks if a {@link RuleNode} matches a {@link RuleValidationNode}. It compares the rule indices of the two nodes
     * and then recursively matches their children.
     *
     * @param expectedRule The rule schema to match against. Must not be null.
     * @param actualRule   The rule node to match. Must not be null.
     * @return {@code true} if the rule node matches the rule schema, {@code false} otherwise.
     */
    private static boolean matchRule(RuleValidationNode expectedRule, RuleNode actualRule) {
        if (expectedRule.getPayload() != actualRule.getRuleContext().getRuleIndex()) {
            return false;
        }
        if (expectedRule.getChildCount() != actualRule.getChildCount()) {
            return false;
        }
        for (int i = 0; i < expectedRule.getChildCount(); i++) {
            if (!matches(expectedRule.getChild(i), actualRule.getChild(i))) {
                return false;
            }
        }
        return true;
    }
}
