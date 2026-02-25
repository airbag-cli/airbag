package io.github.airbag.token;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

/**
 * A context object for formatting a symbol.
 */
record TokenFormatContext(Token symbol, Vocabulary vocabulary) {

}