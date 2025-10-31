package io.github.airbag.tree;

import io.github.airbag.Airbag;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolProvider;
import io.github.airbag.tree.query.QueryLexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class QueryLexerTest {

    private Airbag airbag;
    private SymbolProvider provider;

    @BeforeEach
    void setup() {
        airbag = new Airbag(QueryLexer.class);
        provider = airbag.getSymbolProvider();
    }

    @Test
    void testQuery() {
        List<Symbol> expected = provider.fromSpec(
                "'/' (RULE 'expr') '/' (INDEX '10') '/' (RULE 'expr40') '//' '*' '/' EOF");
        List<Symbol> actual = provider.fromInput("/expr/10/expr40//*/");
        airbag.assertSymbolList(expected, actual);

    }

    @Test
    void testInvertedQuery() {
        List<Symbol> expected = provider.fromSpec(
                "'!' '/' (RULE 'expr') '!' '/' (STRING '\\'60\\'') '/' (TOKEN 'TOKEN') EOF");
        List<Symbol> actual = provider.fromInput("!/expr!/'60'/TOKEN");
        airbag.assertSymbolList(expected, actual);

    }

    @Test
    void testIntAsRule() {
        List<Symbol> expected = provider.fromSpec("'/' (RULE 'expr') '/' (INDEX '4') '/' EOF");
        List<Symbol> actual = provider.fromInput("/expr/4/");
        airbag.assertSymbolList(expected, actual);

    }

    @Test
    void testIntAsToken() {
        List<Symbol> expected = provider.fromSpec("'/' (RULE 'expr') '/' (TYPE '4') EOF");
        List<Symbol> actual = provider.fromInput("/expr/4");
        airbag.assertSymbolList(expected, actual);

    }
}