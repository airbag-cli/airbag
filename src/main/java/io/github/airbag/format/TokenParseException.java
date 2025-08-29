package io.github.airbag.format;

/**
 * An exception that is thrown when a token cannot be parsed.
 */
public class TokenParseException extends TokenException {

    private String input;
    private int position;

    public TokenParseException(String input, int position) {
        super(String.format("Failed to parse token at position %d in input: \"%s\"", position, input));
        this.input = input;
        this.position = position;
    }
}
