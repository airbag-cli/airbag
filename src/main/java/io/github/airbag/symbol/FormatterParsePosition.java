package io.github.airbag.symbol;

import java.text.ParsePosition;

public class FormatterParsePosition extends ParsePosition {

    private String message;

    /**
     * Create a new ParsePosition with the given initial index.
     *
     * @param index initial index
     */
    public FormatterParsePosition(int index) {
        super(index);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}