package io.github.airbag.token;

import io.github.airbag.format.TokenFormatter;
import io.github.airbag.gen.ValidationTreeBaseVisitor;
import io.github.airbag.gen.ValidationTreeLexer;
import io.github.airbag.gen.ValidationTreeParser;
import io.github.airbag.gen.ValidationTreeVisitor;
import org.antlr.v4.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.IntStream;

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

    /** The ANTLR lexer instance used for tokenizing input strings. */
    private final Lexer lexer;

    /** The formatter used to convert tokens back to strings. */
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
     * This method provides a powerful way to define a list of tokens using a concise
     * string-based format. The format is designed to be human-readable and corresponds
     * directly to the format produced by {@link TokenFormatter#SIMPLE}.
     *
     * <p><b>Specification Format</b></p>
     * The input string can contain a sequence of token specifications, which can be:
     * <ol>
     *   <li><b>A literal name:</b> A string enclosed in single quotes (e.g., {@code '='},
     *       {@code 'keyword'}). The provider looks up the corresponding token type in the
     *       lexer's vocabulary. The token's text is the content inside the quotes.</li>
     *   <li><b>A symbolic representation:</b> A parenthesized expression in the format {@code (TYPE 'text')}.
     *       {@code TYPE} is the symbolic name (e.g., {@code ID}), and {@code 'text'} is the token's
     *       exact text. This is used for tokens without a fixed literal name.</li>
     *   <li><b>The EOF token:</b> The special keyword {@code EOF} creates an end-of-file token.</li>
     * </ol>
     *
     * <p><b>Example</b></p>
     * <p>
     * To create tokens for the assignment "x = 5", you would use the following specification,
     * which is the same format produced by {@code TokenFormatter.SIMPLE}:
     * </p>
     * <pre>{@code
     * List<Token> tokens = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
     * }</pre>
     *
     * @param input The string containing the token specification.
     * @return A {@link List} of {@link Token} objects generated from the specification.
     * @see #format(Token)
     * @see TokenFormatter#SIMPLE
     */
    public List<Token> fromSpec(String input) {
        ValidationTreeLexer validationTreeLexer = new ValidationTreeLexer(CharStreams.fromString(input));
        ValidationTreeParser validationTreeParser = new ValidationTreeParser(new CommonTokenStream(validationTreeLexer));
        ValidationTreeVisitor<List<Token>> visitor = new ValidationTreeBaseVisitor<>() {

            @Override
            public List<Token> visitTokenList(ValidationTreeParser.TokenListContext ctx) {
                return IntStream.range(0, ctx.token().size()).mapToObj(i -> visitToken(ctx.token(i), i)).toList();
            }

            private Token visitToken(ValidationTreeParser.TokenContext tokenCtx, int i) {
                if (tokenCtx.EOF_KEYWORD() != null) {
                    var eof = new CommonToken(Token.EOF, "<EOF>");
                    eof.setTokenIndex(i);
                    return eof;
                }
                String typeString;
                String text;
                if (tokenCtx.TOKEN() == null) {
                    typeString = tokenCtx.STRING().getText();
                    text = unquote(typeString);
                } else {
                    typeString = tokenCtx.TOKEN().getText();
                    text = unquote(tokenCtx.STRING().getText());
                }
                var token = new CommonToken(Tokens.getTokenType(typeString, lexer.getVocabulary()),
                        text);
                token.setTokenIndex(i);
                return token;
            }

            private String unquote(String s) {
                if (s != null && s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
                    return s.substring(1, s.length() - 1);
                }
                return s;
            }
        };
        return visitor.visitTokenList(validationTreeParser.tokenList());
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

}
