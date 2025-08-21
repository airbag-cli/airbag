package io.github.airbag;

import io.github.airbag.token.TokenProvider;
import io.github.airbag.token.Tokens;
import io.github.airbag.tree.TreeProvider;
import io.github.airbag.tree.Trees;
import io.github.airbag.tree.ValidationTree;
import io.github.airbag.tree.Validator;
import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The Airbag class is the central component of the Airbag library.
 * It serves as a factory for creating {@link TokenProvider} and {@link TreeProvider} instances, which are essential for creating token streams and parse trees from ANTLR grammars.
 * <p>
 * This class simplifies the process of working with ANTLR grammars by providing a unified interface for creating and managing lexer and parser instances.
 * It supports creating Airbag instances from either the lexer and parser classes directly, or by providing the fully qualified name of the grammar, in which case it will load the classes using the thread's context class loader.
 * <p>
 * The static factory methods {@link #testLexer(String)} and {@link #testGrammar(String)} are particularly useful for testing purposes.
 * They allow you to easily create an Airbag instance for a given lexer or grammar, which can then be used to create tokens and parse trees for your tests.
 */
public class Airbag {

    /**
     * The {@link Class} object for the ANTLR parser.
     * This is used to create new instances of the parser.
     */
    private Class<? extends Parser> parserClass;

    /**
     * The {@link Class} object for the ANTLR lexer.
     * This is used to create new instances of the lexer.
     */
    private Class<? extends Lexer> lexerClass;

    /**
     * The ANTLR recognizer, which can be either a lexer or a parser.
     * This is used to get the vocabulary and other information about the grammar.
     */
    private Recognizer<?, ?> recognizer;

    /**
     * Constructs a new Airbag instance from the given parser and lexer classes.
     *
     * @param parserClass The {@link Class} object for the ANTLR parser. Must not be null.
     * @param lexerClass  The {@link Class} object for the ANTLR lexer. Must not be null.
     */
    public Airbag(Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass) {
        this.parserClass = parserClass;
        this.lexerClass = lexerClass;
        try {
            recognizer = parserClass.getConstructor(TokenStream.class).newInstance((TokenStream) null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a new Airbag instance from the given lexer class.
     * This constructor is intended for use when you only need to test the lexer.
     *
     * @param lexerClass The {@link Class} object for the ANTLR lexer. Must not be null.
     */
    public Airbag(Class<? extends Lexer> lexerClass) {
        this.lexerClass = lexerClass;
        try {
            recognizer = lexerClass.getConstructor(CharStream.class).newInstance((CharStream) null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Airbag instance for testing a lexer.
     * This method loads the lexer class using the thread's context class loader.
     *
     * @param fullyQualifiedName The fully qualified name of the lexer class (e.g., "com.example.MyLexer"). Must not be null or empty.
     * @return A new Airbag instance configured for testing the specified lexer.
     * @throws RuntimeException if the lexer class cannot be found.
     */
    public static Airbag testLexer(String fullyQualifiedName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return new Airbag(loader.loadClass(fullyQualifiedName).asSubclass(Lexer.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Airbag instance for testing a grammar.
     * This method loads the lexer and parser classes using the thread's context class loader.
     * The lexer and parser class names are derived from the fully qualified grammar name by appending "Lexer" and "Parser" respectively.
     *
     * @param fullyQualifiedName The fully qualified name of the grammar (e.g., "com.example.MyGrammar"). Must not be null or empty.
     * @return A new Airbag instance configured for testing the specified grammar.
     * @throws RuntimeException if the lexer or parser class cannot be found.
     */
    public static Airbag testGrammar(String fullyQualifiedName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            var lexerClass = loader.loadClass(fullyQualifiedName + "Lexer").asSubclass(Lexer.class);
            var parserClass = loader.loadClass(fullyQualifiedName + "Parser").asSubclass(Parser.class);
            return new Airbag(parserClass, lexerClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a new {@link TokenProvider} for the configured lexer.
     * The {@link TokenProvider} can be used to create a list of tokens from a string.
     *
     * @return A new {@link TokenProvider} instance.
     */
    public TokenProvider getProvider() {
        return new TokenProvider(lexerClass);
    }

    /**
     * Returns a new {@link TreeProvider} for the configured parser.
     * The {@link TreeProvider} can be used to create a parse tree from a string.
     *
     * @return A new {@link TreeProvider} instance.
     */
    public TreeProvider getTreeProvider() {
        return new TreeProvider(parserClass);
    }

    /**
     * Asserts that the actual list of tokens matches the expected list.
     * If the lists do not match, an {@link AssertionFailedError} is thrown with a detailed message comparing the two lists.
     * The comparison is done using a "weak" equality check, which means that the token type and text must be the same, but other properties such as line and column numbers may differ.
     *
     * @param expected The expected list of tokens. Must not be null.
     * @param actual   The actual list of tokens to check against the expected list. Must not be null.
     * @throws AssertionFailedError if the actual list of tokens does not match the expected list.
     */
    public void assertTokenList(List<? extends Token> expected, List<? extends Token> actual) {
        if (!Utils.listEquals(expected, actual, Tokens::isWeakEqual)) {
            List<String> expectedLines = expected.stream().map(t -> Tokens.format(t, recognizer.getVocabulary())).toList();
            List<String> actualLines = actual.stream().map(t -> Tokens.format(t, recognizer.getVocabulary())).toList();
            throw new AssertionFailedError("Tokens lists are not equal", expectedLines, actualLines);
        }
    }

    /**
     * Asserts that the actual parse tree matches the expected validation tree.
     * If the trees do not match, an {@link AssertionFailedError} is thrown with a detailed message.
     * The validation tree provides a simplified way to check the structure of the parse tree without having to build a full parse tree manually.
     *
     * @param expected The validation tree to check against. Must not be null.
     * @param actual   The actual parse tree to check. Must not be null.
     * @throws AssertionFailedError if the actual parse tree does not match the validation tree.
     */
    public void assertParseTree(ValidationTree expected, ParseTree actual) {
        if (!Validator.matches(expected, actual)) {
            throw new AssertionFailedError("ParseTree does not match the validation tree", Trees.format(expected, recognizer), Trees.format(actual, recognizer));
        }
    }
}
