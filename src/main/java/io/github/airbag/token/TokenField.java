package io.github.airbag.token;

import org.antlr.v4.runtime.Token;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a specific field or attribute of an ANTLR {@link Token}.
 * <p>
 * This class provides a type-safe enum-like pattern for defining and interacting with {@link Token}
 * properties. Each {@code TokenField} encapsulates:
 * <ul>
 *     <li>A {@code name} for identification.</li>
 *     <li>An {@code accessor} function to get the field's value from a {@link Token}.</li>
 *     <li>A {@code resolver} consumer to set the field's value on a {@link TokenBuilder}.</li>
 * </ul>
 * <p>
 * This design allows for flexible and type-safe manipulation of token data, especially when
 * building or transforming tokens. The class includes predefined static instances for all
 * standard {@link Token} fields.
 *
 * @param <T> The type of the value this field represents (e.g., {@link Integer} for line number,
 *            {@link String} for text).
 */
public final class TokenField<T> {

    /**
     * Represents the token type, an integer identifier for the token's category.
     *
     * @see Token#getType()
     */
    public static final TokenField<Integer> TYPE = new TokenField<>("type", Token::getType,
            TokenBuilder::type);

    /**
     * Represents the literal text matched for the token.
     *
     * @see Token#getText()
     */
    public static final TokenField<String> TEXT = new TokenField<>("text",
            Token::getText,
            TokenBuilder::text);

    /**
     * Represents the zero-based index of the token within the token stream.
     *
     * @see Token#getTokenIndex()
     */
    public static final TokenField<Integer> INDEX = new TokenField<>("index",
            Token::getTokenIndex,
            TokenBuilder::index);

    /**
     * Represents the line number where the token begins.
     *
     * @see Token#getLine()
     */
    public static final TokenField<Integer> LINE = new TokenField<>("line",
            Token::getLine,
            TokenBuilder::line);

    /**
     * Represents the character position within the line where the token begins.
     *
     * @see Token#getCharPositionInLine()
     */
    public static final TokenField<Integer> POSITION = new TokenField<>("position",
            Token::getCharPositionInLine, TokenBuilder::charPositionInLine);

    /**
     * Represents the channel to which the token belongs (e.g., default, hidden).
     *
     * @see Token#getChannel()
     */
    public static final TokenField<Integer> CHANNEL = new TokenField<>("channel",
            Token::getChannel, TokenBuilder::channel);

    /**
     * Represents the starting character index of the token in the input stream.
     *
     * @see Token#getStartIndex()
     */
    public static final TokenField<Integer> START = new TokenField<>("start",
            Token::getStartIndex,
            TokenBuilder::startIndex);

    /**
     * Represents the ending character index of the token in the input stream.
     *
     * @see Token#getStopIndex()
     */
    public static final TokenField<Integer> STOP = new TokenField<>("stop",
            Token::getStopIndex,
            TokenBuilder::stopIndex);

    /**
     * The name of the field, used for identification and debugging.
     */
    private final String name;

    /**
     * A function that extracts this field's value from a {@link Token}.
     */
    private final Function<? super Token, T> accessor;
    /**
     * A consumer that sets this field's value on a {@link TokenBuilder}.
     */
    private final BiConsumer<TokenBuilder, T> resolver;

    /**
     * Constructs a new {@code TokenField}.
     *
     * @param name     The name of the field. This is used for identification and debugging.
     * @param accessor A {@link Function} that, when applied to a {@link Token}, returns the value of this field.
     * @param resolver A {@link BiConsumer} that sets the value of this field on a {@link TokenBuilder}.
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

    /**
     * Sets the field's value on a {@link TokenBuilder}.
     * <p>
     * This method uses the field's resolver to apply the given value to the appropriate setter
     * on the {@link TokenBuilder}. It is a key part of the token construction process.
     *
     * @param builder The token builder to modify.
     * @param value   The value to set for this field.
     */
    public void resolve(TokenBuilder builder, T value) {
        resolver.accept(builder, value);
    }

}
