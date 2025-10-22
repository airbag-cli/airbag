package io.github.airbag.tree;

/**
 * A generic exception for tree-related errors.
 */
public class TreeFormatterException extends RuntimeException {

    /**
     * Constructs a new tree exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public TreeFormatterException(String message) {
        super(message);
    }
}