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
                "'/' (ID 'expr') '/' (INT '10') '/' (ID 'expr40') '//' '*' '/' EOF");
        List<Symbol> actual = provider.fromInput("/expr/10/expr40//*/");
        airbag.assertSymbolList(expected, actual);
    }

    @Test
    void testInvertedQuery() {
        List<Symbol> expected = provider.fromSpec(
                "'!' '/' (ID 'expr') '/' (STRING '\\'60\\'') '/' EOF");
        List<Symbol> actual = provider.fromInput("!/expr/'60'/");
        airbag.assertSymbolList(expected, actual);
    }
}