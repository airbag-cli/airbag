package io.github.airbag.format;

import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Vocabulary;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A builder for creating {@link TokenFormatter} instances.
 * <p>
 * This builder provides a fluent API for constructing a token formatter with a specific format.
 * The format is defined by appending a sequence of printer/parsers to the builder.
 * Each printer/parser is responsible for formatting and parsing a specific part of the token.
 */
public class TokenFormatterBuilder {

    /**
     * The list of printer/parsers that make up the format of the token.
     */
    List<TokenPrinterParser> printerParsers = new ArrayList<>();

    /**
     * The list of token fields that are used by the printer/parsers.
     */
    List<TokenField<?>> fields = new ArrayList<>();

    /**
     * Appends a printer/parser for an integer field to the formatter.
     *
     * @param field The integer field to append.
     * @return This builder.
     */
    public TokenFormatterBuilder appendInteger(TokenField<Integer> field) {
        printerParsers.add(new IntegerPrinterParser(field));
        fields.add(field);
        return this;
    }

    /**
     * Appends a printer/parser for a literal string to the formatter.
     *
     * @param literal The literal string to append.
     * @return This builder.
     */
    public TokenFormatterBuilder appendLiteral(String literal) {
        printerParsers.add(new LiteralPrinterParser(literal));
        return this;
    }

    /**
     * Appends a printer/parser for the token's text.
     * <p>
     * This component is non-greedy; it consumes characters only up to the point
     * where the next component in the formatter is able to match. If this is the
     * last component in the sequence, it will consume the remainder of the input string.
     * <p>
     * It is useful for capturing variable text that is situated between more
     * well-defined components (like literals or symbolic types).
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendText() {
        printerParsers.add(new TextPrinterParser());
        return this;
    }

    /**
     * Appends a printer/parser for the token's symbolic type name (e.g., "ID", "INT").
     * <p>
     * This component provides a strict mapping between a token's type and its symbolic name
     * as defined in the ANTLR {@link Vocabulary}.
     * <p>
     * <b>Formatting:</b> It will throw a {@link TokenException} if the vocabulary is missing or
     * if the token's type does not have a symbolic name. This is often the case for tokens
     * representing literals (e.g., keywords, operators like {@code '='}), which have a literal
     * name but not a symbolic one.
     * <p>
     * <b>Parsing:</b> It reads a symbolic name from the input and resolves it back to the
     * corresponding token type.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendSymbolicType() {
        printerParsers.add(new SymbolicTypePrinterParser());
        return this;
    }

    /**
     * Appends a printer/parser for the token's literal type name (e.g., "'='", "'*'").
     * <p>
     * This component maps a token's type to its literal name as defined in the ANTLR
     * {@link Vocabulary}. Literal names are the exact strings defined in the grammar,
     * typically enclosed in single quotes (e.g., {@code '='}).
     * <p>
     * <b>Formatting:</b> The literal name from the vocabulary (including the single quotes)
     * is appended to the output. It will throw a {@link TokenException} if the vocabulary
     * is missing or if the token's type does not have a literal name. This is often the
     * case for tokens with symbolic names like {@code ID} or {@code INT}.
     * <p>
     * <b>Parsing:</b> The parser expects the input to contain the literal name, including
     * the single quotes. It finds the longest possible literal in the vocabulary that
     * matches the input string and resolves it to the corresponding token type.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendLiteralType() {
        printerParsers.addLast(new LiteralTypePrinterParser());
        return this;
    }

    /**
     * Builds the token formatter.
     *
     * @return The built token formatter.
     */
    public TokenFormatter toFormatter() {
        return new TokenFormatter(new CompositePrinterParser(printerParsers), fields, null);
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
         * but does not consume them. Implementations of this method must not alter the parse context.
         *
         * @param context  the parse context.
         * @param text     the text to peek into.
         * @param position the position to start peeking from.
         * @return the position of the potential match if successful, or a negative value if it does not match.
         */
        int peek(TokenParseContext context, CharSequence text, int position);

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
        public int peek(TokenParseContext context, CharSequence text, int position) {
            throw new UnsupportedOperationException();
        }

    }

    static class IntegerPrinterParser implements TokenPrinterParser {

        private final TokenField<Integer> integerTokenField;

        IntegerPrinterParser(TokenField<Integer> integerTokenField) {
            this.integerTokenField = integerTokenField;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            buf.append(integerTokenField.access(context.token()));
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int numberEnd = peek(context, text, position);
            if (numberEnd < 0) {
                return numberEnd;
            }
            context.addField(integerTokenField,
                    Integer.valueOf(text.subSequence(position, numberEnd).toString()));
            return numberEnd;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            return text.charAt(position) == '-' ?
                    findNumberEnd(text, position + 1) :
                    findNumberEnd(text, position);
        }

    }

    private static void validatePosition(CharSequence text, int position) {
        int length = text.length();
        if (position > length || position < 0) {
            throw new IndexOutOfBoundsException();
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

    static class LiteralPrinterParser implements TokenPrinterParser {

        private final String literal;

        LiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            buf.append(literal);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            return peek(context, text, position);
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            int positionEnd = position + literal.length();
            if (positionEnd > text.length() ||
                !literal.equals(text.subSequence(position, positionEnd).toString())) {
                return ~position;
            }
            return positionEnd;
        }

    }

    /**
     * A printer/parser for the text of a token.
     */
    static class TextPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            buf.append(context.token().getText());
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            context.addField(TokenField.TEXT, text.subSequence(position, endPosition).toString());
            return endPosition;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            TokenPrinterParser[] parserChain = context.printerParser().printerParsers;
            int parserIndex = findParserIndex(parserChain);
            TokenPrinterParser next = parserIndex + 1 < parserChain.length ?
                    parserChain[parserIndex + 1] :
                    null;
            if (next == null) {
                return text.length();
            }
            while (position < text.length() && next.peek(context, text, position) < 0) {
                position++;
            }
            return position;
        }

        private int findParserIndex(TokenPrinterParser[] parserChain) {
            for (int i = 0; i < parserChain.length; i++) {
                if (parserChain[i] == this) {
                    return i;
                }
            }
            throw new RuntimeException("Parser is not part of the chain");
        }
    }

    static class SymbolicTypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String symbolicName = vocabulary.getSymbolicName(context.token().getType());
            if (symbolicName == null) {
                return false;
            }
            buf.append(symbolicName);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            String symbolicName = text.subSequence(position, endPosition).toString();
            int type = Tokens.getTokenType(symbolicName, context.vocabulary());
            context.addField(TokenField.TYPE, type);
            return endPosition;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return ~position;
            }
            int longestMatch = 0;
            for (int i = -1; i < vocabulary.getMaxTokenType() + 1; i++) {
                String symbolicName = vocabulary.getSymbolicName(i);
                if (symbolicName == null) {
                    continue;
                }
                int length = symbolicName.length();
                if (position + length > text.length()) {
                    continue;
                }
                if (symbolicName.equals(text.subSequence(position, position + length).toString())) {
                    longestMatch = Math.max(longestMatch, length);
                }
            }
            return longestMatch == 0 ? ~position : position + longestMatch;
        }
    }

    static class LiteralTypePrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return false;
            }
            String literalName = vocabulary.getLiteralName(context.token().getType());
            if (literalName == null) {
                return false;
            }
            buf.append(literalName);
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            String literalName = text.subSequence(position, endPosition).toString();
            int type = findLiteralType(literalName, context.vocabulary());
            context.addField(TokenField.TYPE, type);
            return endPosition;
        }

        private int findLiteralType(String literal, Vocabulary vocabulary) {
            for (int i = 1; i < vocabulary.getMaxTokenType() + 1; i++) {
                if (Objects.equals(literal, vocabulary.getLiteralName(i))) {
                    return i;
                }
            }
            throw new RuntimeException("No literal type found");
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            Vocabulary vocabulary = context.vocabulary();
            if (vocabulary == null) {
                return ~position;
            }
            int longestMatch = 0;
            for (int i = 1; i < vocabulary.getMaxTokenType() + 1; i++) {
                String literalName = vocabulary.getLiteralName(i);
                if (literalName == null) {
                    continue;
                }
                int length = literalName.length();
                if (position + length > text.length()) {
                    continue;
                }
                if (literalName.equals(text.subSequence(position, position + length).toString())) {
                    longestMatch = Math.max(longestMatch, length);
                }
            }
            return longestMatch == 0 ? ~position : position + longestMatch;
        }
    }
}