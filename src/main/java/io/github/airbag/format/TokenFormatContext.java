package io.github.airbag.format;

import org.antlr.v4.runtime.Token;

/**
 * A context object for formatting a token.
 *
 * @param token The token to be formatted.
 */
record TokenFormatContext(Token token) {

}