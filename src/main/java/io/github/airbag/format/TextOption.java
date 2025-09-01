package io.github.airbag.format;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class TextOption {

    public static final TextOption ESCAPED = new TextOption();

    public static final TextOption NOTHING = new TextOption().withEscapeChar('\0')
            .withDefaultValue("")
            .withEscapeMap(Collections.emptyMap());

    private String defaultValue;
    private char escapeChar;
    private Map<Character, Character> escapeMap;
    private Map<Character, Character> unescapeMap;

    public TextOption() {
        withDefaultValue("<no text>");
        withEscapeMap(Map.ofEntries(
                entry('\n', 'n'),
                entry('\r', 'r'),
                entry('\t', 't'),
                entry('\\', '\\')
        ));
        escapeChar = '\\';
    }

    public TextOption withEscapeChar(char escape) {
        this.escapeChar = escape;
        return this;
    }

    public TextOption withDefaultValue(String defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue);
        return this;
    }

    public TextOption withEscapeMap(Map<Character, Character> escapeMap) {
        this.escapeMap = Map.copyOf(escapeMap);
        this.unescapeMap = this.escapeMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public Map<Character, Character> getEscapeMap() {
        return escapeMap;
    }

    public Map<Character, Character> getUnescapeMap() {
        return unescapeMap;
    }
}