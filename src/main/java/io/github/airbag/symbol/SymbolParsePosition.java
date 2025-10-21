package io.github.airbag.symbol;

import java.text.ParsePosition;

public class SymbolParsePosition extends ParsePosition {

    private String message;

    /**
     * Create a new ParsePosition with the given initial index.
     *
     * @param index initial index
     */
    public SymbolParsePosition(int index) {
        super(index);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}