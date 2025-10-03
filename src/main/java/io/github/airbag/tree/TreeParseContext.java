package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;

public final class TreeParseContext {
    private Node<?> node;
    private final Recognizer<?, ?> recognizer;
    private final Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors;
    private final SymbolFormatter terminalFormatter;
    private final SymbolFormatter errorFormatter;

    public TreeParseContext(Recognizer<?, ?> recognizer,
                            Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors,
                            SymbolFormatter terminalFormatter,
                            SymbolFormatter errorFormatter) {
        this.recognizer = recognizer;
        this.connectors = connectors;
        this.terminalFormatter = terminalFormatter;
        this.errorFormatter = errorFormatter;
    }

    public Node<?> getNode() {
        return node;
    }

    public void setNode(Node<?> node) {
        this.node = node;
    }

    public Recognizer<?, ?> recognizer() {
        return recognizer;
    }

    public Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors() {
        return Collections.unmodifiableMap(connectors);
    }

    public SymbolFormatter terminalFormatter() {
        return terminalFormatter;
    }

    public SymbolFormatter errorFormatter() {
        return errorFormatter;
    }

}