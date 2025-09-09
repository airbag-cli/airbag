package io.github.airbag;

import io.github.airbag.symbol.*;
import io.github.airbag.util.Utils;
import org.antlr.v4.runtime.*;
import org.opentest4j.AssertionFailedError;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The Airbag class is the central component of the Airbag library.
 * It serves as a factory for creating {@link SymbolProvider} and {@link TreeProvider} instances, which are essential for creating token streams and parse trees from ANTLR grammars.
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
     * The ANTLR recognizer, which can be either a lexer or a parser.
     * This is used to get the vocabulary and other information about the grammar.
     */
    private Recognizer<?, ?> recognizer;

    /**
     * The Token provider for creating tokens from string or specification.
     */
    private final SymbolProvider symbolProvider;

    /**
     * Constructs a new Airbag instance from the given parser and lexer classes.
     *
     * @param parserClass The {@link Class} object for the ANTLR parser. Must not be null.
     * @param lexerClass  The {@link Class} object for the ANTLR lexer. Must not be null.
     */
    public Airbag(Class<? extends Parser> parserClass, Class<? extends Lexer> lexerClass) {
        this.parserClass = parserClass;
        try {
            recognizer = parserClass.getConstructor(TokenStream.class).newInstance((TokenStream) null);
            symbolProvider = new SymbolProvider(lexerClass);
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
        try {
            recognizer = lexerClass.getConstructor(CharStream.class).newInstance((CharStream) null);
            symbolProvider = new SymbolProvider(lexerClass);
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
     * Returns a new {@link SymbolProvider} for the configured lexer.
     * The {@link SymbolProvider} can be used to create a list of tokens from a string.
     *
     * @return A new {@link SymbolProvider} instance.
     */
    public SymbolProvider getSymbolProvider() {
        return symbolProvider;
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
        SymbolFormatter formatter = symbolProvider.getSymbolFormatter();
        BiPredicate<Symbol, Symbol> equalizer = SymbolField.equalizer(formatter.getFields());
        if (!Utils.listEquals(expected, actual, equalizer)) {
            List<String> expectedLines = expected.stream().map(formatter::format).toList();
            List<String> actualLines = actual.stream().map(formatter::format).toList();
            throw new AssertionFailedError("Symbols lists are not equal", expectedLines, actualLines);
        }
    }

}
