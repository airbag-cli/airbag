package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.CompositePrinterParser;
import io.github.airbag.token.TokenBuilder;
import io.github.airbag.token.TokenField;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Collections;
import java.util.Map;

/**
 * A context object for parsing a token.
 *
 * @param fieldMap A map of token fields to their values.
 */
record TokenParseContext(Map<TokenField<?>, Object> fieldMap, CompositePrinterParser printerParser, Vocabulary vocabulary) {

    @Override
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
        fieldMap.put(field, value);
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
     * @param <T> The type of the field.
     */
    private <T> void resolveFieldHelper(TokenBuilder builder ,TokenField<T> field, Object value) {
        //noinspection unchecked
        builder.resolve(field, (T) value);
    }

}