package io.github.airbag.symbol;

import org.antlr.v4.runtime.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a simplified way to generate and format lists of ANTLR {@link Symbol} objects.
 * <p>
 * This utility class abstracts the boilerplate code required to set up an ANTLR lexer
 * and produce symbol lists from input strings. It also provides configurable utilities
 * for formatting symbols back into strings, making it a versatile tool for testing
 * and debugging components that work with symbols.
 *
 * @see org.antlr.v4.runtime.Lexer
 * @see Symbol
 * @see SymbolFormatter
 */
public class SymbolProvider {

    /**
     * The ANTLR lexer instance used for creating symbols from input strings.
     */
    private final Lexer lexer;

    /**
     * The formatter used to convert symbols back to strings.
     */
    private SymbolFormatter formatter;

    /**
     * Constructs a new SymbolProvider for a specific ANTLR lexer.
     * <p>
     * This constructor uses reflection to create an instance of the provided {@code lexerClass}.
     * It assumes that the lexer class has a public constructor that accepts a {@link CharStream}
     * as its sole argument, which is standard for ANTLR-generated lexers.
     * <p>
     * This also initializes a default symbol formatter ({@link SymbolFormatter#SIMPLE}) using the
     * vocabulary from the provided lexer. This default can be overridden using
     * {@link #setFormatter(SymbolFormatter)}.
     *
     * @param lexerClass The class of the ANTLR-generated lexer to be used for tokenization.
     *                   For example, {@code MyGrammarLexer.class}.
     * @throws IllegalArgumentException if the {@code lexerClass} cannot be instantiated. This
     *                                  can happen if the class does not have a public constructor
     *                                  accepting a {@link CharStream}, or if any other reflection-related
     *                                  error occurs during instantiation.
     */
    public SymbolProvider(Class<? extends Lexer> lexerClass) {
        try {// Instantiate the lexer. ANTLR lexers require a CharStream, but we can pass null
            // for initialization and set the actual stream later for each tokenization operation.
            lexer = lexerClass.getConstructor(CharStream.class).newInstance((CharStream) null);
            lexer.removeErrorListeners();
            formatter = SymbolFormatter.SIMPLE.withVocabulary(lexer.getVocabulary());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to instantiate the provided Lexer class. " +
                                               "Ensure it's a valid ANTLR-generated lexer with a public constructor accepting a CharStream.",
                    e);
        }
    }

    /**
     * Generates a list of {@link Symbol}s from a raw input string.
     * <p>
     * This method takes a string, sets it as the input for the configured lexer,
     * and consumes the entire input to produce a complete list of symbols.
     *
     * @param input The source string to be tokenized by the lexer. Cannot be null.
     * @return A {@link List} of {@link Symbol} objects generated from the input string.
     * The list will include the end-of-file (EOF) symbol.
     * @see org.antlr.v4.runtime.CharStreams#fromString(String)
     * @see org.antlr.v4.runtime.CommonTokenStream
     */
    public List<Symbol> fromInput(String input) {
        //Setting the input stream has the side effect of resetting the lexer as well
        lexer.setInputStream(CharStreams.fromString(input));
        var tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill(); // Eagerly process the entire input stream
        return tokenStream.getTokens().stream().map(Symbol::new).toList();
    }

    /**
     * Generates a list of {@link Symbol}s from a structured string specification.
     * <p>
     * This method iteratively parses a string containing one or more symbol specifications
     * using the provider's currently configured symbol formatter. By default, this is
     * {@link SymbolFormatter#SIMPLE}, so the input string should conform to its format.
     * The behavior of this method can be altered by providing a different formatter
     * via {@link #setFormatter(SymbolFormatter)}.
     * <p>
     * Whitespace between symbol specifications is ignored.
     *
     * <p><b>Specification Format (Default)</b></p>
     * The default format, defined by {@link SymbolFormatter#SIMPLE}, allows for:
     * <ol>
     *   <li><b>A literal name:</b> A string enclosed in single quotes (e.g., {@code '='},
     *       {@code 'keyword'}).</li>
     *   <li><b>A symbolic representation:</b> A parenthesized expression, e.g., {@code (ID 'text')}.</li>
     *   <li><b>The EOF symbol:</b> The special keyword {@code EOF}.</li>
     * </ol>
     *
     * <p><b>Example</b></p>
     * <pre>{@code
     * List<Symbol> symbols = symbolProvider.fromSpec("(ID 'x') '=' (INT '5') EOF");
     * }</pre>
     *
     * @param input The string containing the symbol specifications.
     * @return A {@link List} of {@link Symbol} objects generated from the specification.
     * @throws IllegalArgumentException if any part of the input string cannot be parsed.
     * @see #setFormatter(SymbolFormatter)
     * @see SymbolFormatter#SIMPLE
     */
    public List<Symbol> fromSpec(String input) {
        FormatterParsePosition position = new FormatterParsePosition(0);
        List<Symbol> symbols = new ArrayList<>();
        int index = 0;
        while (position.getIndex() < input.length()) {
            char c = input.charAt(position.getIndex());
            if (Character.isWhitespace(c)) {
                position.setIndex(position.getIndex() + 1);
                continue;
            }

            Symbol parsedSymbol = formatter.parse(input, position);

            if (parsedSymbol == null) {
                throw new SymbolParseException(input, position.getIndex(), position.getMessage());
            }

            // Safely create a new token to set the index if not set
            if (!formatter.getFields().contains(SymbolField.INDEX)) {
                Symbol indexedSymbol = new Symbol(index,
                        parsedSymbol.start(),
                        parsedSymbol.stop(),
                        parsedSymbol.text(),
                        parsedSymbol.type(),
                        parsedSymbol.channel(),
                        parsedSymbol.line(),
                        parsedSymbol.position());
                symbols.add(indexedSymbol);
                index++;
            } else {
                symbols.add(parsedSymbol);
            }
        }
        return symbols;
    }

    /**
     * Formats a given {@link Symbol} into a string using the currently configured formatter.
     * <p>
     * By default, this method uses the {@link SymbolFormatter#SIMPLE} formatter, which produces
     * a string representation that is compatible with the {@link #fromSpec(String)} method.
     * The formatter can be customized for different output styles using {@link #setFormatter(SymbolFormatter)}.
     *
     * @param symbol The symbol to be formatted.
     * @return A string representation of the symbol.
     * @see #fromSpec(String)
     * @see #setFormatter(SymbolFormatter)
     * @see SymbolFormatter#SIMPLE
     */
    public String format(Symbol symbol) {
        return formatter.format(symbol);
    }

    /**
     * Overrides the default symbol formatter for this provider.
     * <p>
     * The provided formatter will be automatically configured with this provider's
     * {@link Vocabulary}, ensuring that it can correctly resolve symbol type names.
     *
     * @param symbolFormatter The new formatter to use for the {@link #format(Symbol)} method.
     */
    public void setFormatter(SymbolFormatter symbolFormatter) {
        this.formatter = symbolFormatter.withVocabulary(lexer.getVocabulary());
    }

    /**
     * Gets the current {@link SymbolFormatter} instance used by this provider.
     * <p>
     * This is the formatter responsible for the behavior of the {@link #format(Symbol)}
     * and {@link #fromSpec(String)} methods.
     *
     * @return The current symbol formatter.
     * @see #setFormatter(SymbolFormatter)
     */
    public SymbolFormatter getFormatter() {
        return formatter;
    }

}