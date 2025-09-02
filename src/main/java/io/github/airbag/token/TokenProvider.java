package io.github.airbag.token;

import io.github.airbag.format.TokenFormatter;
import org.antlr.v4.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a simplified way to generate and format lists of ANTLR {@link Token} objects.
 * <p>
 * This utility class abstracts the boilerplate code required to set up an ANTLR lexer
 * and produce token lists from input strings. It also provides configurable utilities
 * for formatting tokens back into strings, making it a versatile tool for testing
 * and debugging components that work with tokens.
 *
 * @see org.antlr.v4.runtime.Lexer
 * @see org.antlr.v4.runtime.Token
 * @see TokenFormatter
 */
public class TokenProvider {

    /**
     * The ANTLR lexer instance used for tokenizing input strings.
     */
    private final Lexer lexer;

    /**
     * The formatter used to convert tokens back to strings.
     */
    private TokenFormatter formatter;

    /**
     * Constructs a new TokenProvider for a specific ANTLR lexer.
     * <p>
     * This constructor uses reflection to create an instance of the provided {@code lexerClass}.
     * It assumes that the lexer class has a public constructor that accepts a {@link CharStream}
     * as its sole argument, which is standard for ANTLR-generated lexers.
     * <p>
     * This also initializes a default token formatter ({@link TokenFormatter#SIMPLE}) using the
     * vocabulary from the provided lexer. This default can be overridden using
     * {@link #setTokenFormatter(TokenFormatter)}.
     *
     * @param lexerClass The class of the ANTLR-generated lexer to be used for tokenization.
     *                   For example, {@code MyGrammarLexer.class}.
     * @throws IllegalArgumentException if the {@code lexerClass} cannot be instantiated. This
     *                                  can happen if the class does not have a public constructor
     *                                  accepting a {@link CharStream}, or if any other reflection-related
     *                                  error occurs during instantiation.
     */
    public TokenProvider(Class<? extends Lexer> lexerClass) {
        try {// Instantiate the lexer. ANTLR lexers require a CharStream, but we can pass null
            // for initialization and set the actual stream later for each tokenization operation.
            lexer = lexerClass.getConstructor(CharStream.class).newInstance((CharStream) null);
            formatter = TokenFormatter.SIMPLE.withVocabulary(lexer.getVocabulary());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to instantiate the provided Lexer class. " +
                                               "Ensure it's a valid ANTLR-generated lexer with a public constructor accepting a CharStream.",
                    e);
        }
    }

    /**
     * Generates a list of {@link Token}s from a raw input string.
     * <p>
     * This method takes a string, sets it as the input for the configured lexer,
     * and consumes the entire input to produce a complete list of tokens.
     *
     * @param input The source string to be tokenized by the lexer. Cannot be null.
     * @return A {@link List} of {@link Token} objects generated from the input string.
     * The list will include the end-of-file (EOF) token.
     * @see org.antlr.v4.runtime.CharStreams#fromString(String)
     * @see org.antlr.v4.runtime.CommonTokenStream
     */
    public List<Token> fromInput(String input) {
        //Setting the input stream has the side effect of resetting the lexer as well
        lexer.setInputStream(CharStreams.fromString(input));
        var tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill(); // Eagerly process the entire input stream
        return tokenStream.getTokens();
    }

    /**
     * Generates a list of {@link Token}s from a structured string specification.
     * <p>
     * This method iteratively parses a string containing one or more token specifications
     * using the provider's currently configured token formatter. By default, this is
     * {@link TokenFormatter#SIMPLE}, so the input string should conform to its format.
     * The behavior of this method can be altered by providing a different formatter
     * via {@link #setTokenFormatter(TokenFormatter)}.
     * <p>
     * Whitespace between token specifications is ignored.
     *
     * <p><b>Specification Format (Default)</b></p>
     * The default format, defined by {@link TokenFormatter#SIMPLE}, allows for:
     * <ol>
     *   <li><b>A literal name:</b> A string enclosed in single quotes (e.g., {@code '='},
     *       {@code 'keyword'}).</li>
     *   <li><b>A symbolic representation:</b> A parenthesized expression, e.g., {@code (ID 'text')}.</li>
     *   <li><b>The EOF token:</b> The special keyword {@code EOF}.</li>
     * </ol>
     *
     * <p><b>Example</b></p>
     * <pre>{@code
     * List<Token> tokens = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
     * }</pre>
     *
     * @param input The string containing the token specifications.
     * @return A {@link List} of {@link Token} objects generated from the specification.
     * @throws IllegalArgumentException if any part of the input string cannot be parsed.
     * @see #setTokenFormatter(TokenFormatter)
     * @see TokenFormatter#SIMPLE
     */
    public List<Token> fromSpec(String input) {
        ParsePosition position = new ParsePosition(0);
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        while (position.getIndex() < input.length()) {
            char c = input.charAt(position.getIndex());
            if (Character.isWhitespace(c)) {
                position.setIndex(position.getIndex() + 1);
                continue;
            }

            Token parsedToken = formatter.parse(input, position);

            if (parsedToken == null) {
                throw new IllegalArgumentException("Cannot parse input spec '%s' at index %d".formatted(
                        input,
                        position.getErrorIndex()));
            }

            // Safely create a new token to set the index if not set
            if (!formatter.getFields().contains(TokenField.INDEX)) {
                CommonToken finalToken = new CommonToken(parsedToken);
                finalToken.setTokenIndex(index);
                tokens.add(finalToken);
                index++;
            } else {
                tokens.add(parsedToken);
            }
        }
        return tokens;
    }

    /**
     * Formats a given {@link Token} into a string using the currently configured formatter.
     * <p>
     * By default, this method uses the {@link TokenFormatter#SIMPLE} formatter, which produces
     * a string representation that is compatible with the {@link #fromSpec(String)} method.
     * The formatter can be customized for different output styles using {@link #setTokenFormatter(TokenFormatter)}.
     *
     * @param token The token to be formatted.
     * @return A string representation of the token.
     * @see #fromSpec(String)
     * @see #setTokenFormatter(TokenFormatter)
     * @see TokenFormatter#SIMPLE
     */
    public String format(Token token) {
        return formatter.format(token);
    }

    /**
     * Overrides the default token formatter for this provider.
     * <p>
     * The provided formatter will be automatically configured with this provider's
     * {@link Vocabulary}, ensuring that it can correctly resolve token type names.
     *
     * @param tokenFormatter The new formatter to use for the {@link #format(Token)} method.
     */
    public void setTokenFormatter(TokenFormatter tokenFormatter) {
        this.formatter = tokenFormatter.withVocabulary(lexer.getVocabulary());
    }

    /**
     * Gets the current {@link TokenFormatter} instance used by this provider.
     * <p>
     * This is the formatter responsible for the behavior of the {@link #format(Token)}
     * and {@link #fromSpec(String)} methods.
     *
     * @return The current token formatter.
     * @see #setTokenFormatter(TokenFormatter)
     */
    public TokenFormatter getTokenFormatter() {
        return formatter;
    }

}