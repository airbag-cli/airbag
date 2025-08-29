package io.github.airbag.token;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

/**
 * A builder for creating {@link Token} objects.
 * <p>
 * This builder provides a fluent API for constructing a token with specific properties.
 * It is useful for creating tokens in tests or other parts of the validation logic.
 */
public class TokenBuilder {

    /**
     * The type of the token.
     */
    private int type;
    /**
     * The text of the token.
     */
    private String text;
    /**
     * The index of the token.
     */
    private int index = -1;
    /**
     * The line number of the token.
     */
    private int line;
    /**
     * The character position in the line of the token.
     */
    private int charPositionInLine = -1;
    /**
     * The channel of the token.
     */
    private int channel = Token.DEFAULT_CHANNEL;
    /**
     * The start index of the token.
     */
    private int startIndex = -1;
    /**
     * The stop index of the token.
     */
    private int stopIndex = -1;

    /**
     * Sets the type of the token.
     *
     * @param type The type of the token.
     * @return This builder.
     */
    public TokenBuilder type(int type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the text of the token.
     *
     * @param text The text of the token.
     * @return This builder.
     */
    public TokenBuilder text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the index of the token.
     *
     * @param index The index of the token.
     * @return This builder.
     */
    public TokenBuilder index(int index) {
        this.index = index;
        return this;
    }

    /**
     * Sets the line number of the token.
     *
     * @param line The line number of the token.
     * @return This builder.
     */
    public TokenBuilder line(int line) {
        this.line = line;
        return this;
    }

    /**
     * Sets the character position in the line of the token.
     *
     * @param charPositionInLine The character position in the line of the token.
     * @return This builder.
     */
    public TokenBuilder charPositionInLine(int charPositionInLine) {
        this.charPositionInLine = charPositionInLine;
        return this;
    }

    /**
     * Sets the channel of the token.
     *
     * @param channel The channel of the token.
     * @return This builder.
     */
    public TokenBuilder channel(int channel) {
        this.channel = channel;
        return this;
    }

    /**
     * Sets the start index of the token.
     *
     * @param startIndex The start index of the token.
     * @return This builder.
     */
    public TokenBuilder startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Sets the stop index of the token.
     *
     * @param stopIndex The stop index of the token.
     * @return This builder.
     */
    public TokenBuilder stopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
        return this;
    }

    /**
     * Builds the token.
     *
     * @return The built token.
     */
    public Token get() {
        var token = new CommonToken(type, text);
        token.setTokenIndex(index);
        token.setLine(line);
        token.setCharPositionInLine(charPositionInLine);
        token.setChannel(channel);
        token.setStartIndex(startIndex);
        token.setStopIndex(stopIndex);
        return token;
    }

    /**
     * Resolves a token field.
     * @param field The field to resolve.
     * @param value The value of the field.
     * @return This builder.
     * @param <T> The type of the field.
     */
    public <T> TokenBuilder resolve(TokenField<T> field, T value) {
        field.resolve(this, value);
        return this;
    }
}