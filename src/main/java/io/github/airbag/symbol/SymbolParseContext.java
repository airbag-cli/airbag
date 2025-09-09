package io.github.airbag.symbol;

import io.github.airbag.symbol.SymbolFormatterBuilder.CompositePrinterParser;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * A context object for parsing a symbol.
 *
 * @param fieldMap A map of symbol fields to their values.
 */
record SymbolParseContext(Map<SymbolField<?>, Object> fieldMap, CompositePrinterParser printerParser, Vocabulary vocabulary) {

    @Override
    public Map<SymbolField<?>, Object> fieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    /**
     * Adds a field to the context.
     *
     * @param field The field to add.
     * @param value The value of the field.
     */
    <T> void addField(SymbolField<T> field, T value) {
        Optional.ofNullable(fieldMap.put(field, value)).ifPresent(former -> {
            if (!former.equals(value)) {
                throw new SymbolParseException("Cannot set the field %s with a different value".formatted(field.name()));
            }
        });
    }

    /**
     * Resolves the fields in the context into a symbol.
     *
     * @return The resolved symbol.
     */
    Symbol resolveFields() {
        Symbol.Builder builder = new Symbol.Builder();
        for (var e : fieldMap.entrySet()) {
            resolveFieldHelper(builder, e.getKey(), e.getValue());
        }
        return builder.get();
    }

    /**
     * A helper method for resolving a field.
     *
     * @param builder The symbol builder to use.
     * @param field The field to resolve.
     * @param value The value of the field.
     * @param <T> The type of the field.
     */
    private <T> void resolveFieldHelper(Symbol.Builder builder , SymbolField<T> field, Object value) {
        //noinspection unchecked
        builder.resolve(field, (T) value);
    }

}