package io.github.airbag.token;

import org.antlr.v4.runtime.Token;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a specific field or attribute of an ANTLR {@link Token}.
 * <p>
 * This class provides a type-safe way to define and access various properties of a {@link Token},
 * such as its type, text, line number, etc. Each {@code TokenField} encapsulates a field's name
 * and a function to extract its value from a {@code Token} instance.
 * <p>
 * The class provides predefined static instances for common token fields.
 *
 * @param <T> The type of the value this field extracts from a token.
 */
public class TokenField<T> {

    /**
     * A {@link TokenField} that extracts the token type.
     *
     * @see Token#getType()
     */
    public static final TokenField<Integer> TYPE = new TokenField<>("type", Token::getType,
            TokenBuilder::type);

    /**
     * A {@link TokenField} that extracts the token's text.
     *
     * @see Token#getText()
     */
    public static final TokenField<String> TEXT = new TokenField<>("text",
            Token::getText,
            TokenBuilder::text);

    /**
     * A {@link TokenField} that extracts the token's index.
     *
     * @see Token#getTokenIndex()
     */
    public static final TokenField<Integer> INDEX = new TokenField<>("index",
            Token::getTokenIndex,
            TokenBuilder::index);

    /**
     * A {@link TokenField} that extracts the line number where the token appears.
     *
     * @see Token#getLine()
     */
    public static final TokenField<Integer> LINE = new TokenField<>("line",
            Token::getLine,
            TokenBuilder::line);

    /**
     * A {@link TokenField} that extracts the character position within the line.
     *
     * @see Token#getCharPositionInLine()
     */
    public static final TokenField<Integer> POSITION = new TokenField<>("position",
            Token::getCharPositionInLine, TokenBuilder::charPositionInLine);

    /**
     * A {@link TokenField} that extracts the channel of the token.
     *
     * @see Token#getChannel()
     */
    public static final TokenField<Integer> CHANNEL = new TokenField<>("channel",
            Token::getChannel, TokenBuilder::channel);

    /**
     * A {@link TokenField} that extracts the starting character index of the token in the input stream.
     *
     * @see Token#getStartIndex()
     */
    public static final TokenField<Integer> START = new TokenField<>("start",
            Token::getStartIndex,
            TokenBuilder::startIndex);

    /**
     * A {@link TokenField} that extracts the ending character index of the token in the input stream.
     *
     * @see Token#getStopIndex()
     */
    public static final TokenField<Integer> STOP = new TokenField<>("stop",
            Token::getStopIndex,
            TokenBuilder::stopIndex);

    private final String name;

    private final Function<? super Token, T> accessor;
    private final BiConsumer<TokenBuilder, T> resolver;

    /**
     * Constructs a new {@code TokenField}.
     *
     * @param name     The name of the field. This is used for identification and debugging.
     * @param accessor A {@link Function} that, when applied to a {@link Token}, returns the value of this field.
     */
    private TokenField(String name,
                      Function<? super Token, T> accessor,
                      BiConsumer<TokenBuilder, T> resolver) {
        this.name = name;
        this.accessor = accessor;
        this.resolver = resolver;
    }

    /**
     * Accesses the value of this field from the given {@link Token}.
     *
     * @param token The token from which to extract the value.
     * @return The value of this field for the given token.
     */
    public T access(Token token) {
        return accessor.apply(token);
    }

    /**
     * Returns the name of this token field.
     *
     * @return The name of the field.
     */
    public String name() {
        return name;
    }

    public void resolve(TokenBuilder builder, T value) {
        resolver.accept(builder, value);
    }

}
