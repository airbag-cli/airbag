package io.github.airbag.tree;

import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.TreeFormatterBuilder.TreePrinterParser;
import org.antlr.v4.runtime.Recognizer;

import java.text.ParsePosition;
import java.util.Objects;

public class TreeFormatter {

    private final TreePrinterParser treePrinterParser;
    private final SymbolFormatter symbolFormatter;
    private final Recognizer<?,?> recognizer;

    public static final TreeFormatter SIMPLE = new TreeFormatterBuilder().onRule(onRule -> onRule.appendLiteral("(")
                    .appendRule()
                    .appendLiteral(" ")
                    .appendChildren(" ")
                    .appendLiteral(")"))
            .onTerminal(NodeFormatterBuilder::appendSymbol)
            .onError(onError -> onError.appendLiteral("(<error> ")
                    .appendSymbol()
                    .appendLiteral(")"))
            .toFormatter();

    TreeFormatter(TreePrinterParser treePrinterParser) {
        this.symbolFormatter = SymbolFormatter.SIMPLE;
        this.recognizer = null;
        this.treePrinterParser = treePrinterParser;
    }

    TreeFormatter(SymbolFormatter symbolFormatter, Recognizer<?, ?> recognizer, TreePrinterParser treePrinterParser) {
        this.symbolFormatter = symbolFormatter;
        this.recognizer = recognizer;
        this.treePrinterParser = treePrinterParser;
    }

    public String format(DerivationTree tree) {
        NodeFormatContext ctx = new NodeFormatContext(symbolFormatter, recognizer);
        ctx.setNode(tree);
        StringBuilder buf = new StringBuilder();
        if (!treePrinterParser.format(ctx, buf)) {
            throw new RuntimeException("Cannot format %s".formatted(tree));
        }
        return buf.toString();
    }

    public DerivationTree parse(CharSequence text) {
        ParsePosition parsePosition = new ParsePosition(0);
        DerivationTree tree = parse(text, parsePosition);
        if (parsePosition.getErrorIndex() >= 0) {
            throw new RuntimeException("Cannot parse text %s".formatted(text));
        }
        if (parsePosition.getIndex() != text.length()) {
            throw new RuntimeException("Text has unparsed trainling text at %d".formatted(parsePosition.getIndex()));
        }
        return tree;
    }

    public DerivationTree parse(CharSequence text, ParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        RootParseContext rootCtx = new RootParseContext(symbolFormatter, recognizer);
        int result = treePrinterParser.parse(rootCtx, text, position.getIndex());
        if (result < 0) {
            position.setErrorIndex(~result);
            return null;
        }
        position.setIndex(result);
        return rootCtx.resolve();
    }
}