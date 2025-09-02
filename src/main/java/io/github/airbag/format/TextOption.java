package io.github.airbag.format;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Map.entry;

/**
 * Configures how text is formatted and parsed, particularly in the context of escaping special characters.
 * <p>
 * This class allows for the specification of an escape character, a map of characters to their escaped representations,
 * and a default value for when the text is not present.
 */
public class TextOption {

    /**
     * A predefined {@link TextOption} that uses a backslash ({@code \}) as the escape character.
     * <p>
     * This option includes common escape sequences for:
     * <ul>
     *     <li>Newline ({@code \n})</li>
     *     <li>Carriage return ({@code \r})</li>
     *     <li>Tab ({@code \t})</li>
     * </ul>
     */
    public static final TextOption ESCAPED = new TextOption();

    /**
     * A predefined {@link TextOption} that performs no escaping and uses an empty string as the default value.
     * This is useful when the text should be treated as a raw, unescaped string.
     */
    public static final TextOption NOTHING = new TextOption().withEscapeChar('\0')
            .withDefaultValue("")
            .withEscapeMap(Collections.emptyMap());

    private String defaultValue;
    private char escapeChar;
    private Map<Character, Character> escapeMap;
    private Map<Character, Character> unescapeMap;

    /**
     * Creates a new {@link TextOption} with default settings.
     * <p>
     * The default settings are equivalent to {@link #ESCAPED}.
     */
    public TextOption() {
        withDefaultValue("<no text>");
        withEscapeMap(Map.ofEntries(
                entry('\n', 'n'),
                entry('\r', 'r'),
                entry('\t', 't')
        ));
        escapeChar = '\\';
    }

    /**
     * Sets the escape character to be used for formatting and parsing.
     *
     * @param escape the character to use for escaping.
     * @return this {@link TextOption} instance for chaining.
     */
    public TextOption withEscapeChar(char escape) {
        this.escapeChar = escape;
        return this;
    }

    /**
     * Sets the default value to be used when the text is not present.
     *
     * @param defaultValue the default value.
     * @return this {@link TextOption} instance for chaining.
     */
    public TextOption withDefaultValue(String defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue);
        return this;
    }

    /**
     * Sets the map of characters to their escaped representations.
     * <p>
     * The provided map is copied, and an unescape map is automatically generated from it.
     *
     * @param escapeMap a map where the key is the character to escape and the value is its escaped representation.
     * @return this {@link TextOption} instance for chaining.
     */
    public TextOption withEscapeMap(Map<Character, Character> escapeMap) {
        this.escapeMap = Map.copyOf(escapeMap);
        this.unescapeMap = this.escapeMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return this;
    }

    /**
     * @return the default value to be used when the text is not present.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return the escape character.
     */
    public char getEscapeChar() {
        return escapeChar;
    }

    /**
     * @return the map of characters to their escaped representations.
     */
    public Map<Character, Character> getEscapeMap() {
        return escapeMap;
    }

    /**
     * @return the map of escaped representations back to their original characters.
     */
    public Map<Character, Character> getUnescapeMap() {
        return unescapeMap;
    }
}
