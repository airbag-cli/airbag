package io.github.airbag.format;

import io.github.airbag.format.TokenFormatterBuilder.TokenPrinterParser;

public class TokenParseException extends RuntimeException {

    public TokenParseException(String input, int position, TokenPrinterParser printerParser) {
        super("Parse error occurred at position %d; The input %s does not match the pattern %s".formatted(
                position,
                input,
                printerParser));
    }
}
