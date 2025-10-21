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

    public SymbolParseException(String input, int position, String message) {
        super(constructMessage(input, position, message));
        this.input = input;
        this.position = position;
    }

    private static String constructMessage(String input, int position, String message) {
        String markedInput = new StringBuilder(input).insert(position, ">>" + "").toString();
        return """
                Parse failed at index %d:
                %s

                %s
                """.formatted(position, message, markedInput);
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