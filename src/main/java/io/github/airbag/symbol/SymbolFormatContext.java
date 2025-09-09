package io.github.airbag.symbol;

import org.antlr.v4.runtime.Vocabulary;

/**
 * A context object for formatting a symbol.
 *
 * @param symbol The symbol to be formatted.
 */
record SymbolFormatContext(Symbol symbol, Vocabulary vocabulary) {

}