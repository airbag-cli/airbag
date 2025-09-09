package io.github.airbag.symbol;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

/**
 * An immutable representation of a lexical symbol, designed as a lightweight alternative
 * to ANTLR's {@link Token}.
 * <p>
 * This record captures the essential attributes of a token—such as its type, text, and position
 * in the source—without maintaining references to the underlying {@link org.antlr.v4.runtime.CharStream}
 * or {@link org.antlr.v4.runtime.TokenSource}. This makes {@code Symbol} objects serializable,
 * long-lived, and suitable for use in contexts where the original input stream is no longer
 * available, such as in post-processing, analysis, or caching scenarios.
 * <p>
 * Each field in this record directly corresponds to a getter method on the {@link Token} interface,
 * providing a clean and predictable mapping.
 *
 * @param index The zero-based index of the symbol within the stream of symbols. This is equivalent
 *              to {@link Token#getTokenIndex()} and is useful for uniquely identifying a symbol's
 *              position in the sequence.
 * @param start The starting character index of this symbol within the original input stream.
 *              This maps to {@link Token#getStartIndex()}.
 * @param stop The stopping character index of this symbol within the original input stream (inclusive).
 *             This maps to {@link Token#getStopIndex()}.
 * @param text The actual text from the input stream that this symbol represents.
 *             This maps to {@link Token#getText()}.
 * @param type An integer representing the lexical type of the symbol (e.g., {@code MyLexer.ID},
 *             {@code MyLexer.INT}). This maps to {@link Token#getType()}.
 * @param channel The channel on which this symbol was emitted. Channels are used to separate
 *                different kinds of symbols, such as sending comments or whitespace to a
 *                hidden channel. This maps to {@link Token#getChannel()}.
 * @param line The line number in the source file where this symbol begins (1-based).
 *             This maps to {@link Token#getLine()}.
 * @param position The character position of the symbol within its line (0-based).
 *                 This maps to {@link Token#getCharPositionInLine()}.
 */
public record Symbol(int index, int start, int stop, String text, int type, int channel, int line,
                     int position) {

    /**
     * Represents the end-of-file symbol type, which is a standard sentinel in ANTLR.
     */
    public static int EOF = -1;

    /**
     * Represents an invalid symbol type.
     */
    public static int INVALID_TYPE = 0;

    /**
     * The default channel for symbols that are part of the main language syntax.
     */
    public static int DEFAULT_CHANNEL = 0;

    /**
     * Constructs a new Symbol from a {@link Builder}.
     *
     * @param builder The builder to use.
     */
    Symbol(Builder builder) {
        this(builder.index,
                builder.startIndex,
                builder.stopIndex,
                builder.text,
                builder.type,
                builder.channel,
                builder.line,
                builder.charPositionInLine);
    }

    /**
     * Constructs a new {@code Symbol} by copying the attributes of an existing ANTLR {@link Token}.
     * This provides a convenient way to convert a standard ANTLR token into a lightweight,
     * immutable {@code Symbol}.
     *
     * @param token The ANTLR token from which to copy attributes. Must not be null.
     */
    public Symbol(Token token) {
        this(token.getTokenIndex(),
                token.getStartIndex(),
                token.getStopIndex(),
                token.getText(),
                token.getType(),
                token.getChannel(),
                token.getLine(),
                token.getCharPositionInLine());
    }

    /**
     * A factory method for creating a {@link Builder}.
     *
     * @return A new {@link Builder} instance.
     */
    public static Symbol.Builder of() {
        return new Symbol.Builder();
    }

    /**
     * Converts this {@code Symbol} back into an ANTLR {@link Token}.
     * <p>
     * This method creates a {@link org.antlr.v4.runtime.CommonToken} and populates it with the
     * attributes from this symbol. This can be useful when interfacing with tools or
     * parts of the ANTLR runtime that expect a standard {@code Token} instance.
     * <p>
     * Note that the resulting token will not have a reference to the original
     * {@link org.antlr.v4.runtime.CharStream} or {@link org.antlr.v4.runtime.TokenSource},
     * as this information is not stored in the {@code Symbol}.
     *
     * @return A new {@link Token} instance with the same attributes as this symbol.
     */
    public Token toToken() {
        var token = new CommonToken(type, text);
        token.setTokenIndex(index);
        token.setStartIndex(start);
        token.setStopIndex(stop);
        token.setChannel(channel);
        token.setLine(line);
        token.setCharPositionInLine(position);
        return token;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return SymbolFormatter.ANTLR.format(this);
    }

    /**
     * A builder for creating {@link Symbol} instances.
     */
    public static class Builder {

        /**
         * The type of the symbol.
         */
        private int type = SymbolField.TYPE.getDefault();
        /**
         * The text of the symbol.
         */
        private String text = SymbolField.TEXT.getDefault();
        /**
         * The index of the symbol.
         */
        private int index = SymbolField.INDEX.getDefault();
        /**
         * The line number of the symbol.
         */
        private int line = SymbolField.LINE.getDefault();
        /**
         * The character position in the line of the symbol.
         */
        private int charPositionInLine = SymbolField.POSITION.getDefault();
        /**
         * The channel of the symbol.
         */
        private int channel = SymbolField.CHANNEL.getDefault();
        /**
         * The start index of the symbol.
         */
        private int startIndex = SymbolField.START.getDefault();
        /**
         * The stop index of the symbol.
         */
        private int stopIndex = SymbolField.STOP.getDefault();

        /**
         * Sets the type of the symbol.
         *
         * @param type The type of the symbol.
         * @return This builder.
         */
        public Symbol.Builder type(int type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the text of the symbol.
         *
         * @param text The text of the symbol.
         * @return This builder.
         */
        public Symbol.Builder text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the index of the symbol.
         *
         * @param index The index of the symbol.
         * @return This builder.
         */
        public Symbol.Builder index(int index) {
            this.index = index;
            return this;
        }

        /**
         * Sets the line number of the symbol.
         *
         * @param line The line number of the symbol.
         * @return This builder.
         */
        public Symbol.Builder line(int line) {
            this.line = line;
            return this;
        }

        /**
         * Sets the character position in the line of the symbol.
         *
         * @param position The character position in the line of the symbol.
         * @return This builder.
         */
        public Symbol.Builder position(int position) {
            this.charPositionInLine = position;
            return this;
        }

        /**
         * Sets the channel of the symbol.
         *
         * @param channel The channel of the symbol.
         * @return This builder.
         */
        public Symbol.Builder channel(int channel) {
            this.channel = channel;
            return this;
        }

        /**
         * Sets the start index of the symbol.
         *
         * @param startIndex The start index of the symbol.
         * @return This builder.
         */
        public Symbol.Builder start(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        /**
         * Sets the stop index of the symbol.
         *
         * @param stopIndex The stop index of the symbol.
         * @return This builder.
         */
        public Symbol.Builder stop(int stopIndex) {
            this.stopIndex = stopIndex;
            return this;
        }

        /**
         * Resolves a symbol field.
         *
         * @param field The field to resolve.
         * @param value The value of the field.
         * @param <T>   The type of the field.
         */
        public <T> void resolve(SymbolField<T> field, T value) {
            field.resolve(this, value);
        }

        /**
         * Builds the symbol.
         *
         * @return The built symbol.
         */
        public Symbol get() {
            return new Symbol(this);
        }

    }
}
