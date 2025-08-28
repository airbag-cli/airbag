package io.github.airbag.format;

import io.github.airbag.token.TokenBuilder;
import io.github.airbag.token.TokenField;
import org.antlr.v4.runtime.Token;

import java.util.Collections;
import java.util.Map;

record TokenParseContext(Map<TokenField<?>, Object> fieldMap) {

    @Override
    public Map<TokenField<?>, Object> fieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    void addField(TokenField<?> field, Object value) {
        fieldMap.put(field, value);
    }

    Token resolveFields() {
        TokenBuilder builder = new TokenBuilder();
        for (var e : fieldMap.entrySet()) {
            resolveFieldHelper(builder, e.getKey(), e.getValue());
        }
        return builder.get();
    }

    private <T> void resolveFieldHelper(TokenBuilder builder ,TokenField<T> field, Object value) {
        //noinspection unchecked
        builder.resolve(field, (T) value);
    }

}
