package io.github.airbag.symbol;


import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;


/**
 * A type-safe representation of a field within a {@link Symbol}.
 * <p>
 * This class provides a mechanism to abstract the fields of the {@link Symbol} record,
 * such as {@code type}, {@code text}, or {@code line}. Each {@code SymbolField} instance
 * encapsulates a specific field's name, its data type, a function to access its value
 * from a {@code Symbol}, and a consumer to set its value in a {@code Symbol.Builder}.
 * <p>
 * This abstraction is primarily used by the {@link SymbolFormatter} to dynamically
 * build formatters and parsers that can operate on different combinations of symbol fields.
 *
 * @param <T> The data type of the field (e.g., {@code Integer} for line number, {@code String} for text).
 */
public final class SymbolField<T> {


    /**
     * Represents the {@code type} field of a {@link Symbol}.
     * This field holds an integer identifying the kind of symbol (e.g., identifier, keyword, operator).
     *
     * @see Symbol#type()
     */
    public static final SymbolField<Integer> TYPE = new SymbolField<>("type", Symbol::type,
            Symbol.Builder::type);


    /**
     * Represents the {@code text} field of a {@link Symbol}.
     * This field holds the actual text matched for the symbol from the input source.
     *
     * @see Symbol#text()
     */
    public static final SymbolField<String> TEXT = new SymbolField<>("text",
            Symbol::text,
            Symbol.Builder::text);


    /**
     * Represents the {@code index} field of a {@link Symbol}.
     * This field holds the zero-based index of the symbol within the symbol stream.
     *
     * @see Symbol#index()
     */
    public static final SymbolField<Integer> INDEX = new SymbolField<>("index",
            Symbol::index,
            Symbol.Builder::index);


    /**
     * Represents the {@code line} field of a {@link Symbol}.
     * This field holds the line number where the symbol appears in the input source.
     *
     * @see Symbol#line()
     */
    public static final SymbolField<Integer> LINE = new SymbolField<>("line",
            Symbol::line,
            Symbol.Builder::line);


    /**
     * Represents the {@code position} field of a {@link Symbol}.
     * This field holds the character position of the symbol within its line.
     *
     * @see Symbol#position()
     */
    public static final SymbolField<Integer> POSITION = new SymbolField<>("position",
            Symbol::position, Symbol.Builder::position);


    /**
     * Represents the {@code channel} field of a {@link Symbol}.
     * This field is used to associate a symbol with a specific channel, such as a hidden channel for whitespace or comments.
     *
     * @see Symbol#channel()
     */
    public static final SymbolField<Integer> CHANNEL = new SymbolField<>("channel",
            Symbol::channel, Symbol.Builder::channel);

    /**
     * Represents the {@code start} field of a {@link Symbol}.
     * This field holds the starting character index of the symbol in the input source.
     *
     * @see Symbol#start()
     */
    public static final SymbolField<Integer> START = new SymbolField<>("start",
            Symbol::start,
            Symbol.Builder::start);


    /**
     * Represents the {@code stop} field of a {@link Symbol}.
     * This field holds the stopping character index of the symbol in the input source.
     *
     * @see Symbol#stop()
     */
    public static final SymbolField<Integer> STOP = new SymbolField<>("stop",
            Symbol::stop,
            Symbol.Builder::stop);

    /**
     * The name of the symbol field, e.g., "type", "text".
     */
    private final String name;

    /**
     * A function that extracts the field's value from a {@link Symbol}.
     */
    private final Function<? super Symbol, T> accessor;

    /**
     * A consumer that sets the field's value on a {@link Symbol.Builder}.
     */
    private final BiConsumer<Symbol.Builder, T> resolver;

    /**
     * Private constructor
     */
    private SymbolField(String name,
                        Function<? super Symbol, T> accessor,
                        BiConsumer<Symbol.Builder, T> resolver) {
        this.name = name;
        this.accessor = accessor;
        this.resolver = resolver;
    }

    /**
     * Extracts the value of this specific field from a given {@link Symbol} instance.
     *
     * @param symbol The symbol from which to retrieve the field's value. Must not be null.
     * @return The value of the field, cast to the field's type {@code T}.
     */
    public T access(Symbol symbol) {
        return accessor.apply(symbol);
    }

    /**
     * Gets the programmatic name of this field.
     *
     * @return The lower-case string name of the field (e.g., "type", "text").
     */
    public String name() {
        return name;
    }

    /**
     * Sets the value of this specific field on a {@link Symbol.Builder}.
     *
     * @param builder The builder instance on which to set the value. Must not be null.
     * @param value   The value to set for the field.
     */
    public void resolve(Symbol.Builder builder, T value) {
        resolver.accept(builder, value);
    }

    /**
     * Gets the default value for this field.
     * <p>
     * This value is used during parsing when a field is not explicitly provided in the
     * input string. The default values are defined as follows:
     * <ul>
     *     <li>{@code type}: {@link Symbol#INVALID_TYPE} (0)</li>
     *     <li>{@code text}: Empty string ({@code ""})</li>
     *     <li>{@code channel}: {@link Symbol#DEFAULT_CHANNEL} (0)</li>
     *     <li>{@code index}, {@code line}, {@code position}, {@code start}, {@code stop}: -1</li>
     * </ul>
     *
     * @return The default value for this field.
     * @throws RuntimeException if the field name is unknown.
     */
    @SuppressWarnings("unchecked")
    public T getDefault() {
        return switch (this.name) {
            case "type" -> (T) Integer.valueOf(Symbol.INVALID_TYPE);
            case "text" -> (T) "";
            case "channel" -> (T) Integer.valueOf(Symbol.DEFAULT_CHANNEL);
            case "index", "line", "position", "start", "stop" -> (T) Integer.valueOf(-1);
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
    public static BiPredicate<Symbol, Symbol> equalizer(Set<SymbolField<?>> fields) {
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
    public static Set<SymbolField<?>> all() {
        return Set.of(TYPE, TEXT, INDEX, LINE, POSITION, START, STOP, CHANNEL);
    }

    /**
     * Returns a predefined set of common {@code SymbolField} instances for basic symbol identification.
     * This set typically includes fields like {@code TYPE}, {@code TEXT}, {@code INDEX}, and {@code CHANNEL}.
     *
     * @return A set containing {@code TYPE}, {@code TEXT}, {@code INDEX}, and {@code CHANNEL} fields.
     */
    public static Set<SymbolField<?>> simple() {
        return Set.of(TYPE, TEXT, INDEX, CHANNEL);
    }

}