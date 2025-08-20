package io.github.airbag;

import org.antlr.v4.runtime.Token;

/**
 * A validation node that represents an error in the parse tree.
 * <p>
 * This validation node is used to match against an {@link org.antlr.v4.runtime.tree.ErrorNode} in the parse tree.
 */
public class ErrorValidationNode extends AbstractValidationNode {

    /**
     * The token that represents the error.
     */
    private final Token token;

    /**
     * Constructs a new ErrorValidationNode with the specified parent and token.
     *
     * @param parent The parent of this schema node.
     * @param token  The token that represents the error.
     */
    protected ErrorValidationNode(ValidationTree parent, Token token) {
        super(parent);
        this.token = token;
    }

    public static ErrorValidationNode attachTo(ValidationTree parent, Token token) {
        return new ErrorValidationNode(parent, token);
    }

    /**
     * Returns the token that represents the error.
     *
     * @return The token that represents the error.
     */
    @Override
    public Token getPayload() {
        return token;
    }
}
