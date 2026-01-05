package io.github.airbag.symbol;

import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FormatterParsePosition extends ParsePosition {

    private final NavigableSet<String> messages = new TreeSet<>();
    private int symbolIndex = -1;

    /**
     * Create a new ParsePosition with the given initial index.
     *
     * @param index initial index
     */
    public FormatterParsePosition(int index) {
        super(index);
    }

    public Collection<String> getMessages() {
        return Collections.unmodifiableSet(messages);
    }

    public String getMessage() {
        var joiner = new StringJoiner("%n".formatted());
        messages.forEach(joiner::add);
        return joiner.toString();
    }

    public void appendMessage(String message) {
        messages.add(message);
    }

    public void setMessage(String message) {
        messages.clear();
        messages.add(message);
    }

    public int getSymbolIndex() {
        return symbolIndex;
    }

    public void setSymbolIndex(int symbolIndex) {
        this.symbolIndex = symbolIndex;
    }

    public boolean isSymbolIndex() {
        return symbolIndex != -1;
    }
}