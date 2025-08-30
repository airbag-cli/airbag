package io.github.airbag.token;

import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * A utility class for working with ANTLR {@link Token} objects.
 * <p>
 * This class provides helper methods for comparing tokens, which can be useful
 * in tests or other parts of the validation logic.
 */
public class Tokens {

    /**
     * Performs a "strong" equality check between two tokens.
     * <p>
     * Strong equality means that most properties of the two tokens must be identical,
     * including their type, channel, text, line number, character position, and start/stop indices.
     * This is a comprehensive comparison for most practical purposes.
     * <p>
     * Note that this comparison intentionally ignores the {@link Token#getTokenSource()} and
     * {@link Token#getInputStream()} properties, as these often differ even for
     * otherwise identical tokens, especially when they are created in different parsing contexts.
     *
     * @param t1 The first token to compare.
     * @param t2 The second token to compare.
     * @return {@code true} if the tokens are strongly equal, {@code false} otherwise.
     */
    public static boolean isStrongEqual(Token t1, Token t2) {
        return isWeakEqual(t1, t2) &&
                t1.getLine() == t2.getLine() &&
                t1.getCharPositionInLine() == t2.getCharPositionInLine() &&
                t1.getStartIndex() == t2.getStartIndex() &&
                t1.getStopIndex() == t2.getStopIndex();
    }

    /**
     * Performs a "weak" equality check between two tokens.
     * <p>
     * Weak equality means that the core properties of the two tokens must be the same:
     * their type, channel, token index, and text. This comparison is less strict than
     * {@link #isStrongEqual(Token, Token)} because it ignores positional information
     * like line number and character position. This can be useful when comparing tokens
     * from different sources or when positional information is not relevant.
     *
     * @param t1 The first token to compare.
     * @param t2 The second token to compare.
     * @return {@code true} if the tokens are weakly equal, {@code false} otherwise.
     */
    public static boolean isWeakEqual(Token t1, Token t2) {
        if (t1 == t2) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        return t1.getType() == t2.getType() &&
                t1.getChannel() == t2.getChannel() &&
                t1.getTokenIndex() == t2.getTokenIndex() &&
                Objects.equals(t1.getText(), t2.getText());
    }

    /**
     * Resolves a token type from a string representation using a provided ANTLR {@link Vocabulary}.
     * <p>
     * This method provides a flexible way to determine the integer type of a token from various
     * string formats. It supports resolving token types by:
     * <ol>
     *   <li><b>Numeric ID:</b> If the string is a valid integer (e.g., "42"), it is directly parsed
     *       as the token type.</li>
     *   <li><b>Symbolic Name:</b> The method searches for a symbolic name in the vocabulary that
     *       matches the input string (e.g., "INT", "WHITESPACE"). This is the typical way to
     *       refer to tokens defined in an ANTLR grammar.</li>
     *   <li><b>Literal Name:</b> If no symbolic name matches, the method searches for a literal
     *       name that matches the input string (e.g., "'+'", "'while'"). Literal names are
     *       often used for keywords or operators.</li>
     *   <li><b>EOF Handling:</b> The special string "EOF" is explicitly handled and resolved to
     *       {@link Token#EOF}, which is a standard convention in ANTLR.</li>
     * </ol>
     * This utility is particularly useful for creating tools or tests that need to work with
     * token types in a more readable, string-based format rather than hard-coded integer constants.
     *
     * @param type The string representation of the token type. This can be a numeric ID,
     *             a symbolic name, a literal name, or the special "EOF" string.
     * @param voc  The ANTLR {@link Vocabulary} from the lexer, which contains the mappings
     *             between integer types and their symbolic or literal names.
     * @return The integer constant corresponding to the resolved token type.
     * @throws RuntimeException if the provided {@code type} string cannot be resolved to a
     *                          valid token type in the given vocabulary.
     * @see org.antlr.v4.runtime.Vocabulary
     * @see org.antlr.v4.runtime.Token
     */
    public static int getTokenType(String type, Vocabulary voc) {
        if (type.matches("-?\\d+")) {
            return Integer.parseInt(type);
        }
        if (type.equals("EOF")) {
            return Token.EOF;
        }
        for (int i = 0; i < voc.getMaxTokenType() + 1; i++) {
            if (Objects.equals(voc.getSymbolicName(i), type)) {
                return i;
            }
            if (Objects.equals(voc.getLiteralName(i), type)) {
                return i;
            }
        }
        throw new RuntimeException("Type \"%s\" not found".formatted(type));
    }

    /**
     * Creates a new {@link TokenBuilder} for creating a single token.
     *
     * @return A new token builder.
     */
    public static TokenBuilder singleTokenOf() {
        return new TokenBuilder();
    }

    /**
     * Formats a {@link Token} into a string representation that is compatible with the
     * {@link TokenProvider#fromSpec(String)} method.
     * <p>
     * This method generates a string that describes the token, making it easy to read and use
     * in test specifications. The formatting logic is as follows:
     * <ol>
     *   <li>If the token is an {@code EOF} token, it returns the string "EOF".</li>
     *   <li>If the token's type has a literal name in the provided {@link Vocabulary} (e.g., for a keyword
     *       like {@code 'while'}), it returns the literal name enclosed in single quotes (e.g., {@code "'while'"}).
     *       Note that the single quotes are part of the string.</li>
     *   <li>If the token's type has a symbolic name (e.g., {@code ID}), it returns a string in the
     *       format {@code "(SYMBOLIC_NAME 'text')"}, where {@code text} is the actual text of the token.
     *       For example, an identifier with text "myVar" would be formatted as {@code "(ID 'myVar')"}.</li>
     *   <li>If the token's type has neither a literal nor a symbolic name in the vocabulary, or if the
     *       vocabulary is {@code null}, it falls back to using the integer type of the token, producing a
     *       string like {@code "(42 'text')"}.</li>
     * </ol>
     * This formatting is particularly useful for creating readable representations of token streams,
     * for example, when debugging or generating test cases.
     *
     * @param token The token to format.
     * @param voc   The ANTLR {@link Vocabulary} to use for looking up token names. Can be {@code null}.
     * @return A string representation of the token, suitable for use with {@link TokenProvider#fromSpec(String)}.
     */
    public static String format(Token token, Vocabulary voc) {
        if (token.getType() == Token.EOF) {
            return "EOF";
        }
        int type = token.getType();
        String text = Utils.escape(token.getText());
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

    /**
     * Returns a {@link BiPredicate} that can be used to compare two tokens for equality.
     * <p>
     * The predicate compares only the fields that are given.
     *
     * @param fields The fields to compare.
     * @return A predicate that can be used to compare two tokens for equality.
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
}
