package io.github.airbag.token;

import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Provides a simplified way to generate and format lists of {@link Token} objects.
 * <p>
 * This utility class abstracts the boilerplate code required to set up an ANTLR lexer
 * and produce symbol lists from input strings. It also provides configurable utilities
 * for formatting symbols back into strings, making it a versatile tool for testing
 * and debugging components that work with symbols.
 *
 * @see org.antlr.v4.runtime.Lexer
 * @see Token
 * @see TokenFormatter
 */
public class TokenProvider {

    /**
     * The ANTLR lexer instance used for creating symbols from input strings.
     */
    private final Lexer lexer;

    /**
     * The formatter used to convert symbols back to strings.
     */
    private TokenFormatter formatter;

    /**
     * Constructs a new TokenProvider for a specific ANTLR lexer.
     * <p>
     * This constructor uses reflection to create an instance of the provided {@code lexerClass}.
     * It assumes that the lexer class has a public constructor that accepts a {@link CharStream}
     * as its sole argument, which is standard for ANTLR-generated lexers.
     * <p>
     * This also initializes a default symbol formatter ({@link TokenFormatter#SIMPLE}) using the
     * vocabulary from the provided lexer. This default can be overridden using
     * {@link #setFormatter(TokenFormatter)}.
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
            lexer.removeErrorListeners();
            lexer.addErrorListener(Utils.rethrowingErrorListener());
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
     * and consumes the entire input to produce a complete list of symbols.
     *
     * @param input The source string to be tokenized by the lexer. Cannot be {@code null}.
     * @return A {@link List} of {@link Token} objects generated from the input string.
     * The list will include the end-of-file (EOF) symbol.
     * @see org.antlr.v4.runtime.CharStreams#fromString(String)
     * @see org.antlr.v4.runtime.CommonTokenStream
     */
    public List<Token> actual(String input) {
        //Setting the input stream has the side effect of resetting the lexer as well
        try {
            lexer.setInputStream(CharStreams.fromString(input));
            var tokenStream = new CommonTokenStream(lexer);
            tokenStream.fill(); // Eagerly process the entire input stream
            return tokenStream.getTokens();
        } catch (TokenParseException e) {
            throw new TokenParseException(input, e.getLine(), e.getPosition(), e.getMessage());
        }
    }

    /**
     * Generates a list of {@link Token}s from a structured string specification.
     * <p>
     * This method iteratively parses a string containing one or more symbol specifications
     * using the provider's currently configured symbol formatter. By default, this is
     * {@link TokenFormatter#SIMPLE}, so the input string should conform to its format.
     * The behavior of this method can be altered by providing a different formatter
     * via {@link #setFormatter(TokenFormatter)}.
     * <p>
     * Whitespace between symbol specifications is ignored.
     *
     * @param input The string containing the symbol specifications.
     * @return A {@link List} of {@link Token} objects generated from the specification.
     * @throws IllegalArgumentException if any part of the input string cannot be parsed.
     * @see #setFormatter(TokenFormatter)
     * @see TokenFormatter#parseList(CharSequence)
     * @see TokenFormatter#SIMPLE
     */
    public List<Token> expected(String input) {
        return formatter.parseList(input);
    }

    /**
     * Formats a given {@link Token} into a string using the currently configured formatter.
     * <p>
     * By default, this method uses the {@link TokenFormatter#SIMPLE} formatter, which produces
     * a string representation that is compatible with the {@link #expected(String)} method.
     * The formatter can be customized for different output styles using {@link #setFormatter(TokenFormatter)}.
     *
     * @param symbol The symbol to be formatted.
     * @return A string representation of the symbol.
     * @see #expected(String)
     * @see #setFormatter(TokenFormatter)
     * @see TokenFormatter#SIMPLE
     */
    public String format(Token symbol) {
        return formatter.format(symbol);
    }

    /**
     * Overrides the default symbol formatter for this provider.
     * <p>
     * The provided formatter will be automatically configured with this provider's
     * {@link Vocabulary}, ensuring that it can correctly resolve symbol getType names.
     *
     * @param symbolFormatter The new formatter to use for the {@link #format(Token)} method.
     */
    public void setFormatter(TokenFormatter symbolFormatter) {
        this.formatter = symbolFormatter.withVocabulary(lexer.getVocabulary());
    }

    /**
     * Gets the current {@link TokenFormatter} instance used by this provider.
     * <p>
     * This is the formatter responsible for the behavior of the {@link #format(Token)}
     * and {@link #expected(String)} methods.
     *
     * @return The current symbol formatter.
     * @see #setFormatter(TokenFormatter)
     */
    public TokenFormatter getFormatter() {
        return formatter;
    }

}