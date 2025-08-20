package io.github.airbag;

import org.antlr.v4.runtime.Token;

/**
 * A validation node that represents a terminal node in the parse tree.
 * <p>
 * This validation node is used to match against a {@link org.antlr.v4.runtime.tree.TerminalNode} in the parse tree.
 */
public class TerminalValidationNode extends AbstractValidationNode {

    /**
     * The token that this schema node represents.
     */
    private final Token token;

    /**
     * Constructs a new TerminalValidationNode with the specified parent and token.
     * This constructor is private to enforce the use of the static factory methods.
     *
     * @param parent The parent of this schema node.
     * @param token  The token that this schema node represents.
     */
    private TerminalValidationNode(ValidationTree parent, Token token) {
        super(parent);
        this.token = token;
    }

    /**
     * Creates a new TerminalValidationNode with the specified parent and token.
     *
     * @param parent The parent of this schema node.
     * @param token  The token that this schema node represents.
     * @return The new TerminalValidationNode.
     */
    public static TerminalValidationNode attachTo(ValidationTree parent, Token token) {
        return new TerminalValidationNode(parent, token);
    }

    /**
     * Returns the token that this schema node represents.
     *
     * @return The token that this schema node represents.
     */
    @Override
    public Token getPayload() {
        return token;
    }
}
