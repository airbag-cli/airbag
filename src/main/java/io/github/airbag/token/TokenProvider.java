package io.github.airbag.token;

import io.github.airbag.gen.ValidationTreeBaseVisitor;
import io.github.airbag.gen.ValidationTreeLexer;
import io.github.airbag.gen.ValidationTreeParser;
import io.github.airbag.gen.ValidationTreeVisitor;
import org.antlr.v4.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Provides a simplified way to generate lists of ANTLR {@link Token} objects from various sources.
 * <p>
 * This utility class abstracts the boilerplate code required to set up an ANTLR lexer,
 * including the creation of {@link CharStream} and {@link CommonTokenStream}. It is designed
 * to be initialized with a specific ANTLR-generated {@link Lexer} class and then used to
 * produce token lists from input strings. This is particularly useful for testing parser rules
 * or other components that operate on a stream of tokens.
 *
 * @see org.antlr.v4.runtime.Lexer
 * @see org.antlr.v4.runtime.Token
 */
public class TokenProvider {

    /**
     * The ANTLR lexer instance used for tokenizing input strings.
     * This field is initialized in the constructor with a specific lexer implementation.
     */
    private final Lexer lexer;

    /**
     * Constructs a new TokenProvider for a specific ANTLR lexer.
     * <p>
     * This constructor uses reflection to create an instance of the provided {@code lexerClass}.
     * It assumes that the lexer class has a public constructor that accepts a {@link CharStream}
     * as its sole argument, which is standard for ANTLR-generated lexers.
     *
     * @param lexerClass The class of the ANTLR-generated lexer to be used for tokenization.
     *                   For example, {@code MyGrammarLexer.class}.
     * @throws IllegalArgumentException if the {@code lexerClass} cannot be instantiated. This
     *                                  can happen if the class does not have a public constructor
     *                                  accepting a {@link CharStream}, or if any other reflection-related
     *                                  error occurs during instantiation (e.g., {@link InstantiationException},
     *                                  {@link IllegalAccessException}, {@link InvocationTargetException},
     *                                  {@link NoSuchMethodException}).
     */
    public TokenProvider(Class<? extends Lexer> lexerClass) {
        try {// Instantiate the lexer. ANTLR lexers require a CharStream, but we can pass null
            // for initialization and set the actual stream later for each tokenization operation.
            lexer = lexerClass.getConstructor(CharStream.class).newInstance((CharStream) null);
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
     * string-based format, which is formally defined by the {@code tokenList} rule in the
     * {@code ValidationTree.g4} grammar. It is designed for creating precise token sequences,
     * making it ideal for setting up detailed test cases for parsers or other token-consuming components.
     *
     * <p><b>Specification Grammar</b></p>
     * The input string can contain a sequence of token specifications. According to the {@code ValidationTree.g4}
     * grammar, a token can be specified in three distinct ways:
     * <ol>
     *   <li><b>As a simple literal:</b> A string enclosed in single quotes (e.g., {@code '='},
     *       {@code 'keyword'}). The method will look up the corresponding token type in the
     *       provided lexer's vocabulary. The text of the token will be the text inside the quotes.
     *       For example, {@code '='} creates a token with text "=" and the type corresponding to the '=' literal.</li>
     *   <li><b>As a compound token:</b> A parenthesized expression in the format {@code (TYPE 'text')}.
     *       <ul>
     *         <li>{@code TYPE} is the symbolic name of the token (e.g., {@code ID}, {@code INT}).</li>
     *         <li>{@code 'text'} is the exact text for the token, enclosed in single quotes.</li>
     *       </ul>
     *       This allows creating tokens with specific values, such as an identifier with the name "x"
     *       or an integer with the value "5". For example, {@code (ID 'x')} creates a token with type {@code ID}
     *       and text "x".</li>
     *   <li><b>As the EOF token:</b> The special keyword {@code EOF} creates an end-of-file token.
     *       This is useful for testing parser rules that expect an EOF marker.</li>
     * </ol>
     *
     * <p><b>Example</b></p>
     * <p>
     * To create a sequence of three tokens representing the assignment "x = 5" followed by EOF, you would use the
     * following specification:
     * </p>
     * <pre>{@code
     * List<Token> tokens = tokenProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
     * }</pre>
     * This will produce a list containing:
     * <ol>
     *   <li>A token with type {@code ID} and text "x".</li>
     *   <li>A token with the type corresponding to the literal "=" and text "=".</li>
     *   <li>A token with type {@code INT} and text "5".</li>
     *   <li>An EOF token.</li>
     * </ol>
     * The token types for {@code ID}, {@code INT}, and {@code '='} are all resolved using the
     * vocabulary of the lexer that was provided to this {@code TokenProvider}'s constructor.
     *
     * @param input The string containing the token specification, conforming to the {@code tokenList}
     *              rule in {@code ValidationTree.g4}.
     * @return A {@link List} of {@link Token} objects generated from the specification.
     * @see #TokenProvider(Class)
     * @see Tokens#getTokenType(String, Vocabulary)
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
     * Formats a given {@link Token} into a human-readable string representation using the lexer's vocabulary.
     * <p>
     * This method is a convenience wrapper around {@link Tokens#format(Token, Vocabulary)}. It automatically
     * uses the vocabulary of the lexer associated with this {@code TokenProvider} instance to generate
     * a string that is compatible with the {@link #fromSpec(String)} method. This is useful for
     * debugging or creating string-based representations of tokens.
     *
     * @param token The token to be formatted.
     * @return A string representation of the token.
     * @see Tokens#format(Token, Vocabulary)
     */
    public String format(Token token) {
        return Tokens.format(token, lexer.getVocabulary());
    }


}
