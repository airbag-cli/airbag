package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolProvider;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeProvider;
import io.github.airbag.tree.Validator;
import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * The Airbag class is the central component of the Airbag library.
 * It serves as a factory for creating {@link SymbolProvider} and {@link io.github.airbag.tree.TreeProvider} instances, which are essential for creating token streams and parse trees from ANTLR grammars.
 * <p>
 * This class simplifies the process of working with ANTLR grammars by providing a unified interface for creating and managing lexer and parser instances.
 * It supports creating Airbag instances from either the lexer and parser classes directly, or by providing the fully qualified name of the grammar, in which case it will load the classes using the thread's context class loader.
 * <p>
 * The static factory methods {@link #testLexer(String)} and {@link #testGrammar(String)} are particularly useful for testing purposes.
 * They allow you to easily create an Airbag instance for a given lexer or grammar, which can then be used to create tokens and parse trees for your tests.
 */
public class Airbag {

    /**
     * The Token provider for creating tokens from string or specification.
     */
    private final SymbolProvider symbolProvider;

    /**
     * The Tree provider for creating CSTs and VSTs.
     */
    private final TreeProvider treeProvider;

    /**
     * Constructs a new Airbag instance from the given parser and lexer classes.
     *
     * @param parserClass The {@link Class} object for the ANTLR parser. Must not be null.
     * @param lexerClass  The {@link Class} object for the ANTLR lexer. Must not be null.
     */
    public Airbag(Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass) {
        symbolProvider = new SymbolProvider(lexerClass);
        treeProvider = new TreeProvider(parserClass);
    }

    /**
     * Constructs a new Airbag instance from the given lexer class.
     * This constructor is intended for use when you only need to test the lexer.
     *
     * @param lexerClass The {@link Class} object for the ANTLR lexer. Must not be null.
     */
    public Airbag(Class<? extends Lexer> lexerClass) {
        symbolProvider = new SymbolProvider(lexerClass);
        treeProvider = null;
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
            var parserClass = loader.loadClass(fullyQualifiedName + "Parser")
                    .asSubclass(Parser.class);
            return new Airbag(parserClass, lexerClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Asserts that the actual symbol matches the expected symbol.
     * If the symbols do not match, an {@link AssertionFailedError} is thrown with a detailed message comparing the two symbols.
     * The comparison is done using a "weak" equality check, which means that the token type and text must be the same, but other properties such as line and column numbers may differ.
     *
     * @param expected The expected symbol. Must not be null.
     * @param actual   The actual symbol to check against the expected symbol. Must not be null.
     * @throws AssertionFailedError if the actual symbol does not match the expected symbol.
     */
    public void assertSymbol(Symbol expected, Symbol actual) {
        assertSymbolList(List.of(expected), List.of(actual));
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
    public void assertSymbolList(List<Symbol> expected, List<Symbol> actual) {
        SymbolFormatter formatter = symbolProvider.getFormatter();
        BiPredicate<Symbol, Symbol> equalizer = SymbolField.equalizer(formatter.getFields());
        if (!Utils.listEquals(expected, actual, equalizer)) {
            List<String> expectedLines = expected.stream().map(formatter::format).toList();
            List<String> actualLines = actual.stream().map(formatter::format).toList();
            throw new AssertionFailedError("Symbols lists are not equal",
                    expectedLines,
                    actualLines);
        }
    }

    /**
     * Asserts that the actual derivation tree matches the expected derivation tree.
     * <p>
     * The comparison is performed by a {@link Validator}, which traverses both trees and compares the symbols at each node.
     * The symbols are compared using a "weak" equality check by default, which means that the token type and text must be the same, but other properties such as line and column numbers may differ.
     * The exact fields to be compared can be configured on the {@link SymbolFormatter} instance obtained from the {@link TreeProvider}.
     * <p>
     * If the trees do not match, an {@link AssertionFailedError} is thrown with a message indicating the mismatch.
     * The error will contain the expected and actual trees, which can be inspected to find the difference.
     *
     * @param expected The expected derivation tree. Must not be null.
     * @param actual   The actual derivation tree to check against the expected tree. Must not be null.
     * @throws AssertionFailedError if the actual derivation tree does not match the expected tree.
     * @throws NullPointerException if the {@link TreeProvider} was not configured for this Airbag instance.
     */
    public void assertTree(DerivationTree expected, DerivationTree actual) {
        SymbolFormatter formatter = Objects.requireNonNull(treeProvider)
                .getFormatter()
                .getSymbolFormatter();
        Validator validator = new Validator(SymbolField.equalizer(formatter.getFields()));
        if (!validator.validate(expected, actual)) {
            throw new AssertionFailedError("Derivation trees are not matching", expected, actual);
        }
    }

    /**
     * Returns a {@link SymbolProvider} for the configured lexer.
     * The {@link SymbolProvider} can be used to create a list of symbols.
     *
     * @return A new {@link SymbolProvider} instance.
     */
    public SymbolProvider getSymbolProvider() {
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