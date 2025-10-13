package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class NodeFormatterBuilder {

    private final List<NodePrinterParser> printerParsers = new ArrayList<>();

    NodeFormatterBuilder() {

    }

    public NodeFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    public NodeFormatterBuilder appendSymbol() {
        printerParsers.add(new SymbolPrinterParser());
        return this;
    }

    public NodeFormatterBuilder appendRule() {
        printerParsers.add(new RulePrinterParser());
        return this;
    }

    NodePrinterParser[] printerParsers() {
        return printerParsers.toArray(new NodePrinterParser[0]);
    }

    public NodeFormatterBuilder appendChildren(String separator) {
        printerParsers.add(new ChildrenPrinterParser(new LiteralPrinterParser(separator)));
        return this;
    }

    interface NodePrinterParser {

        boolean format(NodeFormatContext ctx, StringBuilder buf);

        int parse(NodeParseContext ctx, CharSequence text, int position);
    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    static class LiteralPrinterParser implements NodePrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }
    }

    static class SymbolPrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            return switch(ctx.node()) {
                case DerivationTree.Terminal terminalNode -> {
                    SymbolFormatter formatter = ctx.symbolFormatter();
                    buf.append(formatter.format(terminalNode.symbol()));
                    yield true;
                }
                case DerivationTree.Error errorNode -> {
                    SymbolFormatter formatter = ctx.symbolFormatter();
                    buf.append(formatter.format(errorNode.symbol()));
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            ParsePosition parsePosition = new ParsePosition(position);
            SymbolFormatter symbolFormatter = ctx.symbolFormatter();
            Symbol symbol = symbolFormatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() > 0) {
                return ~parsePosition.getErrorIndex();
            }
            if (ctx instanceof RootParseContext.Terminal terminalCtx) {
                terminalCtx.setSymbol(symbol);
            } else if (ctx instanceof RootParseContext.Error errorCtx) {
                errorCtx.setSymbol(symbol);
            } else {
                throw new RuntimeException("Wrong context type");
            }
            return parsePosition.getIndex();
        }
    }

    static class IntegerRulePrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            DerivationTree node = ctx.node();
            return switch (node) {
                case DerivationTree.Rule ruleNode -> {
                    buf.append(ruleNode.index());
                    yield true;
                }
                case null, default -> false;
            };
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            int numberEnd = peek(text, position);
            if (numberEnd < 0) {
                return numberEnd;
            }
            int ruleIndex = Integer.parseInt(text.subSequence(position, numberEnd).toString());
            if (ctx instanceof RootParseContext.Rule ruleCtx) {
                ruleCtx.setIndex(ruleIndex);
            } else {
                throw new RuntimeException("No rule context");
            }
            return numberEnd;
        }

        public int peek(CharSequence text, int position) {
            validatePosition(text, position);
            return text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
        }
    }

    static int findNumberEnd(CharSequence text, int position) {
        if (position >= text.length() || !Character.isDigit(text.charAt(position))) {
            return ~position;
        }
        while (text.length() != position && Character.isDigit(text.charAt(position))) {
            position++;
        }
        return position;
    }

    static class StringRuleNamePrinterParser implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            DerivationTree node = ctx.node();
            return switch (node) {
                case DerivationTree.Rule ruleNode -> {
                    String[] ruleNames = ctx.recognizer() == null ?
                            new String[0] :
                            ctx.recognizer().getRuleNames();
                    if (ruleNode.index() >= ruleNames.length) {
                        yield false;
                    }
                    buf.append(ruleNames[ruleNode.index()]);
                    yield true;
                }
                case null, default -> false;
            };
        }


        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            String[] ruleNames = ctx.recognizer() == null ?
                    new String[0] :
                    ctx.recognizer().getRuleNames();
            int index = findRuleIndex(text, ruleNames, position);
            if (index < 0) {
                return ~position;
            }
            if (ctx instanceof RootParseContext.Rule ruleContext) {
                ruleContext.setIndex(index);
            }
            return position + ruleNames[index].length();
        }

        private int findRuleIndex(CharSequence text, String[] ruleNames, int position) {
            int index = -1;
            int maxLength = 0;
            for (int i = 0; i < ruleNames.length; i++) {
                String ruleName = ruleNames[i];
                if (ruleName.length() > maxLength &&
                    text.length() - position >= ruleName.length() &&
                    text.subSequence(position, position + ruleName.length())
                            .toString()
                            .equals(ruleName)) {
                    maxLength = ruleName.length();
                    index = i;
                }
            }
            return index;
        }
    }

    static class RulePrinterParser implements NodePrinterParser {

        private final NodePrinterParser[] printerParsers;

        RulePrinterParser() {
            printerParsers = new NodePrinterParser[]{new StringRuleNamePrinterParser(),
                    new IntegerRulePrinterParser()};
        }

        @Override
        public boolean format(NodeFormatContext context, StringBuilder buf) {
            for (NodePrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(NodeParseContext context, CharSequence text, int position) {
            for (NodePrinterParser parser : printerParsers) {
                int end = parser.parse(context, text, position);
                if (end > 0) {
                    return end;
                }
            }
            return ~position;
        }
    }

    record ChildrenPrinterParser(LiteralPrinterParser separator) implements NodePrinterParser {

        @Override
        public boolean format(NodeFormatContext ctx, StringBuilder buf) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int parse(NodeParseContext ctx, CharSequence text, int position) {
            throw new UnsupportedOperationException();
        }
    }


}