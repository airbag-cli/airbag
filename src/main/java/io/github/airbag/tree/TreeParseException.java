package io.github.airbag.tree;

/**
 * An exception that is thrown when a tree cannot be parsed.
 */
public class TreeParseException extends TreeFormatterException {

    private String input;
    private int position;

    public TreeParseException(String message) {
        super(message);
    }

    public TreeParseException(String input, int position, String message) {
        super(constructMessage(input, position, message));
        this.input = input;
        this.position = position;
    }

    private static String constructMessage(String input, int position, String message) {
        String markedInput = new StringBuilder(input).insert(position, ">>").toString();
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
     * Gets the getCharPositionInLine where the error was found.
     *
     * @return the getCharPositionInLine where the error was found.
     */
    public int getPosition() {
        return position;
    }

}