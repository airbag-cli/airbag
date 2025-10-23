package io.github.airbag.symbol;

/**
 * A generic exception for symbol-related errors.
 */
public class SymbolFormatterException extends RuntimeException {

    /**
     * Constructs a new symbol exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public SymbolFormatterException(String message) {
        super(message);
    }
}