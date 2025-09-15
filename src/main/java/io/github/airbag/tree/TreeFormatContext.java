package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.concurrent.atomic.AtomicReference;

public record TreeFormatContext(AtomicReference<Node<?>> nodeReference, Recognizer<?,?> recognizer, SymbolFormatter terminalFormatter, SymbolFormatter errorFormatter) {

    public Node<?> getNode() {
        return nodeReference.get();
    }

    public void setNode(Node<?> node) {
        nodeReference.set(node);
    }

}
