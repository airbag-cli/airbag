package io.github.airbag.token.format;

/**
 * An exception that is thrown when a token cannot be parsed.
 */
public class TokenParseException extends TokenException {

    private String input;
    private int position;

    public TokenParseException(String message) {
        super(message);
    }

    public TokenParseException(String input, int position) {
        super(String.format("Failed to parse token at position %d in input: \"%s\"", position, input));
        this.input = input;
        this.position = position;
    }

    public TokenParseException(String input, int position, String message) {
        super(message);
        this.input = input;
        this.position = position;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
