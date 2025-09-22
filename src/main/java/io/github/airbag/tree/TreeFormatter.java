package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolException;
import io.github.airbag.symbol.SymbolFormatter;
import org.antlr.v4.runtime.Recognizer;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static java.util.Map.entry;

public class TreeFormatter {

    public static final TreeFormatter SIMPLE = new TreeFormatterBuilder().appendLiteral("(")
            .appendRule()
            .appendLiteral(" ")
            .appendChildren(" ")
            .appendLiteral(")")
            .toFormatter();

    private final TreeFormatterBuilder.NodePrinterParser printerParser;
    private final Recognizer<?, ?> recognizer;
    private final SymbolFormatter terminalFormatter;
    private final SymbolFormatter errorFormatter;

    TreeFormatter(TreeFormatterBuilder.NodePrinterParser printerParser) {
        this.printerParser = printerParser;
        this.recognizer = null;
        this.terminalFormatter = SymbolFormatter.SIMPLE;
        this.errorFormatter = SymbolFormatter.ofPattern("(<%error%> (S 'X'))");
    }

    TreeFormatter(TreeFormatterBuilder.NodePrinterParser printerParser,
                  Recognizer<?, ?> recognizer,
                  SymbolFormatter terminalFormatter,
                  SymbolFormatter errorFormatter) {
        this.printerParser = printerParser;
        this.recognizer = recognizer;
        this.terminalFormatter = terminalFormatter;
        this.errorFormatter = errorFormatter;
    }

    public String format(ConcreteSyntaxTree tree) {
        StringBuilder buf = new StringBuilder();
        TreeFormatContext ctx = new TreeFormatContext(new AtomicReference<>(tree),
                recognizer,
                terminalFormatter,
                errorFormatter);
        if (!printerParser.format(ctx, buf)) {
            buf.setLength(0);
            throw new SymbolException("Failed to format tree %s".formatted(tree));
        }
        return buf.toString();
    }

    public ConcreteSyntaxTree parseCST(CharSequence text) {
        Map<String, BiFunction<Node<?>, Object, Node<?>>> connectors = Map.ofEntries(entry("rule",
                        (parent, index) -> ConcreteSyntaxTree.Rule.attachTo((ConcreteSyntaxTree) parent,
                                (int) index)),
                entry("terminal",
                        (parent, symbol) -> ConcreteSyntaxTree.Terminal.attachTo((ConcreteSyntaxTree) parent,
                                (Symbol) symbol)),
                entry("error",
                        (parent, symbol) -> ConcreteSyntaxTree.Error.attachTo((ConcreteSyntaxTree) parent,
                                (Symbol) symbol)));
        TreeParseContext ctx = new TreeParseContext(new AtomicReference<>(),
                recognizer,
                connectors,
                terminalFormatter,
                errorFormatter);
        int result = printerParser.parse(ctx, text, 0);
        if (result < 0) {
            throw new RuntimeException("Cannot parse");
        }
        return (ConcreteSyntaxTree) ctx.getNode();
    }

    public TreeFormatter withRecognizer(Recognizer<?, ?> recognizer) {
        return new TreeFormatter(printerParser,
                recognizer,
                terminalFormatter.withVocabulary(recognizer.getVocabulary()),
                errorFormatter.withVocabulary(
                        recognizer.getVocabulary()));
    }

}