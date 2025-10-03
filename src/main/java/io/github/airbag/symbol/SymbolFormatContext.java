package io.github.airbag.symbol;

import org.antlr.v4.runtime.Vocabulary;

/**
 * A context object for formatting a symbol.
 */
class SymbolFormatContext {

    private final Symbol symbol;
    private final Vocabulary vocabulary;

    /**
     * @param symbol The symbol to be formatted.
     */
    SymbolFormatContext(Symbol symbol, Vocabulary vocabulary) {
        this.symbol = symbol;
        this.vocabulary = vocabulary;
    }

    public Symbol symbol() {
        return symbol;
    }

    public Vocabulary vocabulary() {
        return vocabulary;
    }

}