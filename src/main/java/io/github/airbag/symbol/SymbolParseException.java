package io.github.airbag.symbol;

/**
 * An exception that is thrown when a symbol cannot be parsed.
 */
public class SymbolParseException extends SymbolException {

    private String input;
    private int position;

    public SymbolParseException(String message) {
        super(message);
    }

    public SymbolParseException(String input, int position) {
        super(String.format("Failed to parse symbol at position %d in input: \"%s\"", position, input));
        this.input = input;
        this.position = position;
    }

    public SymbolParseException(String input, int position, String message) {
        super(message);
        this.input = input;
        this.position = position;
    }

    /**
     * Gets the input string that caused the parse exception.
     *
     * @return the input string.
     */
    public String getInput() {
        return input;
    }


    /**
     * Gets the position where the error was found.
     *
     * @return the position where the error was found.
     */
    public int getPosition() {
        return position;
    }

}
