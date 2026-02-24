package io.github.airbag.token;

/**
 * A generic exception for symbol-related errors.
 */
public class TokenFormatterException extends RuntimeException {

    /**
     * Constructs a new symbol exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public TokenFormatterException(String message) {
        super(message);
    }
}