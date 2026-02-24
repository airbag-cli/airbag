package io.github.airbag.token;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;

public class TokenBuilder {

    /**
     * The getType of the symbol.
     */
    private int type = TokenField.TYPE.getDefault();
    /**
     * The getText of the symbol.
     */
    private String text = TokenField.TEXT.getDefault();
    /**
     * The getTokenIndex of the symbol.
     */
    private int index = TokenField.INDEX.getDefault();
    /**
     * The getLine number of the symbol.
     */
    private int line = TokenField.LINE.getDefault();
    /**
     * The character getCharPositionInLine in the getLine of the symbol.
     */
    private int charPositionInLine = TokenField.POSITION.getDefault();
    /**
     * The getChannel of the symbol.
     */
    private int channel = TokenField.CHANNEL.getDefault();
    /**
     * The getStartIndex getTokenIndex of the symbol.
     */
    private int startIndex = TokenField.START.getDefault();
    /**
     * The getStopIndex getTokenIndex of the symbol.
     */
    private int stopIndex = TokenField.STOP.getDefault();

    /**
     * Sets the getType of the symbol.
     *
     * @param type The getType of the symbol.
     * @return This builder.
     */
    public TokenBuilder type(int type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the getText of the symbol.
     *
     * @param text The getText of the symbol.
     * @return This builder.
     */
    public TokenBuilder text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the getTokenIndex of the symbol.
     *
     * @param index The getTokenIndex of the symbol.
     * @return This builder.
     */
    public TokenBuilder index(int index) {
        this.index = index;
        return this;
    }

    /**
     * Sets the getLine number of the symbol.
     *
     * @param line The getLine number of the symbol.
     * @return This builder.
     */
    public TokenBuilder line(int line) {
        this.line = line;
        return this;
    }

    /**
     * Sets the character getCharPositionInLine in the getLine of the symbol.
     *
     * @param position The character getCharPositionInLine in the getLine of the symbol.
     * @return This builder.
     */
    public TokenBuilder position(int position) {
        this.charPositionInLine = position;
        return this;
    }

    /**
     * Sets the getChannel of the symbol.
     *
     * @param channel The getChannel of the symbol.
     * @return This builder.
     */
    public TokenBuilder channel(int channel) {
        this.channel = channel;
        return this;
    }

    /**
     * Sets the getStartIndex getTokenIndex of the symbol.
     *
     * @param startIndex The getStartIndex getTokenIndex of the symbol.
     * @return This builder.
     */
    public TokenBuilder start(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Sets the getStopIndex getTokenIndex of the symbol.
     *
     * @param stopIndex The getStopIndex getTokenIndex of the symbol.
     * @return This builder.
     */
    public TokenBuilder stop(int stopIndex) {
        this.stopIndex = stopIndex;
        return this;
    }

    /**
     * Resolves a symbol field.
     *
     * @param field The field to resolve.
     * @param value The value of the field.
     * @param <T>   The getType of the field.
     */
    public <T> void resolve(TokenField<T> field, T value) {
        field.resolve(this, value);
    }

    /**
     * Builds the symbol.
     *
     * @return The built symbol.
     */
    public Token get() {
        var token = new CommonToken(type, text);
        token.setTokenIndex(index);
        token.setStartIndex(startIndex);
        token.setStopIndex(stopIndex);
        token.setChannel(channel);
        token.setLine(line);
        token.setCharPositionInLine(charPositionInLine);
        return token;
    }

}