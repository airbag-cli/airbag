package io.github.airbag.symbol;

import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * A utility class for working with ANTLR {@link Token} objects.
 * <p>
 * This class provides helper methods for comparing tokens, which can be useful
 * in tests or other parts of the validation logic.
 */
public class Symbols {

    /**
     * Formats a {@link Symbol} into a string representation that is compatible with the
     * {@link SymbolProvider#fromSpec(String)} method.
     * <p>
     * This method generates a string that describes the symbol, making it easy to read and use
     * in test specifications. The formatting logic is as follows:
     * <ol>
     *   <li>If the symbol is an {@code EOF} symbol, it returns the string "EOF".</li>
     *   <li>If the symbol's type has a literal name in the provided {@link Vocabulary} (e.g., for a keyword
     *       like {@code 'while'}), it returns the literal name enclosed in single quotes (e.g., {@code "'while'"}).
     *       Note that the single quotes are part of the string.</li>
     *   <li>If the symbol's type has a symbolic name (e.g., {@code ID}), it returns a string in the
     *       format {@code "(SYMBOLIC_NAME 'text')"}, where {@code text} is the actual text of the symbol.
     *       For example, an identifier with text "myVar" would be formatted as {@code "(ID 'myVar')"}.</li>
     *   <li>If the symbol's type has neither a literal nor a symbolic name in the vocabulary, or if the
     *       vocabulary is {@code null}, it falls back to using the integer type of the symbol, producing a
     *       string like {@code "(42 'text')"}.</li>
     * </ol>
     * This formatting is particularly useful for creating readable representations of symbol streams,
     * for example, when debugging or generating test cases.
     *
     * @param symbol The symbol to format.
     * @param voc   The ANTLR {@link Vocabulary} to use for looking up symbol names. Can be {@code null}.
     * @return A string representation of the symbol, suitable for use with {@link SymbolProvider#fromSpec(String)}.
     */
    public static String format(Symbol symbol, Vocabulary voc) {
        if (symbol.type() == Token.EOF) {
            return "EOF";
        }
        int type = symbol.type();
        String text = Utils.escape(symbol.text());
        String typeString = String.valueOf(type);
        if (voc != null) {
            String literalName = voc.getLiteralName(type);
            if (literalName != null) {
                return "'%s'".formatted(text);
            }
            String symbolicName = voc.getSymbolicName(type);
            if (symbolicName != null) {
                return "(%s '%s')".formatted(symbolicName, text);
            }
        }
        return "(%s '%s')".formatted(typeString, text);
    }


}


