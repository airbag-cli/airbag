package io.github.airbag.tree;

import io.github.airbag.tree.NodeFormatterBuilder.LiteralPrinterParser;
import io.github.airbag.tree.NodeFormatterBuilder.NodePrinterParser;

import java.util.function.Consumer;

public class TreeFormatterBuilder {

    private NodePrinterParser[] rulePrinterParsers;
    private NodePrinterParser[] terminalPrinterParsers;
    private NodePrinterParser[] errorPrinterParsers;

    public TreeFormatterBuilder onRule(Consumer<NodeFormatterBuilder> onRule) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onRule.accept(builder);
        rulePrinterParsers = builder.printerParsers();
        return this;
    }

    public TreeFormatterBuilder onTerminal(Consumer<NodeFormatterBuilder> onTerminal) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onTerminal.accept(builder);
        terminalPrinterParsers = builder.printerParsers();
        return this;
    }

    public TreeFormatterBuilder onError(Consumer<NodeFormatterBuilder> onError) {
        NodeFormatterBuilder builder = new NodeFormatterBuilder();
        onError.accept(builder);
        errorPrinterParsers = builder.printerParsers();
        return this;
    }

    public TreeFormatter toFormatter() {
        return new TreeFormatter(new TreePrinterParser(rulePrinterParsers,
                terminalPrinterParsers,
                errorPrinterParsers));
    }

    static class TreePrinterParser implements NodePrinterParser {

        private final CompositePrinterParser rulePrinterParser;
        private final CompositePrinterParser terminalPrinterParser;
        private final CompositePrinterParser errorPrinterParser;

        public TreePrinterParser(NodePrinterParser[] rulePrinterParsers,
                                 NodePrinterParser[] terminalPrinterParsers,
                                 NodePrinterParser[] errorPrinterParsers) {

            for (int i = 0; i < rulePrinterParsers.length; i++) {
                if (rulePrinterParsers[i] instanceof NodeFormatterBuilder.ChildrenPrinterParser(
                        LiteralPrinterParser separator
                )) {
                    rulePrinterParsers[i] = new ChildrenPrinterParser(this, separator);
                }
            }
            this.rulePrinterParser = new CompositePrinterParser(rulePrinterParsers);
            this.terminalPrinterParser = new CompositePrinterParser(terminalPrinterParsers);
            this.errorPrinterParser = new CompositePrinterParser(errorPrinterParsers);
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            return switch (ctx.node()) {
                case DerivationTree.Rule ignored -> rulePrinterParser.format(ctx, buf);
                case DerivationTree.Terminal ignored -> terminalPrinterParser.format(ctx, buf);
                case DerivationTree.Error ignored -> errorPrinterParser.format(ctx, buf);
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int result;
            RootParseContext.Terminal terminalCtx = ctx.root().new Terminal();
            result = terminalPrinterParser.parse(terminalCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(terminalCtx);
                return result;
            }
            RootParseContext.Rule ruleCtx = ctx.root().new Rule();
            result = rulePrinterParser.parse(ruleCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(ruleCtx);
                return result;
            }
            RootParseContext.Error errorCtx = ctx.root().new Error();
            result = errorPrinterParser.parse(errorCtx, text, position);
            if (result > 0) {
                ctx.addChildContext(errorCtx);
            }
            return result;
        }

    }

    static class ChildrenPrinterParser implements NodePrinterParser {

        private final TreePrinterParser treePrinterParser;
        private final LiteralPrinterParser separator;

        ChildrenPrinterParser(TreePrinterParser treePrinterParser, LiteralPrinterParser separator) {
            this.treePrinterParser = treePrinterParser;
            this.separator = separator;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            var current = ctx.node();
            for (int i = 0; i < current.size(); i++) {
                var child = current.getChild(i);
                ctx.setNode(child);
                if (!treePrinterParser.format(ctx, buf)) {
                    ctx.setNode(current);
                    return false;
                }
                if (i + 1 != current.size()) {
                    separator.format(ctx, buf);
                }
            }
            ctx.setNode(current);
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int result;
            do {
                position = treePrinterParser.parse(ctx, text, position);
                result = position;
                position = separator.parse(ctx, text, position);
            } while (position > 0);
            return result;
        }
    }

    static class CompositePrinterParser implements NodePrinterParser {

        private final NodePrinterParser[] printerParsers;

        private CompositePrinterParser(NodePrinterParser[] printerParsers) {
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            int initialLength = buf.length();
            for (NodePrinterParser printer : printerParsers) {
                if (!printer.format(ctx, buf)) {
                    buf.setLength(initialLength);
                    return false;
                }
            }
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            for (NodePrinterParser parser : printerParsers) {
                position = parser.parse(ctx, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }
    }
}