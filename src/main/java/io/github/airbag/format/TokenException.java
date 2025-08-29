package io.github.airbag.format;

/**
 * A generic exception for token-related errors.
 */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }
}