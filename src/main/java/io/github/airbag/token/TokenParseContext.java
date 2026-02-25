package io.github.airbag.token;

import io.github.airbag.token.TokenFormatterBuilder.CompositePrinterParser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A context object for parsing a token.
 */
class TokenParseContext {

    private final Map<TokenField<?>, Object> fieldMap;
    private final CompositePrinterParser printerParser;
    private final Vocabulary vocabulary;
    private String errorMessage;

    public TokenParseContext(CompositePrinterParser printerParser, Vocabulary vocabulary) {
        this.printerParser = printerParser;
        this.vocabulary = vocabulary;
        fieldMap = new HashMap<>();
    }

    public CompositePrinterParser printerParser() {
        return printerParser;
    }

    public Vocabulary vocabulary() {
        return vocabulary;
    }

    public Map<TokenField<?>, Object> fieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    /**
     * Adds a field to the context.
     *
     * @param field The field to add.
     * @param value The value of the field.
     */
    <T> void addField(TokenField<T> field, T value) {
        Optional.ofNullable(fieldMap.put(field, value)).ifPresent(former -> {
            if (!former.equals(value)) {
                throw new TokenParseException("Cannot set the field '%s' with a different value".formatted(field.name()));
            }
        });
    }

    /**
     * Resolves the fields in the context into a token.
     *
     * @return The resolved token.
     */
    Token resolveFields() {
        TokenBuilder builder = new TokenBuilder();
        for (var e : fieldMap.entrySet()) {
            resolveFieldHelper(builder, e.getKey(), e.getValue());
        }
        return builder.get();
    }

    /**
     * A helper method for resolving a field.
     *
     * @param builder The token builder to use.
     * @param field The field to resolve.
     * @param value The value of the field.
     * @param <T> The getType of the field.
     */
    private <T> void resolveFieldHelper(TokenBuilder builder , TokenField<T> field, Object value) {
        //noinspection unchecked
        builder.resolve(field, (T) value);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}