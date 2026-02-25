package io.github.airbag.token;

/**
 * An exception that is thrown when a symbol cannot be parsed.
 */
public class TokenParseException extends TokenFormatterException {

    private int line;
    private String input;
    private int position;

    /**
     * Constructs a new {@code TokenParseException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public TokenParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code TokenParseException} with the specified input, getCharPositionInLine, and detail message.
     * The message is formatted to include the input and the error getCharPositionInLine.
     *
     * @param input    the input string that caused the parse exception.
     * @param position the getCharPositionInLine in the input string where the error was found.
     * @param message  the detail message explaining the parse error.
     */
    public TokenParseException(String input, int position, String message) {
        super(constructMessage(input, position, message));
        this.input = input;
        this.position = position;
    }

    public TokenParseException(String input, int line, int position, String message) {
        super(constructMessage(input,line , position, message));
        this.input = input;
        this.position = position;
    }

    public TokenParseException(String message, int line, int position) {
        super(message);
        this.line = line;
        this.position = position;
    }

    /**
     * Constructs a detailed error message that includes the input string,
     * marks the getCharPositionInLine of the error, and provides the given message.
     *
     * @param input    The input string.
     * @param position The getTokenIndex in the input where the error occurred.
     * @param message  The specific error message.
     * @return A formatted string detailing the parse failure.
     */
    private static String constructMessage(String input, int position, String message) {
        String markedInput = new StringBuilder(input).insert(position, ">>").toString();
        return """
                Parse failed at index %d:
                %s

                %s
                """.formatted(position, message, markedInput);
    }

    private static String constructMessage(String input, int line, int charPositionInLine, String message) {
        return """
                Parse failed at line %d with position %d:
                %s

                %s
                """.formatted(line, charPositionInLine, message, input);
    }

    /**
     * Gets the input string that caused the parse exception.
     *
     * @return the input string.
     */
    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;

    }


    /**
     * Gets the getCharPositionInLine where the error was found.
     *
     * @return the getCharPositionInLine where the error was found.
     */
    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }
}