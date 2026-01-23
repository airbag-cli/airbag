package io.github.airbag.tree.pattern;

import io.github.airbag.symbol.FormatterParsePosition;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.TreeParseException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Recognizer;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Formats {@link Pattern} objects into a string representation and parses string representations
 * back into {@link Pattern} objects.
 * <p>
 * This formatter uses internal {@link SymbolFormatter} instances to handle the formatting
 * and parsing of individual {@link PatternBuilder.TreePatternElement}s.
 * It can be configured with a specific {@link Parser} to provide contextual rule and symbol names,
 * and with a custom {@link SymbolFormatter} for finer control over symbol representation.
 * </p>
 */
public class PatternFormatter {

    /**
     * A simple, default instance of {@code PatternFormatter} that does not use a specific
     * ANTLR {@link Parser} and employs a basic {@link SymbolFormatter}.
     */
    public static PatternFormatter SIMPLE = new PatternFormatter();

    private final SymbolFormatter symbolFormatter;
    private final SymbolFormatter symbolTagFormatter;
    private final SymbolFormatter ruleTagFormatter;
    private final Recognizer<?, ?> parser;
    private final String start;
    private final String end;

    /**
     * Constructs a default {@code PatternFormatter} with no associated parser and
     * uses {@link SymbolFormatter#SIMPLE} for symbol formatting, and "<" ">" as delimiters.
     */
    public PatternFormatter() {
        this(null, SymbolFormatter.SIMPLE, "<", ">");
    }

    /**
     * Private constructor used for creating configured instances of {@code PatternFormatter}.
     *
     * @param parser          The ANTLR {@link Parser} to use for vocabulary and rule names, or {@code null}.
     * @param symbolFormatter The {@link SymbolFormatter} to use for formatting and parsing symbols.
     * @param start           The starting delimiter for tags (e.g., "<").
     * @param end             The ending delimiter for tags (e.g., ">").
     */
    private PatternFormatter(Recognizer<?, ?> parser,
                             SymbolFormatter symbolFormatter,
                             String start,
                             String end) {
        if (parser != null) {
            this.symbolFormatter = symbolFormatter.withVocabulary(parser.getVocabulary());
            var tagFormatter = SymbolFormatter.ofPattern("%s[x:]S%s".formatted(start, end));
            this.symbolTagFormatter = tagFormatter.withVocabulary(parser.getVocabulary());
            this.ruleTagFormatter = tagFormatter.withVocabulary(new RuleVocabulary(parser.getRuleNames()));
        } else {
            // If no parser is provided, use generic tag patterns without specific vocabulary.
            this.symbolFormatter = symbolFormatter.withVocabulary(null);
            // Default pattern for symbol tags when no parser is present.
            this.symbolTagFormatter = SymbolFormatter.ofPattern("%s[x:]I/%s".formatted(start, end));
            // Default pattern for rule tags when no parser is present.
            this.ruleTagFormatter = SymbolFormatter.ofPattern("%s[x:]I%s".formatted(start, end));
        }
        this.start = start;
        this.end = end;
        this.parser = parser;
    }

    /**
     * Formats a given {@link Pattern} object into its string representation.
     * Each element of the pattern is formatted and joined by spaces.
     *
     * @param pattern The {@link Pattern} to format.
     * @return A string representation of the pattern.
     * @throws RuntimeException if an unknown pattern element type is encountered.
     */
    public String format(Pattern pattern) {
        StringJoiner joiner = new StringJoiner(" ");
        for (var element : pattern.getElements()) {
            switch (element) {
                case PatternBuilder.SymbolPatternElement symbolElement ->
                        joiner.add(symbolFormatter.format(symbolElement.getSymbol()));
                case PatternBuilder.SymbolTagPatternElement symbolTagElement ->
                        joiner.add(symbolTagFormatter.format(Symbol.of()
                                .type(symbolTagElement.type())
                                .text(Optional.ofNullable(symbolTagElement.label()).orElse(""))
                                .get()));
                case PatternBuilder.RuleTagPatternElement ruleTagElement ->
                        joiner.add(ruleTagFormatter.format(Symbol.of()
                                .type(ruleTagElement.type())
                                .text(Optional.ofNullable(ruleTagElement.label()).orElse(""))
                                .get()));
                default -> throw new RuntimeException("Unknown pattern element");
            }
        }
        return joiner.toString();
    }

    /**
     * Parses a character sequence into a {@link Pattern} object.
     * This method assumes the entire {@code text} should be consumed.
     *
     * @param text The character sequence representing the pattern.
     * @return A new {@link Pattern} object parsed from the text.
     * @throws PatternException    If a parsing error occurs within the pattern.
     * @throws TreeParseException If there is unparsed trailing text.
     */
    public Pattern parse(CharSequence text) {
        FormatterParsePosition parsePosition = new FormatterParsePosition(0);
        Pattern pattern = parse(text, parsePosition);
        if (parsePosition.getErrorIndex() >= 0) {
            throw new PatternException(text.toString(),
                    parsePosition.getErrorIndex(),
                    parsePosition.getMessage());
        }
        if (parsePosition.getIndex() != text.length()) {
            throw new TreeParseException("Text has unparsed trailing text at %d".formatted(
                    parsePosition.getIndex()));
        }
        return pattern;
    }

    /**
     * Parses a character sequence into a {@link Pattern} object, using a provided
     * {@link FormatterParsePosition} to manage parsing state (current index, error status).
     *
     * @param text     The character sequence representing the pattern.
     * @param position The {@link FormatterParsePosition} to use for parsing.
     * @return A new {@link Pattern} object parsed from the text.
     * @throws NullPointerException If {@code text} or {@code position} is {@code null}.
     */
    public Pattern parse(CharSequence text, FormatterParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        PatternBuilder patternBuilder = new PatternBuilder();
        // Loop while there's text to process and no error has occurred.
        while (position.getIndex() < text.length() && position.getErrorIndex() < 0) {
            // Attempt to parse as a raw symbol
            Symbol symbol = symbolFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendSymbol(symbol,
                        SymbolField.equalizer(symbolFormatter.getFields()));
                continue;
            }
            // If not a raw symbol, attempt to parse as a symbol tag
            symbol = symbolTagFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendSymbolTag(symbol.type(), symbol.text());
                continue;
            }
            // If not a symbol tag, attempt to parse as a rule tag
            symbol = ruleTagFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendRuleTag(symbol.type(), symbol.text());
                continue;
            }
            // If none of the above, check for whitespace to advance the position
            if (Character.isWhitespace(text.charAt(position.getIndex()))) {
                position.setIndex(position.getIndex() + 1);
                position.setErrorIndex(-1); // Clear error if it was just whitespace
            } else {
                // If not whitespace and no pattern matched, set an error.
                position.setErrorIndex(position.getIndex());
                position.setMessage("Unexpected character: '%s'".formatted(text.charAt(position.getIndex())));
            }
        }
        return patternBuilder.toPattern();
    }

    /**
     * Returns a new {@code PatternFormatter} instance configured with the given ANTLR {@link Parser}.
     * This allows the formatter to use the parser's vocabulary and rule names for more meaningful
     * formatting and parsing.
     *
     * @param parser The ANTLR {@link Parser} to associate with this formatter. Can be {@code null} to reset.
     * @return A new {@code PatternFormatter} instance.
     */
    public PatternFormatter withRecognizer(Recognizer<?, ?> parser) {
        if (parser == this.parser) {
            return this;
        }
        return new PatternFormatter(parser, symbolFormatter, start, end);
    }

    /**
     * Returns a new {@code PatternFormatter} instance configured with a custom {@link SymbolFormatter}.
     * This allows for fine-grained control over how individual symbols are formatted and parsed.
     *
     * @param symbolFormatter The {@link SymbolFormatter} to use for symbols.
     * @return A new {@code PatternFormatter} instance.
     */
    public PatternFormatter withSymbolFormatter(SymbolFormatter symbolFormatter) {
        if (symbolFormatter == this.symbolFormatter) {
            return this;
        }
        return new PatternFormatter(parser, symbolFormatter, start, end);
    }
}