package io.github.airbag.token;


import org.antlr.v4.runtime.Token;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;


/**
 * A getType-safe representation of a field within a {@link Token}.
 * <p>
 * This class provides a mechanism to abstract the fields of the {@link Token} record,
 * such as {@code getType}, {@code getText}, or {@code getLine}. Each {@code TokenField} instance
 * encapsulates a specific field's name, its data getType, a function to access its value
 * from a {@code Token}, and a consumer to set its value in a {@code Token.Builder}.
 * <p>
 * This abstraction is primarily used by the {@link TokenFormatter} to dynamically
 * build formatters and parsers that can operate on different combinations of symbol fields.
 *
 * @param <T> The data getType of the field (e.g., {@code Integer} for getLine number, {@code String} for getText).
 */
public final class TokenField<T> {


    /**
     * Represents the {@code getType} field of a {@link Token}.
     * This field holds an integer identifying the kind of symbol (e.g., identifier, keyword, operator).
     *
     * @see Token#getType()
     */
    public static final TokenField<Integer> TYPE = new TokenField<>("type", Token::getType,
            TokenBuilder::type);


    /**
     * Represents the {@code getText} field of a {@link Token}.
     * This field holds the actual getText matched for the symbol from the input source.
     *
     * @see Token#getText()
     */
    public static final TokenField<String> TEXT = new TokenField<>("getText",
            Token::getText,
            TokenBuilder::text);


    /**
     * Represents the {@code getTokenIndex} field of a {@link Token}.
     * This field holds the zero-based getTokenIndex of the symbol within the symbol stream.
     *
     * @see Token#getTokenIndex()
     */
    public static final TokenField<Integer> INDEX = new TokenField<>("getTokenIndex",
            Token::getTokenIndex,
            TokenBuilder::index);


    /**
     * Represents the {@code getLine} field of a {@link Token}.
     * This field holds the getLine number where the symbol appears in the input source.
     *
     * @see Token#getLine()
     */
    public static final TokenField<Integer> LINE = new TokenField<>("getLine",
            Token::getLine,
            TokenBuilder::line);


    /**
     * Represents the {@code getCharPositionInLine} field of a {@link Token}.
     * This field holds the character getCharPositionInLine of the symbol within its getLine.
     *
     * @see Token#getCharPositionInLine()
     */
    public static final TokenField<Integer> POSITION = new TokenField<>("getCharPositionInLine",
            Token::getCharPositionInLine, TokenBuilder::position);


    /**
     * Represents the {@code getChannel} field of a {@link Token}.
     * This field is used to associate a symbol with a specific getChannel, such as a hidden getChannel for whitespace or comments.
     *
     * @see Token#getChannel()
     */
    public static final TokenField<Integer> CHANNEL = new TokenField<>("getChannel",
            Token::getChannel, TokenBuilder::channel);

    /**
     * Represents the {@code getStartIndex} field of a {@link Token}.
     * This field holds the starting character getTokenIndex of the symbol in the input source.
     *
     * @see Token#getStartIndex()
     */
    public static final TokenField<Integer> START = new TokenField<>("getStartIndex",
            Token::getStartIndex,
            TokenBuilder::start);


    /**
     * Represents the {@code getStopIndex} field of a {@link Token}.
     * This field holds the stopping character getTokenIndex of the symbol in the input source.
     *
     * @see Token#getStopIndex()
     */
    public static final TokenField<Integer> STOP = new TokenField<>("getStopIndex",
            Token::getStopIndex,
            TokenBuilder::stop);

    /**
     * The name of the symbol field, e.g., "getType", "getText".
     */
    private final String name;

    /**
     * A function that extracts the field's value from a {@link Token}.
     */
    private final Function<? super Token, T> accessor;

    /**
     * A consumer that sets the field's value on a {@link TokenBuilder}.
     */
    private final BiConsumer<TokenBuilder, T> resolver;

    /**
     * Private constructor
     */
    private TokenField(String name,
                       Function<? super Token, T> accessor,
                       BiConsumer<TokenBuilder, T> resolver) {
        this.name = name;
        this.accessor = accessor;
        this.resolver = resolver;
    }

    /**
     * Extracts the value of this specific field from a given {@link Token} instance.
     *
     * @param symbol The symbol from which to retrieve the field's value. Must not be null.
     * @return The value of the field, cast to the field's getType {@code T}.
     */
    public T access(Token symbol) {
        return accessor.apply(symbol);
    }

    /**
     * Gets the programmatic name of this field.
     *
     * @return The lower-case string name of the field (e.g., "getType", "getText").
     */
    public String name() {
        return name;
    }

    /**
     * Sets the value of this specific field on a {@link TokenBuilder}.
     *
     * @param builder The builder instance on which to set the value. Must not be null.
     * @param value   The value to set for the field.
     */
    public void resolve(TokenBuilder builder, T value) {
        resolver.accept(builder, value);
    }

    /**
     * Gets the default value for this field.
     * <p>
     * This value is used during parsing when a field is not explicitly provided in the
     * input string. The default values are defined as follows:
     * <ul>
     *     <li>{@code getType}: {@link Token#INVALID_TYPE} (0)</li>
     *     <li>{@code getText}: Empty string ({@code ""})</li>
     *     <li>{@code getChannel}: {@link Token#DEFAULT_CHANNEL} (0)</li>
     *     <li>{@code getTokenIndex}, {@code getLine}, {@code getCharPositionInLine}, {@code getStartIndex}, {@code getStopIndex}: -1</li>
     * </ul>
     *
     * @return The default value for this field.
     * @throws RuntimeException if the field name is unknown.
     */
    @SuppressWarnings("unchecked")
    public T getDefault() {
        return switch (this.name) {
            case "type" -> (T) Integer.valueOf(Token.INVALID_TYPE);
            case "getText" -> (T) "";
            case "getChannel" -> (T) Integer.valueOf(Token.DEFAULT_CHANNEL);
            case "getTokenIndex", "getLine", "getCharPositionInLine", "getStartIndex", "getStopIndex" -> (T) Integer.valueOf(-1);
            default -> throw new RuntimeException("Unknown field");
        };
    }

    /**
     * Returns a {@link BiPredicate} that can be used to compare two tokens for equality.
     * <p>
     * The predicate compares only the fields that are given.
     *
     * @param fields The fields to compare.
     * @return A predicate that can be used to compare two symbols for equality.
     */
    public static BiPredicate<Token, Token> equalizer(Set<TokenField<?>> fields) {
        return (t1, t2) -> {
            for (var field : fields) {
                if (!Objects.equals(field.access(t1), field.access(t2))) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Return a set of all fields.
     *
     * @return a set of all fields.
     */
    public static Set<TokenField<?>> all() {
        return Set.of(TYPE, TEXT, INDEX, LINE, POSITION, START, STOP, CHANNEL);
    }

    /**
     * Returns a predefined set of common {@code TokenField} instances for basic symbol identification.
     * This set typically includes fields like {@code TYPE}, {@code TEXT}, {@code INDEX}, and {@code CHANNEL}.
     *
     * @return A set containing {@code TYPE}, {@code TEXT}, {@code INDEX}, and {@code CHANNEL} fields.
     */
    public static Set<TokenField<?>> simple() {
        return Set.of(TYPE, TEXT, INDEX, CHANNEL);
    }

}