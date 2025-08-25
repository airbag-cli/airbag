package io.github.airbag.format;

import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Vocabulary;

/**
 * A context object that holds state during a parse operation.
 * <p>
 * This context is not thread-safe and should only be used for a single parse operation.
 * It provides the {@link io.github.airbag.token.Tokens.Builder} to be populated
 * and allows communication between different {@link TokenFormatterBuilder.TokenPrinterParser} instances,
 * such as providing the next parser in the chain.
 *
 * @since 1.0
 */
public class TokenParseContext {

    private Tokens.Builder builder;

    private TokenFormatterBuilder.TokenPrinterParser nextPrinterParser;

    private Vocabulary vocabulary;

    /**
     * Gets the token builder being populated during the parse.
     *
     * @return the token builder
     */
    public Tokens.Builder getBuilder() {
        return builder;
    }

    /**
     * Sets the token builder to be populated.
     *
     * @param builder the token builder
     */
    public void setBuilder(Tokens.Builder builder) {
        this.builder = builder;
    }

    /**
     * Gets the next printer/parser in the chain.
     * <p>
     * This is used by parsers (like {@code TextPrinterParser}) that need to know
     * what the terminating element is.
     *
     * @return the next printer/parser, or {@code null} if this is the last one
     */
    public TokenFormatterBuilder.TokenPrinterParser getNextPrinterParser() {
        return nextPrinterParser;
    }

    /**
     * Sets the next printer/parser in the chain.
     *
     * @param nextPrinterParser the next printer/parser
     */
    public void setNextPrinterParser(TokenFormatterBuilder.TokenPrinterParser nextPrinterParser) {
        this.nextPrinterParser = nextPrinterParser;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
}
