package io.github.airbag.tree.pattern;

import io.github.airbag.symbol.FormatterParsePosition;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.TreeParseException;
import org.antlr.v4.runtime.Parser;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class TreePatternFormatter {

    public static TreePatternFormatter SIMPLE = new TreePatternFormatter();

    private final SymbolFormatter symbolFormatter;
    private final SymbolFormatter symbolTagFormatter;
    private final SymbolFormatter ruleTagFormatter;
    private final Parser parser;
    private final String start;
    private final String end;

    public TreePatternFormatter() {
        this(null, SymbolFormatter.SIMPLE, "<", ">");
    }

    private TreePatternFormatter(Parser parser,
                                 SymbolFormatter symbolFormatter,
                                 String start,
                                 String end) {
        if (parser != null) {
            this.symbolFormatter = symbolFormatter.withVocabulary(parser.getVocabulary());
            var tagFormatter = SymbolFormatter.ofPattern("%s[x:]S%s".formatted(start, end));
            this.symbolTagFormatter = tagFormatter.withVocabulary(parser.getVocabulary());
            this.ruleTagFormatter = tagFormatter.withVocabulary(new RuleVocabulary(parser.getRuleNames()));
        } else {
            this.symbolFormatter = symbolFormatter.withVocabulary(null);
            this.symbolTagFormatter = SymbolFormatter.ofPattern("%s[x:]I/%s".formatted(start, end));
            this.ruleTagFormatter = SymbolFormatter.ofPattern("%s[x:]I%s".formatted(start, end));
        }
        this.start = start;
        this.end = end;
        this.parser = parser;
    }

    public String format(TreePattern pattern) {
        StringJoiner joiner = new StringJoiner(" ");
        for (var element : pattern.getElements()) {
            switch (element) {
                case TreePatternBuilder.SymbolPatternElement symbolElement ->
                        joiner.add(symbolFormatter.format(symbolElement.getSymbol()));
                case TreePatternBuilder.SymbolTagPatternElement symbolTagElement ->
                        joiner.add(symbolTagFormatter.format(Symbol.of()
                                .type(symbolTagElement.type())
                                .text(Optional.ofNullable(symbolTagElement.label()).orElse(""))
                                .get()));
                case TreePatternBuilder.RuleTagPatternElement ruleTagElement ->
                        joiner.add(ruleTagFormatter.format(Symbol.of()
                                .type(ruleTagElement.type())
                                .text(Optional.ofNullable(ruleTagElement.label()).orElse(""))
                                .get()));
                default -> throw new RuntimeException("Unknown pattern element");
            }
        }
        return joiner.toString();
    }

    public TreePattern parse(CharSequence text) {
        FormatterParsePosition parsePosition = new FormatterParsePosition(0);
        TreePattern pattern = parse(text, parsePosition);
        if (parsePosition.getErrorIndex() >= 0) {
            throw new TreePatternException(text.toString(),
                    parsePosition.getErrorIndex(),
                    parsePosition.getMessage());
        }
        if (parsePosition.getIndex() != text.length()) {
            throw new TreeParseException("Text has unparsed trailing text at %d".formatted(
                    parsePosition.getIndex()));
        }
        return pattern;
    }

    public TreePattern parse(CharSequence text, FormatterParsePosition position) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
        TreePatternBuilder patternBuilder = new TreePatternBuilder();
        while (position.getIndex() < text.length() && position.getErrorIndex() < 0) {
            Symbol symbol = symbolFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendSymbol(symbol,
                        SymbolField.equalizer(symbolFormatter.getFields()));
                continue;
            }
            symbol = symbolTagFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendSymbolTag(symbol.type(), symbol.text());
                continue;
            }
            symbol = ruleTagFormatter.parse(text, position);
            if (position.getErrorIndex() < 0) {
                patternBuilder.appendRuleTag(symbol.type(), symbol.text());
                continue;
            }
            if (Character.isWhitespace(text.charAt(position.getIndex()))) {
                position.setIndex(position.getIndex() + 1);
                position.setErrorIndex(-1);
            }
        }
        return patternBuilder.toPattern();
    }

    public TreePatternFormatter withRecognizer(Parser parser) {
        if (parser == null) {
            return new TreePatternFormatter(null, symbolFormatter, start, end);
        }
        return new TreePatternFormatter(parser, symbolFormatter, start, end);
    }

    public TreePatternFormatter withSymbolFormatter(SymbolFormatter symbolFormatter) {
        if (symbolFormatter == this.symbolFormatter) {
            return this;
        }
        return new TreePatternFormatter(parser, symbolFormatter, start, end);
    }
}