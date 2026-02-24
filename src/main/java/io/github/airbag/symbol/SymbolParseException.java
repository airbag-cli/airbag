package io.github.airbag.symbol;

/**
 * An exception that is thrown when a symbol cannot be parsed.
 */
public class SymbolParseException extends SymbolFormatterException {

    private int line;
    private String input;
    private int position;

    /**
     * Constructs a new {@code SymbolParseException} with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public SymbolParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code SymbolParseException} with the specified input, position, and detail message.
     * The message is formatted to include the input and the error position.
     *
     * @param input    the input string that caused the parse exception.
     * @param position the position in the input string where the error was found.
     * @param message  the detail message explaining the parse error.
     */
    public SymbolParseException(String input, int position, String message) {
        super(constructMessage(input, position, message));
        this.input = input;
        this.position = position;
    }

    public SymbolParseException(String input, int line, int position, String message) {
        super(constructMessage(input,line , position, message));
        this.input = input;
        this.position = position;
    }

    public SymbolParseException(String message, int line, int position) {
        super(message);
        this.line = line;
        this.position = position;
    }

    /**
     * Constructs a detailed error message that includes the input string,
     * marks the position of the error, and provides the given message.
     *
     * @param input    The input string.
     * @param position The index in the input where the error occurred.
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
     * Gets the position where the error was found.
     *
     * @return the position where the error was found.
     */
    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }
}