package io.github.airbag.format;

import io.github.airbag.token.TokenField;

import java.util.ArrayList;
import java.util.List;

public class TokenFormatterBuilder {

    List<TokenPrinterParser> printerParsers = new ArrayList<>();
    List<TokenField<?>> fields = new ArrayList<>();

    public TokenFormatterBuilder appendInteger(TokenField<Integer> field) {
        printerParsers.add(new IntegerPrinterParser(field));
        fields.add(field);
        return this;
    }

    public TokenFormatter toFormatter() {
        return new TokenFormatter(new CompositePrinterParser(printerParsers), fields);
    }


    /**
     * The internal interface for parsing and formatting.
     * This interface is the building block for the composite {@link TokenFormatter}.
     * It defines the dual functionality of formatting (printing) a token stream
     * and parsing an input character sequence.
     */
    interface TokenPrinterParser {

        /**
         * Formats a value from a context into a string buffer.
         *
         * @param context the context holding the values to be formatted.
         * @param buf     the buffer to append the formatted text to.
         * @return true if the formatting was successful, false otherwise.
         */
        boolean format(TokenFormatContext context, StringBuilder buf);

        /**
         * Parses a text string, consuming characters and updating the context.
         *
         * @param context  the context to store the parsed values.
         * @param text     the text to parse.
         * @param position the position to start parsing from.
         * @return the new position after a successful parse, or a negative value if parsing fails.
         * @throws NullPointerException      if the context or text is null
         * @throws IndexOutOfBoundsException if the position is invalid
         */
        int parse(TokenParseContext context, CharSequence text, int position);

        /**
         * Peeks ahead in the text to see if the next characters match this parser's rule,
         * but does not consume them.
         *
         * @param text     the text to peek into.
         * @param position the position to start peeking from.
         * @return the position of the potential match if successful, or a negative value if it does not match.
         */
        int peek(CharSequence text, int position);

        /**
         * Checks if the parser is lazy. A lazy parser will match as little as possible
         * to allow a subsequent parser to match. This is similar to reluctant quantifiers
         * in regular expressions.
         *
         * @return true if the parser is lazy, false otherwise.
         */
        boolean isLazy();

    }

    static final class CompositePrinterParser implements TokenPrinterParser {

        private final TokenPrinterParser[] printerParsers;

        private CompositePrinterParser(List<TokenPrinterParser> printerParsers) {
            this(printerParsers.toArray(new TokenPrinterParser[0]));
        }

        private CompositePrinterParser(TokenPrinterParser[] printerParsers) {
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            int initialLength = buf.length();
            for (TokenPrinterParser printer : printerParsers) {
                if (!printer.format(context, buf)) {
                    buf.setLength(initialLength);
                    return false;
                }
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                position = parser.parse(context, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }

        @Override
        public int peek(CharSequence text, int position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLazy() {
            return false;
        }
    }

    static class IntegerPrinterParser implements TokenPrinterParser {

        private final TokenField<Integer> integerTokenField;

        public IntegerPrinterParser(TokenField<Integer> integerTokenField) {
            this.integerTokenField = integerTokenField;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            buf.append(integerTokenField.access(context.token()));
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int numberEnd = text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
            if (numberEnd < 0) {
                return numberEnd;
            }
            context.addField(integerTokenField,
                    Integer.valueOf(text.subSequence(position, numberEnd).toString()));
            return numberEnd;
        }

        @Override
        public int peek(CharSequence text, int position) {
            return text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
        }

        @Override
        public boolean isLazy() {
            return false;
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

}
