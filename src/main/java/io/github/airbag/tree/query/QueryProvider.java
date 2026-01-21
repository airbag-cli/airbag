package io.github.airbag.tree.query;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality to compile string-based query expressions into executable {@link Query} objects.
 * This class uses an ANTLR-generated lexer to tokenize the query string and then constructs
 * a sequence of {@link QueryElement}s that represent the query's logic.
 * <p>
 * The query language supports path-like expressions for navigating a {@link io.github.airbag.tree.DerivationTree},
 * allowing for selection of nodes based on rule names, token types, wildcards, and navigators
 * (e.g., child, descendant).
 *
 * @see Query
 * @see QueryElement
 * @see QueryLexer
 */
public class QueryProvider {

    private final Parser parser;

    /**
     * Constructs a new {@code QueryProvider} instance.
     *
     * @param parser An ANTLR {@link Parser} instance. This is used by the provider to
     *               resolve rule and token names to their corresponding integer indices,
     *               which are essential for creating {@link QueryElement}s that match
     *               specific grammar rules or tokens.
     */
    public QueryProvider(Parser parser) {
        this.parser = parser;
    }

    /**
     * Compiles a string-based query expression into an executable {@link Query} object.
     * The input {@code path} string is tokenized using the {@link QueryLexer}, and
     * these tokens are then translated into a sequence of {@link QueryElement}s that
     * define the tree traversal and matching logic.
     *
     * <p>The query language supports:
     * <ul>
     *     <li>{@code /} (root or child navigator)</li>
     *     <li>{@code //} (anywhere or descendant navigator)</li>
     *     <li>{@code ! } (inversion operator)</li>
     *     <li>{@code *} (wildcard for any node)</li>
     *     <li>Rule names (e.g., {@code expression})</li>
     *     <li>Token names (e.g., {@code ID})</li>
     *     <li>Integer rule/token indices (e.g., {@code 123})</li>
     * </ul>
     *
     * @param path The string representation of the query expression.
     * @return A compiled {@link Query} object that can be evaluated against a {@link io.github.airbag.tree.DerivationTree}.
     * @throws IllegalArgumentException If the query string contains unrecognized navigators or filters.
     */
    public Query compile(String path) {
        QueryLexer lexer = new QueryLexer(CharStreams.fromString(path));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> tokens = tokenStream.getTokens();
        boolean isInverted = false;
        List<QueryElement> elements = new ArrayList<>();
        for (int i = 0; i < tokens.size() - 1; i += 2) {
            Token current = tokens.get(i);
            Token next = tokens.get(i + 1);
            if (current.getType() == Token.EOF || next.getType() == Token.EOF) {
                break;
            }
            if (current.getType() == QueryLexer.BANG) {
                isInverted = !isInverted;
                continue;
            }
            QueryElement.Navigator navigator = switch (current.getType()) {
                case QueryLexer.ROOT -> elements.isEmpty() ?
                        QueryElement.Navigator.ROOT :
                        QueryElement.Navigator.CHILDREN;
                case QueryLexer.ANYWHERE -> elements.isEmpty() ?
                        QueryElement.Navigator.ALL :
                        QueryElement.Navigator.DESCENDANTS;
                default -> throw new IllegalArgumentException("Unrecognized navigator %s".formatted(
                        current.getText()));
            };
            elements.add(createElement(navigator, next, isInverted));
        }
        return new Query(elements.toArray(new QueryElement[0]));
    }

    /**
     * Creates a {@link QueryElement} based on the provided navigator, filter token, and inversion status.
     * This private helper method translates the parsed tokens into the appropriate {@link QueryElement}
     * implementation (e.g., Wildcard, Token, Rule).
     *
     * @param navigator  The {@link QueryElement.Navigator} indicating how to traverse the tree.
     * @param filter     The ANTLR {@link Token} representing the filter (e.g., rule name, token type, wildcard).
     * @param isInverted A boolean indicating if this element's match should be inverted.
     * @return A new {@link QueryElement} instance corresponding to the given parameters.
     * @throws IllegalArgumentException If the filter token type is not recognized.
     */
    private QueryElement createElement(QueryElement.Navigator navigator,
                                       Token filter,
                                       boolean isInverted) {
        return switch (filter.getType()) {
            case QueryLexer.WILDCARD -> new QueryElement.Wildcard(navigator, isInverted);
            case QueryLexer.STRING, QueryLexer.TOKEN ->
                    new QueryElement.Token(navigator, isInverted,
                            parser.getTokenType(filter.getText()));
            case QueryLexer.INDEX -> new QueryElement.Rule(navigator,
                    isInverted,
                    Integer.parseInt(filter.getText()));
            case QueryLexer.TYPE -> new QueryElement.Token(navigator, isInverted, Integer.parseInt(
                    filter.getText()));
            case QueryLexer.RULE ->
                    new QueryElement.Rule(navigator, isInverted, parser.getRuleIndex(
                            filter.getText()));
            default ->
                    throw new IllegalArgumentException("Unrecognized filter %s".formatted(filter.getText()));
        };
    }

    /**
     * Determines the {@link QueryElement.Navigator} based on the first token encountered.
     * This helper is used to interpret the initial navigation operator (e.g., {@code /} or {@code //}).
     *
     * @param first The first ANTLR {@link Token} of a query segment.
     * @return The corresponding {@link QueryElement.Navigator}.
     * @throws IllegalArgumentException If the token type does not represent a recognized navigator.
     * @deprecated This method appears to be unused and its logic is integrated directly into {@link #compile(String)}.
     *             Consider removing it if it's indeed redundant.
     */
    private static QueryElement.Navigator getNavigator(Token first) {
        return switch (first.getType()) {
            case QueryLexer.ROOT -> QueryElement.Navigator.ROOT;
            case QueryLexer.ANYWHERE -> QueryElement.Navigator.DESCENDANTS;
            default ->
                    throw new IllegalArgumentException("Unrecognized navigator %s".formatted(first.getText()));
        };
    }

}