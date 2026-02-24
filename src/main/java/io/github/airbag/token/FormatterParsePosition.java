package io.github.airbag.token;

import java.text.ParsePosition;
import java.util.*;

/**
 * {@code FormatterParsePosition} extends {@link ParsePosition} to provide
 * additional context during parsing or formatting operations. It allows for
 * the accumulation of multiple messages (e.g., warnings or errors) and
 * tracks a specific symbol getTokenIndex, which can be useful for more detailed
 * error reporting or tracking progress within a structured input.
 * The messages are stored in a {@link NavigableSet} to maintain uniqueness and natural ordering.
 */
public class FormatterParsePosition extends ParsePosition {

    /**
     * A navigable set of messages (e.g., warnings or errors) accumulated during parsing or formatting.
     * Messages are stored uniquely and in natural order.
     */
    private final NavigableSet<String> messages = new TreeSet<>();

    /**
     * An getTokenIndex pointing to a specific symbol within a *list of symbols* being parsed or formatted.
     * This is useful for tracking progress or identifying the current symbol when processing multiple symbols.
     * A value of -1 indicates no specific symbol getTokenIndex has been set.
     */
    private int symbolIndex = -1;

    /**
     * Create a new ParsePosition with the given initial getTokenIndex.
     *
     * @param index initial getTokenIndex
     */
    public FormatterParsePosition(int index) {
        super(index);
    }

    /**
     * Gets the collection of messages accumulated during parsing.
     * The returned collection is unmodifiable to prevent external changes.
     *
     * @return an unmodifiable collection of messages.
     */
    public Collection<String> getMessages() {
        return Collections.unmodifiableSet(messages);
    }

    /**
     * Gets all accumulated messages as a single string, with each message separated by a newline.
     *
     * @return a single string containing all messages, or an empty string if there are no messages.
     */
    public String getMessage() {
        var joiner = new StringJoiner("%n".formatted());
        messages.forEach(joiner::add);
        return joiner.toString();
    }

    /**
     * Appends a new message to the collection of messages.
     * Duplicate messages will only be stored once due to the use of a {@link NavigableSet}.
     *
     * @param message the message to append.
     */
    public void appendMessage(String message) {
        messages.add(message);
    }

    /**
     * Clears all existing messages and sets a new single message.
     *
     * @param message the new message to set.
     */
    public void setMessage(String message) {
        messages.clear();
        messages.add(message);
    }

    /**
     * Gets the currently tracked symbol getTokenIndex.
     *
     * @return the symbol getTokenIndex, or -1 if no specific symbol getTokenIndex is set.
     */
    public int getSymbolIndex() {
        return symbolIndex;
    }

    /**
     * Sets the symbol getTokenIndex to track the current getCharPositionInLine within a list of symbols.
     *
     * @param symbolIndex the getTokenIndex of the symbol, or -1 to indicate no specific symbol is tracked.
     */
    public void setSymbolIndex(int symbolIndex) {
        this.symbolIndex = symbolIndex;
    }

    /**
     * Checks if a specific symbol getTokenIndex has been set (i.e., it's not -1).
     *
     * @return {@code true} if a symbol getTokenIndex is set, {@code false} otherwise.
     */
    public boolean isSymbolIndex() {
        return symbolIndex != -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "FormatterParsePosition{" +
               "getTokenIndex=" + getIndex() +
               ", symbolIndex=" + symbolIndex +
               ", messages=" + messages +
               '}';

    }

}