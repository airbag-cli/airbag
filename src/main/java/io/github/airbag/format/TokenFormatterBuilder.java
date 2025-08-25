package io.github.airbag.format;

import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;

public class TokenFormatterBuilder {

    interface TokenPrinterParser {

        boolean format(Token token, StringBuilder buf);


        int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next);
    }

    private static final class CompositePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            return false;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            return 0;
        }
    }

    private static final class CharLiteralPrinterParser implements TokenPrinterParser {

        private final char literal;

        public CharLiteralPrinterParser(char literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char ch = text.charAt(position);
            if (ch != literal) {
                return ~position;
            }
            return position + 1;
        }
    }

    private static final class StringLiteralPrinterParser implements TokenPrinterParser {
        private final String literal;

        private StringLiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int length = text.length();
            if (position > length || position < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (!text.subSequence(position, literal.length() + 1).equals(literal)) {
                return ~position;
            }
            return position + literal.length();
        }
    }

    private static final class IndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getTokenIndex());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.index(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }

    private static int findNumberEnd(CharSequence text, int position) {
        int endPosition = position;
        if (text.charAt(position) == '-') {
            endPosition++;
            if (!Character.isDigit(text.charAt(endPosition))) {
                return ~endPosition;
            } else {
                endPosition++;
            }
        }
        while (Character.isDigit(text.charAt(endPosition))) {
            endPosition++;
        }
        if (position == endPosition) {
            return ~endPosition;
        }
        return endPosition;
    }

    private static class StartIndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getStartIndex());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.startIndex(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }

    private static class StopIndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getStopIndex());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.stopIndex(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }

    private static class LinePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getLine());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.line(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }

    private static class CharPositionPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getCharPositionInLine());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.charPositionInLine(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }

    private static class TextPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getText());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            if (next == null) {
                context.text(text.subSequence(position, text.length() + 1).toString());
                return text.length();
            }
            int endPosition = position;
            while (next.parser(context, text, position, null) < 0) {
                endPosition++;
            }
            context.text(text.subSequence(position, endPosition).toString());
            return endPosition;
        }
    }

    private static class TypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getType());
            return true;
        }

        @Override
        public int parser(Tokens.Builder context, CharSequence text, int position, TokenPrinterParser next) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.type(Integer.parseInt(text.subSequence(position, endPosition).toString()));
            return endPosition;
        }
    }
}
