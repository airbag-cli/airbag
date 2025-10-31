package io.github.airbag.tree.query;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;


public class QueryProvider {

    private final Parser parser;

    public QueryProvider(Parser parser) {
        this.parser = parser;
    }

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

    private static QueryElement.Navigator getNavigator(Token first) {
        return switch (first.getType()) {
            case QueryLexer.ROOT -> QueryElement.Navigator.ROOT;
            case QueryLexer.ANYWHERE -> QueryElement.Navigator.DESCENDANTS;
            default ->
                    throw new IllegalArgumentException("Unrecognized navigator %s".formatted(first.getText()));
        };
    }

}