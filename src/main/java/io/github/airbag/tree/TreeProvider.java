package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * A utility for creating {@link DerivationTree} instances from various sources, primarily for testing.
 * <p>
 * This class provides two main ways to construct a tree:
 * <ol>
 *   <li>{@link #fromInput(List, String)}: Simulates the ANTLR parsing process from a list of {@link Symbol} objects.</li>
 *   <li>{@link #fromSpec(String)}: Deserializes a tree from a string specification using a {@link TreeFormatter}.</li>
 * </ol>
 * It requires an ANTLR-generated {@link Parser} class for its operations, using reflection to invoke parser rules.
 * <p>
 * This class is immutable and thread-safe.
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Assume MyParser is an ANTLR-generated parser
 * TreeProvider treeProvider = new TreeProvider(MyParser.class);
 *
 * // 1. Create a tree by parsing a list of symbols starting from the "prog" rule
 * List<Symbol> symbols = List.of(...);
 * DerivationTree treeFromInput = treeProvider.fromInput(symbols, "prog");
 *
 * // 2. Create a tree from a string specification
 * TreeProvider specProvider = treeProvider.withFormatter(TreeFormatter.SIMPLE);
 * DerivationTree treeFromSpec = specProvider.fromSpec("(prog (stat ...))");
 * }</pre>
 *
 * @see DerivationTree
 * @see TreeFormatter
 */
public class TreeProvider {

    private final Parser parser;
    private TreeFormatter formatter;

    /**
     * Constructs a TreeProvider for a specific ANTLR {@link Parser} class.
     * <p>
     * This constructor instantiates the parser to gain access to its rule methods. For testing
     * convenience, the instantiated parser is configured with a {@link BailErrorStrategy} to
     * ensure parsing errors cause immediate failure rather than attempting recovery.
     *
     * @param parserClass The class of the ANTLR-generated parser (e.g., {@code MyParser.class}).
     * @throws IllegalArgumentException if the parser class cannot be instantiated. This typically
     *                                  happens if it is not a valid ANTLR parser with a public
     *                                  constructor that accepts a {@link TokenStream}.
     */
    public TreeProvider(Class<? extends Parser> parserClass) {
        this(instantiateParser(parserClass), TreeFormatter.SIMPLE);
    }

    private TreeProvider(Parser parser, TreeFormatter formatter) {
        this.parser = parser;
        this.formatter = Objects.requireNonNull(formatter).withRecognizer(parser);
    }

    private static Parser instantiateParser(Class<? extends Parser> parserClass) {
        try {
            Parser p = parserClass.getConstructor(TokenStream.class).newInstance((TokenStream) null);
            p.removeErrorListeners();
            return p;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to instantiate the provided Parser class. " +
                                               "Ensure it's a valid ANTLR-generated parser with a public constructor accepting a TokenStream.",
                    e);
        }
    }

    /**
     * Creates a new {@link TreeProvider} with the specified {@link TreeFormatter}.
     * <p>
     * This is required to use the {@link #fromSpec(String)} method.
     *
     * @param formatter The formatter to use for parsing string specifications.
     * @return A new {@link TreeProvider} instance configured with the formatter.
     */
    public TreeProvider withFormatter(TreeFormatter formatter) {
        return new TreeProvider(this.parser, formatter);
    }

    /**
     * Creates a {@link DerivationTree} by parsing a list of {@link Symbol} objects.
     * <p>
     * This method simulates a full ANTLR parse. It converts the symbols into a token stream,
     * feeds it to the parser, and invokes the specified parser rule by name using reflection.
     * The resulting {@link ParseTree} is then converted into a {@link DerivationTree}.
     *
     * @param symbolList A list of {@link Symbol} objects representing the token stream.
     * @param rule       The name of the parser rule to use as the entry point (e.g., "prog", "statement").
     * @return The resulting {@link DerivationTree}.
     * @throws RuntimeException if the specified rule method cannot be found or invoked, or if a
     *                          parse error occurs (due to the {@link BailErrorStrategy}).
     */
    public DerivationTree fromInput(List<Symbol> symbolList, String rule) {
        TokenSource tokenSource = new ListTokenSource(symbolList.stream()
                .map(Symbol::toToken)
                .toList());
        parser.setTokenStream(new CommonTokenStream(tokenSource));
        try {
            Method ruleMethod = parser.getClass().getMethod(rule);
            ParseTree parseTree = (ParseTree) ruleMethod.invoke(parser);
            return DerivationTree.from(parseTree);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link DerivationTree} by parsing a string specification.
     * <p>
     * This method uses a configured {@link TreeFormatter} to deserialize a string
     * (e.g., a LISP-style S-expression) into a {@link DerivationTree}.
     *
     * @param stringTree The string representation of the tree.
     * @return The parsed {@link DerivationTree}.
     * @throws IllegalStateException if a {@link TreeFormatter} has not been configured via
     *                               {@link #withFormatter(TreeFormatter)} beforehand.
     * @throws RuntimeException      if the string cannot be parsed by the configured formatter.
     */
    public DerivationTree fromSpec(String stringTree) {
        return formatter.parse(stringTree);
    }

    public TreeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(TreeFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter.withRecognizer(parser));
    }
}