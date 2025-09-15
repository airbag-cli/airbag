package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public record TreeParseContext(AtomicReference<Node<?>> nodeReference, Recognizer<?, ?> recognizer,
                               Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors, SymbolFormatter terminalFormatter, SymbolFormatter errorFormatter) {

    public Node<?> getNode() {
        return nodeReference.get();
    }

    public void setNode(Node<?> node) {
        nodeReference.set(node);
    }


}
