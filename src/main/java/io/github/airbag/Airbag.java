package io.github.airbag;

import io.github.airbag.token.TokenField;
import io.github.airbag.token.TokenFormatter;
import io.github.airbag.token.TokenProvider;
import io.github.airbag.tree.TreeFormatter;
import io.github.airbag.tree.TreeProvider;
import io.github.airbag.tree.Validator;
import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.Tree;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * The Airbag class is the central component of the Airbag library.
 * It serves as a factory for creating {@link TokenProvider} and {@link TreeProvider} instances,
 * which are essential for creating symbol streams and parse trees from ANTLR grammars.
 * <p>
 * This class simplifies the process of working with ANTLR grammars by providing a unified interface
 * for creating and managing lexer and parser instances. It supports creating Airbag instances from
 * either the lexer and parser classes directly, or by providing the fully qualified name of the
 * grammar, in which case it will load the classes using the thread's context class loader.
 * <p>
 * The static factory methods are particularly useful for testing purposes, allowing for easy
 * creation of an Airbag instance for a given lexer, parser, or full grammar.
 */
public class Airbag {

    /**
     * The Token provider for creating symbols from string or specification.
     */
    private final TokenProvider symbolProvider;

    /**
     * The Tree provider for creating derivation trees.
     */
    private final TreeProvider treeProvider;

    /**
     * Constructs a new Airbag instance from the given parser and lexer classes.
     *
     * @param parserClass The {@link Class} object for the ANTLR parser.
     * @param lexerClass  The {@link Class} object for the ANTLR lexer.
     */
    private Airbag(Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) {
        symbolProvider = lexerClass == null ? null : new TokenProvider(lexerClass);
        treeProvider = parserClass == null ? null : new TreeProvider(parserClass);
    }


    /**
     * Creates a new Airbag instance for testing a lexer by its fully qualified name.
     * This method loads the lexer class using the thread's context class loader.
     *
     * @param fullyQualifiedName The fully qualified name of the lexer class (e.g., "com.example.MyLexer"). Must not be null or empty.
     * @return A new Airbag instance configured for testing the specified lexer.
     * @throws RuntimeException if the lexer class cannot be found.
     */
    public static Airbag testLexer(String fullyQualifiedName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return new Airbag(loader.loadClass(fullyQualifiedName).asSubclass(Lexer.class), null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Airbag instance for testing a lexer from its {@link Class} object.
     *
     * @param lexerClass The {@link Class} object for the ANTLR lexer.
     * @return A new Airbag instance configured for testing the specified lexer.
     */
    public static Airbag testLexer(Class<? extends Lexer> lexerClass) {
        return new Airbag(lexerClass, null);
    }

    /**
     * Creates a new Airbag instance for testing a parser by its fully qualified name.
     * This method loads the parser class using the thread's context class loader.
     *
     * @param fullyQualifiedName The fully qualified name of the parser class (e.g., "com.example.MyParser"). Must not be null or empty.
     * @return A new Airbag instance configured for testing the specified parser.
     * @throws RuntimeException if the parser class cannot be found.
     */
    public static Airbag testParser(String fullyQualifiedName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return new Airbag(null, loader.loadClass(fullyQualifiedName).asSubclass(Parser.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Airbag instance for testing a parser from its {@link Class} object.
     *
     * @param parserClass The {@link Class} object for the ANTLR parser.
     * @return A new Airbag instance configured for testing the specified parser.
     */
    public static Airbag testParser(Class<? extends Parser> parserClass) {
        return new Airbag(null, parserClass);
    }

    /**
     * Creates a new Airbag instance for testing a grammar by its fully qualified name.
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
            return new Airbag(lexerClass, parserClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Airbag instance for testing a grammar from its lexer and parser {@link Class} objects.
     *
     * @param lexerClass  The {@link Class} object for the ANTLR lexer.
     * @param parserClass The {@link Class} object for the ANTLR parser.
     * @return A new Airbag instance configured for testing the specified grammar.
     */
    public static Airbag testGrammar(Class<? extends Lexer> lexerClass, Class<? extends Parser> parserClass) {
        return new Airbag(lexerClass, parserClass);
    }

    /**
     * Asserts that the actual symbol matches the expected symbol, comparing only the specified fields.
     * If the symbols do not match, an {@link AssertionFailedError} is thrown with a detailed message.
     *
     * @param expected The expected symbol. Must not be null.
     * @param actual   The actual symbol to check against the expected symbol. Must not be null.
     * @param fields   A collection of {@link TokenField} instances to use for comparison.
     * @throws AssertionFailedError if the actual symbol does not match the expected symbol based on the specified fields.
     */
    public static void assertToken(Token expected,
                                   Token actual,
                                   Collection<TokenField<?>> fields) {
        assertToken(expected, actual, TokenFormatter.fromFields(fields));
    }

    /**
     * Asserts that the actual symbol matches the expected symbol, comparing only the specified fields.
     * This is a convenience method that takes an array of {@link TokenField} instances.
     * If the symbols do not match, an {@link AssertionFailedError} is thrown with a detailed message.
     *
     * @param expected The expected symbol. Must not be null.
     * @param actual   The actual symbol to check against the expected symbol. Must not be null.
     * @param fields   An array of {@link TokenField} instances to use for comparison.
     * @throws AssertionFailedError if the actual symbol does not match the expected symbol based on the specified fields.
     */
    public static void assertToken(Token expected, Token actual, TokenField<?>... fields) {
        assertToken(expected, actual, TokenFormatter.fromFields(Arrays.asList(fields)));
    }

    /**
     * Asserts that the actual symbol matches the expected symbol using the provided formatter for comparison.
     * If the symbols do not match, an {@link AssertionFailedError} is thrown with a detailed message.
     *
     * @param expected  The expected symbol. Must not be null.
     * @param actual    The actual symbol to check against the expected symbol. Must not be null.
     * @param formatter The {@link TokenFormatter} to use for formatting and comparing symbols.
     * @throws AssertionFailedError if the actual symbol does not match the expected symbol.
     */
    public static void assertToken(Token expected, Token actual, TokenFormatter formatter) {
        assertTokens(List.of(expected), List.of(actual), formatter);
    }

    /**
     * Asserts that the actual symbol matches the expected symbol.
     * If the symbols do not match, an {@link AssertionFailedError} is thrown with a detailed message comparing the two symbols.
     * The comparison is done using a "weak" equality check, which means that the token getType and getText must be the same, but other properties such as getLine and column numbers may differ.
     *
     * @param expected The expected symbol. Must not be null.
     * @param actual   The actual symbol to check against the expected symbol. Must not be null.
     * @throws AssertionFailedError if the actual symbol does not match the expected symbol.
     */
    public void assertToken(Token expected, Token actual) {
        assertTokens(List.of(expected), List.of(actual));
    }

    /**
     * Asserts that the actual input string, when tokenized, results in a list of symbols that matches the expected symbol specification.
     * If the lists do not match, an {@link AssertionFailedError} is thrown with a detailed message.
     * The comparison is done using a "weak" equality check, where only symbol fields captured by
     * the underlying formatter are compared.
     *
     * @param expected The expected symbol specification string. Must not be null.
     * @param actual   The actual input string to tokenize and compare. Must not be null.
     * @throws AssertionFailedError if the tokenized actual input does not match the expected symbols.
     */
    public void assertTokens(String expected, String actual) {
        Objects.requireNonNull(symbolProvider, "No symbol provider instantiated.");
        assertTokens(symbolProvider.expected(expected), symbolProvider.actual(actual));
    }

    /**
     * Asserts that the actual list of symbols matches the expected list using the formatter from the {@link TokenProvider}.
     * The comparison is done using a "weak" equality check, where only symbol fields captured by
     * the underlying formatter are compared.
     *
     * @param expected The expected list of symbols. Must not be null.
     * @param actual   The actual list of symbols to check against the expected list. Must not be null.
     * @throws AssertionFailedError if the actual list of symbols does not match the expected list.
     */
    public void assertTokens(List<? extends Token> expected, List<? extends Token> actual) {
        Objects.requireNonNull(symbolProvider, "No symbol provider instantiated.");
        TokenFormatter formatter = symbolProvider.getFormatter();
        assertTokens(expected, actual, formatter);
    }

    /**
     * Asserts that the actual list of symbols matches the expected list.
     * If the lists do not match, an {@link AssertionFailedError} is thrown with a detailed message comparing the two lists.
     * The comparison is done using a "weak" equality check, where only symbol fields captured by
     * the underlying formatter are compared.
     *
     * @param expected  The expected list of symbols. Must not be null.
     * @param actual    The actual list of symbols to check against the expected list. Must not be null.
     * @param formatter The formatter for formatting and comparing symbols.
     * @throws AssertionFailedError if the actual list of symbols does not match the expected list.
     */
    public static void assertTokens(List<? extends Token> expected,
                                    List<? extends Token> actual,
                                    TokenFormatter formatter) {

        BiPredicate<Token, Token> equalizer = TokenField.equalizer(formatter.getFields());
        if (!Utils.listEquals(expected, actual, equalizer)) {
            throw new SymbolAssertionFailedError(formatter, expected, actual);
        }
    }

    /**
     * Asserts that the actual derivation tree matches the expected derivation tree.
     * <p>
     * The comparison is performed by a {@link Validator}, which traverses both trees and compares the symbols at each node.
     * The symbols are compared using a "weak" equality check by default. The exact fields to be compared
     * can be configured on the {@link TokenFormatter} instance obtained from the {@link TreeProvider}.
     * <p>
     * If the trees do not match, an {@link AssertionFailedError} is thrown with a message indicating the mismatch,
     * containing the formatted expected and actual trees.
     *
     * @param expected The expected derivation tree. Must not be null.
     * @param actual   The actual derivation tree to check against the expected tree. Must not be null.
     * @throws AssertionFailedError if the actual derivation tree does not match the expected tree or if one of them is null.
     * @throws NullPointerException if the {@link TreeProvider} was not configured for this Airbag instance.
     */
    public void assertTree(Tree expected, Tree actual) {
        Objects.requireNonNull(treeProvider, "No tree provider instantiated");
        TreeFormatter treeFormatter = treeProvider.getFormatter();
        if (expected == null || actual == null) {
            String expectedString = expected == null ? null : treeFormatter.format(expected);
            String actualString = actual == null ? null : treeFormatter.format(actual);
            throw new AssertionFailedError("Cannot compare with null tree",
                    expectedString,
                    actualString);
        }

        TokenFormatter symbolFormatter = treeFormatter.getSymbolFormatter();
        Validator validator = new Validator(TokenField.equalizer(symbolFormatter.getFields()));
        if (!validator.validate(expected, actual)) {
            throw new AssertionFailedError("Derivation trees do not match",
                    treeFormatter.format(expected),
                    treeFormatter.format(actual));
        }
    }

    /**
     * Returns a {@link TokenProvider} for the configured lexer.
     * The {@link TokenProvider} can be used to create a list of symbols.
     *
     * @return The configured {@link TokenProvider} instance.
     */
    public TokenProvider getSymbolProvider() {
        return symbolProvider;
    }

    /**
     * Returns the {@link TreeProvider} for the configured parser.
     *
     * @return the {@link TreeProvider} for the configured parser.
     */
    public TreeProvider getTreeProvider() {
        return treeProvider;
    }
}