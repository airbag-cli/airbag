package io.github.airbag.symbol;

import java.text.ParsePosition;
import java.util.*;

/**
 * {@code FormatterParsePosition} extends {@link ParsePosition} to provide
 * additional context during parsing or formatting operations. It allows for
 * the accumulation of multiple messages (e.g., warnings or errors) and
 * tracks a specific symbol index, which can be useful for more detailed
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
     * An index pointing to a specific symbol within a *list of symbols* being parsed or formatted.
     * This is useful for tracking progress or identifying the current symbol when processing multiple symbols.
     * A value of -1 indicates no specific symbol index has been set.
     */
    private int symbolIndex = -1;

    /**
     * Create a new ParsePosition with the given initial index.
     *
     * @param index initial index
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
     * Gets the currently tracked symbol index.
     *
     * @return the symbol index, or -1 if no specific symbol index is set.
     */
    public int getSymbolIndex() {
        return symbolIndex;
    }

    /**
     * Sets the symbol index to track the current position within a list of symbols.
     *
     * @param symbolIndex the index of the symbol, or -1 to indicate no specific symbol is tracked.
     */
    public void setSymbolIndex(int symbolIndex) {
        this.symbolIndex = symbolIndex;
    }

    /**
     * Checks if a specific symbol index has been set (i.e., it's not -1).
     *
     * @return {@code true} if a symbol index is set, {@code false} otherwise.
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
               "index=" + getIndex() +
               ", symbolIndex=" + symbolIndex +
               ", messages=" + messages +
               '}';

    }

}