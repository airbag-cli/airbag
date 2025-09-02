package io.github.airbag.format;

import io.github.airbag.token.TokenField;
import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.*;

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
    Set<TokenField<?>> fields = new HashSet<>();

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
     * <p>
     * This method uses the {@link TextOption#NOTHING} option, which means no
     * escaping is performed.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendText() {
        printerParsers.add(new TextPrinterParser());
        fields.add(TokenField.TEXT);
        return this;
    }

    /**
     * Appends a printer/parser for the token's text, with custom escaping and default value behavior.
     * <p>
     * This component is non-greedy; it consumes characters only up to the point
     * where the next component in the formatter is able to match. If this is the
     * last component in the sequence, it will consume the remainder of the input string.
     *
     * @param option The {@link TextOption} to use for formatting and parsing the text.
     * @return This builder.
     * @see TextOption
     */
    public TokenFormatterBuilder appendText(TextOption option) {
        printerParsers.add(new TextPrinterParser(option));
        fields.add(TokenField.TEXT);
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
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the token's literal type name (e.g., {@code '='}, {@code '*'}).
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
        printerParsers.add(new LiteralTypePrinterParser());
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the token's type, with a configurable format.
     * <p>
     * This method provides a flexible way to format and parse a token's type by specifying
     * a {@link TypeFormat}. The format determines which representations of the type are
     * used and in what order. For example, a type can be represented by its symbolic name
     * (e.g., "ID"), its literal name (e.g., "'='"), or its raw integer value.
     * <p>
     * <b>Formatting:</b> The formatter will attempt to represent the token's type using the
     * strategies defined by the {@link TypeFormat}, in order. The first successful
     * representation will be appended to the output. If no representation is successful
     * (e.g., a symbolic name is requested but not available), the formatting for this
     * component fails.
     * <p>
     * <b>Parsing:</b> The parser will attempt to match the input text against the possible
     * representations defined by the {@link TypeFormat}, in order. The first representation
     * that successfully matches and parses the input will be used.
     * <p>
     * This is a more general version of {@link #appendSymbolicType()} and
     * {@link #appendLiteralType()}, which correspond to {@link TypeFormat#SYMBOLIC_ONLY}
     * and {@link TypeFormat#LITERAL_ONLY} respectively.
     *
     * @param format The format to use for the token's type.
     * @return This builder.
     * @see TypeFormat
     */
    public TokenFormatterBuilder appendType(TypeFormat format) {
        printerParsers.add(new TypePrinterParser(format));
        fields.add(TokenField.TYPE);
        return this;
    }

    /**
     * Appends a printer/parser for the end-of-file (EOF) token.
     * <p>
     * <b>Formatting:</b> If the token's type is {@link Token#EOF}, this component
     * appends the string "EOF". For any other token type, it fails.
     * <p>
     * <b>Parsing:</b> It matches the literal string "EOF" and resolves it to a
     * token of type {@link Token#EOF}.
     *
     * @return This builder.
     */
    public TokenFormatterBuilder appendEOF() {
        printerParsers.add(new EOFPrinterParser());
        fields.add(TokenField.TYPE);
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

    static class TextPrinterParser implements TokenPrinterParser {

        private final TextOption option;

        TextPrinterParser() {
            this(TextOption.NOTHING);
        }

        TextPrinterParser(TextOption option) {
            this.option = option;
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            String text = context.token().getText();
            var escapeMap = option.getEscapeMap();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escapeMap.containsKey(c)) {
                    buf.append(option.getEscapeChar()).append(escapeMap.get(c));
                } else {
                    buf.append(c);
                }
            }
            if (text.isEmpty()) {
                buf.append(option.getDefaultValue());
            }
            return true;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int endPosition = peek(context, text, position);
            if (endPosition < 0) {
                return endPosition;
            }
            StringBuilder buf = unescapeText(text, position, endPosition);
            String tokenText = buf.toString();
            if (tokenText.equals(option.getDefaultValue())) {
                tokenText = "";
            }
            context.addField(TokenField.TEXT, tokenText);
            return endPosition;
        }

        private StringBuilder unescapeText(CharSequence text, int position, int endPosition) {
            StringBuilder buf = new StringBuilder();
            //Since we peeked before we know that reescaping will work
            char escape = option.getEscapeChar();
            var unescapeMap = option.getUnescapeMap();
            for (int i = position; i < endPosition; i++) {
                char c = text.charAt(i);
                if (c == escape) {
                    buf.append(unescapeMap.get(text.charAt(i + 1)));
                    i++;
                } else {
                    buf.append(c);
                }
            }
            return buf;
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
            var unescapeMap = option.getUnescapeMap();
            var escapeChar = option.getEscapeChar();
            while (position < text.length() && next.peek(context, text, position) < 0) {
                if (text.charAt(position) == escapeChar) {
                    if (text.length() == position + 1 ||
                        !unescapeMap.containsKey(text.charAt(position + 1))) {
                        return ~position; //Invalid escape
                    } else {
                        position++; //Escape characters cannot be delimiters.
                    }
                }
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
            validatePosition(text, position);
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
            context.addField(TokenField.TEXT, literalName.substring(1, literalName.length() - 1));
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
            validatePosition(text, position);
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

    static class TypePrinterParser implements TokenPrinterParser {

        private TokenPrinterParser[] printerParsers;

        TypePrinterParser(TypeFormat format) {
            switch (format) {
                case INTEGER_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new IntegerPrinterParser(
                                TokenField.TYPE)};
                case SYMBOLIC_FIRST ->
                        printerParsers = new TokenPrinterParser[]{new SymbolicTypePrinterParser(),
                                new LiteralTypePrinterParser(),
                                new IntegerPrinterParser(TokenField.TYPE)};
                case LITERAL_FIRST ->
                        printerParsers = new TokenPrinterParser[]{new LiteralTypePrinterParser(),
                                new SymbolicTypePrinterParser(),
                                new IntegerPrinterParser(TokenField.TYPE)};
                case SYMBOLIC_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new SymbolicTypePrinterParser()};
                case LITERAL_ONLY ->
                        printerParsers = new TokenPrinterParser[]{new LiteralTypePrinterParser()};
            }
        }

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            for (TokenPrinterParser printer : printerParsers) {
                if (printer.format(context, buf)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                if (parser.peek(context, text, position) > 0) {
                    return parser.parse(context, text, position);
                }
            }
            return ~position;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            for (TokenPrinterParser parser : printerParsers) {
                int peeked = parser.peek(context, text, position);
                if (peeked > 0) {
                    return peeked;
                }
            }
            return ~position;
        }
    }

    static class EOFPrinterParser implements TokenPrinterParser {

        @Override
        public boolean format(TokenFormatContext context, StringBuilder buf) {
            Token token = context.token();
            if (token.getType() == Token.EOF) {
                buf.append("EOF");
                return true;
            }
            return false;
        }

        @Override
        public int parse(TokenParseContext context, CharSequence text, int position) {
            int end = peek(context, text, position);
            if (end < 0) {
                return end;
            }
            context.addField(TokenField.TYPE, Token.EOF);
            return end;
        }

        @Override
        public int peek(TokenParseContext context, CharSequence text, int position) {
            validatePosition(text, position);
            if (position + 3 > text.length()) {
                return ~position;
            }
            boolean matches = text.subSequence(position, position + 3).toString().equals("EOF");
            return matches ? position + 3 : ~position;
        }
    }
}