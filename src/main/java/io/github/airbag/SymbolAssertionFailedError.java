package io.github.airbag;

import io.github.airbag.token.TokenFormatter;
import io.github.airbag.token.TokenListDiffPrinter;
import org.antlr.v4.runtime.Token;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.List;

public class SymbolAssertionFailedError extends AssertionFailedError {

    public SymbolAssertionFailedError(TokenFormatter formatter,
                                      List<? extends Token> expectedList,
                                      List<? extends Token> actualList) {
        super(message(formatter, expectedList, actualList),
                ValueWrapper.create(expectedList, formatter.formatList(expectedList, "\n")),
                ValueWrapper.create(actualList, formatter.formatList(actualList, "\n")));
    }

    private static String message(TokenFormatter formatter,
                                  List<? extends Token> expected,
                                  List<? extends Token> actual) {
        TokenListDiffPrinter diffPrinter = new TokenListDiffPrinter(formatter);
        return """
                The expected symbol list does not match the actual:
                
                %s
                """.formatted(diffPrinter.printDiff(expected, actual));
    }

}