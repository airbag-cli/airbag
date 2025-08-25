package io.github.airbag.format;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to create a {@link TokenFormatter}.
 * <p>
 * This builder allows the creation of a {@code TokenFormatter} by assembling a
 * sequence of format/parse elements. Each element is appended to the builder in order.
 * For example:
 * <pre>
 * TokenFormatter formatter = new TokenFormatterBuilder()
 *     .appendLiteral("Token[")
 *     .appendIndex()
 *     .appendLiteral("]=")
 *     .appendText()
 *     .appendLiteral("]")
 *     .build();
 * </pre>
 *
 * @see TokenFormatter
 * @since 1.0
 */
public class TokenFormatterBuilder {

    private List<TokenPrinterParser> printerParsers = new ArrayList<>();

    private Vocabulary vocabulary;

    /**
     * Appends a literal character to the formatter.
     *
     * @param literal the character to append
     * @return this builder, for chaining
     */
    public TokenFormatterBuilder appendLiteral(char literal) {
        printerParsers.add(new CharLiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a literal string to the formatter.
     *
     * @param literal the string to append
     * @return this builder, for chaining
     */
    public TokenFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new StringLiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends the token index to the formatter.
     * This is the token's index within the stream.
     *
     * @return this builder, for chaining
     * @see Token#getTokenIndex()
     */
    public TokenFormatterBuilder appendIndex() {
        printerParsers.add(new IndexPrinterParser());
        return this;
    }

    /**
     * Appends the token start index to the formatter.
     * This is the starting character index of the token in the input stream.
     *
     * @return this builder, for chaining
     * @see Token#getStartIndex()
     */
    public TokenFormatterBuilder appendStartIndex() {
        printerParsers.add(new StartIndexPrinterParser());
        return this;
    }

    /**
     * Appends the token stop index to the formatter.
     * This is the last character index of the token in the input stream.
     *
     * @return this builder, for chaining
     * @see Token#getStopIndex()
     */
    public TokenFormatterBuilder appendStopIndex() {
        printerParsers.add(new StopIndexPrinterParser());
        return this;
    }

    /**
     * Appends the token text to the formatter.
     *
     * @return this builder, for chaining
     * @see Token#getText()
     */
    public TokenFormatterBuilder appendText() {
        printerParsers.add(new TextPrinterParser());
        return this;
    }

    /**
     * Appends the token type to the formatter.
     *
     * @return this builder, for chaining
     * @see Token#getType()
     */
    public TokenFormatterBuilder appendType() {
        printerParsers.add(new TypePrinterParser());
        return this;
    }

    /**
     * Appends the token line number to the formatter.
     *
     * @return this builder, for chaining
     * @see Token#getLine()
     */
    public TokenFormatterBuilder appendLine() {
        printerParsers.add(new LinePrinterParser());
        return this;
    }

    /**
     * Appends the token's character position within the line to the formatter.
     *
     * @return this builder, for chaining
     * @see Token#getCharPositionInLine()
     */
    public TokenFormatterBuilder appendPosition() {
        printerParsers.add(new CharPositionPrinterParser());
        return this;
    }

    public TokenFormatterBuilder setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
        return this;
    }

    /**
     * Builds the {@link TokenFormatter} from the appended elements.
     *
     * @return the new formatter
     */
    public TokenFormatter build() {
        return new TokenFormatter(new CompositePrinterParser(printerParsers.toArray(new TokenPrinterParser[0])), vocabulary);
    }


    /**
     * Strategy for formatting and parsing a specific part of a token's string representation.
     */
    public interface TokenPrinterParser {

        /**
         * Formats a token field, appending it to the string builder.
         *
         * @param token the token being formatted
         * @param buf   the buffer to append to
         * @return true if formatting was successful
         */
        boolean format(Token token, StringBuilder buf);


        /**
         * Parses a section of text, consumes it, and updates the token builder.
         *
         * @param context  the context for the parsing operation, containing the builder
         * @param text     the text to parse from
         * @param position the position to start parsing at
         * @return the new position after parsing, or a negative value if parsing fails
         */
        int parse(TokenParseContext context, CharSequence text, int position);

        /**
         * Checks if the parser can parse the text at the given position without
         * consuming it.
         * <p>
         * This method is intended for lookahead operations, allowing a parser to see if
         * the text ahead matches its pattern without altering the state of the overall
         * parse operation. It should not have any side effects.
         *
         * @param text     the text to parse from
         * @param position the position to start checking at
         * @return {@code true} if the text at the position can be parsed, {@code false} otherwise
         */
        boolean peek(CharSequence text, int position);
    }

    /**
     * A printer/parser that combines a sequence of other printer/parsers.
     */
    static final class CompositePrinterParser implements TokenPrinterParser {

        private final TokenPrinterParser[] printerParsers;

        public CompositePrinterParser(TokenPrinterParser[] printerParsers) {
            this.printerParsers = printerParsers;
        }

        @Override
        public boolean format(Token token, StringBuilder buf) {
            for (var printer : printerParsers) {
                printer.format(token, buf);
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            for (int i = 0; i < printerParsers.length; i++) {
                var printer = printerParsers[i];
                context.setNextPrinterParser(i == printerParsers.length - 1 ? null : printerParsers[i + 1]);
                position = printer.parse(context, text, position);
                if (position < 0) {
                    return position;
                }
            }
            return position;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            throw new UnsupportedOperationException();
        }
    }

    /**
         * Prints and parses a literal character.
         */
        private record CharLiteralPrinterParser(char literal) implements TokenPrinterParser {

        @Override
            public boolean format(Token token, StringBuilder buf) {
                buf.append(literal);
                return true;
            }

            @Override
            public int parse(TokenParseContext context, CharSequence text, int position) {
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

            @Override
            public boolean peek(CharSequence text, int position) {
                return parse(null, text, position) > 0;
            }
        }

    /**
         * Prints and parses a literal string.
         */
        private record StringLiteralPrinterParser(String literal) implements TokenPrinterParser {

        @Override
            public boolean format(Token token, StringBuilder buf) {
                buf.append(literal);
                return true;
            }

            @Override
            public int parse(TokenParseContext context, CharSequence text, int position) {
                int length = text.length();
                if (position + literal.length() > length || position < 0) {
                    return ~position;
                }
                if (!text.subSequence(position, position + literal.length()).equals(literal)) {
                    return ~position;
                }
                return position + literal.length();
            }

            @Override
            public boolean peek(CharSequence text, int position) {
                return parse(null, text, position) > 0;
            }
        }

    /**
     * Prints and parses the token index.
     */
    private static final class IndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getTokenIndex());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().index(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }

    /**
     * Finds the end of an integer in the text.
     *
     * @param text     the text to search
     * @param position the starting position
     * @return the end position, or a negative value if no number is found
     */
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
        while (endPosition < text.length() && Character.isDigit(text.charAt(endPosition))) {
            endPosition++;
        }
        if (position == endPosition) {
            return ~endPosition;
        }
        return endPosition;
    }

    /**
     * Prints and parses the token start index.
     */
    private static class StartIndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getStartIndex());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().startIndex(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }

    /**
     * Prints and parses the token stop index.
     */
    private static class StopIndexPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getStopIndex());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().stopIndex(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }

    /**
     * Prints and parses the token line number.
     */
    private static class LinePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getLine());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().line(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }

    /**
     * Prints and parses the token's character position in the line.
     */
    private static class CharPositionPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getCharPositionInLine());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().charPositionInLine(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }

    /**
     * Prints and parses the token text. The text is parsed by consuming characters
     * until the next element in the formatter can be successfully parsed.
     */
    private static class TextPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getText());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            var next = context.getNextPrinterParser();
            if (next == null) {
                context.getBuilder().text(text.subSequence(position, text.length()).toString());
                return text.length();
            }
            int endPosition = position;
            while (!next.peek(text, endPosition)) {
                endPosition++;
            }
            context.getBuilder().text(text.subSequence(position, endPosition).toString());
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return true;
        }
    }

    /**
     * Prints and parses the token type.
     */
    private static class TypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(Token token, StringBuilder buf) {
            buf.append(token.getType());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = findNumberEnd(text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.getBuilder().type(Integer.parseInt(text.subSequence(position,
                    endPosition).toString()));
            return endPosition;
        }

        @Override
        public boolean peek(CharSequence text, int position) {
            return 0 < findNumberEnd(text, position);
        }
    }
}
