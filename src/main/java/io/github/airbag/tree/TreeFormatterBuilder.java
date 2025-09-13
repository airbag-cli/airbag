package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class TreeFormatterBuilder {

    private final List<TreePrinterParser> printerParsers = new ArrayList<>();

    interface TreePrinterParser {

        boolean format(TreeFormatContext ctx, StringBuilder buf);

        int parse(TreeParseContext ctx, CharSequence text, int position);
    }

    static class TerminalNodePrinterParser implements TreePrinterParser {

        private final SymbolFormatter formatter;

        public TerminalNodePrinterParser(SymbolFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            Map.Entry<Integer, Node<?>> e = ctx.nodeMap().lastEntry();
            if (e == null) {
                return false;
            }
            return switch (e.getValue()) {
                case Node.Terminal<?> terminalNode-> {
                    buf.append(formatter.format(terminalNode.getSymbol()));
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            ParsePosition parsePosition = new ParsePosition(position);
            Symbol symbol = formatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() > 0) {
                return ~parsePosition.getErrorIndex();
            }
            Map.Entry<Integer, Node<?>> e = ctx.nodeMap().lastEntry();
            Node<?> parent = e == null ? null : e.getValue();
            BiFunction<? super Node<?>, Object, ? extends Node<?>> connector = ctx.connectors()
                    .get("terminal");
            connector.apply(parent, symbol);
            return parsePosition.getIndex();
        }
    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    static class LiteralPrinterParser implements TreePrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(TreeFormatContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(TreeParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }
    }

    static class ErrorPrinterParser implements TreePrinterParser {

        private final SymbolFormatter formatter;

        public ErrorPrinterParser(SymbolFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public boolean format(TreeFormatContext ctx, StringBuilder buf) {
            Map.Entry<Integer, Node<?>> e = ctx.nodeMap().lastEntry();
            if (e == null) {
                return false;
            }
            return switch (e.getValue()) {
                case Node.Error<?> errorNode-> {
                    buf.append(formatter.format(errorNode.getSymbol()));
                    yield true;
                }
                default -> false;
            };
        }

        @Override
        public int parse(TreeParseContext ctx, CharSequence text, int position) {
            validatePosition(text, position);
            ParsePosition parsePosition = new ParsePosition(position);
            Symbol symbol = formatter.parse(text, parsePosition);
            if (parsePosition.getErrorIndex() > 0) {
                return ~parsePosition.getErrorIndex();
            }
            Map.Entry<Integer, Node<?>> e = ctx.nodeMap().lastEntry();
            Node<?> parent = e == null ? null : e.getValue();
            BiFunction<? super Node<?>, Object, ? extends Node<?>> connector = ctx.connectors()
                    .get("error");
            connector.apply(parent, symbol);
            return parsePosition.getIndex();
        }

    }


}
