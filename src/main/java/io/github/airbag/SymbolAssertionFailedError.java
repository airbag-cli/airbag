package io.github.airbag;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolListDiffPrinter;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.List;

public class SymbolAssertionFailedError extends AssertionFailedError {

    public SymbolAssertionFailedError(SymbolFormatter formatter,
                                      List<Symbol> expectedList,
                                      List<Symbol> actualList) {
        super(message(formatter, expectedList, actualList),
                ValueWrapper.create(expectedList, formatter.formatList(expectedList, "\n")),
                ValueWrapper.create(actualList, formatter.formatList(actualList, "\n")));
    }

    private static String message(SymbolFormatter formatter,
                                  List<Symbol> expected,
                                  List<Symbol> actual) {
        SymbolListDiffPrinter diffPrinter = new SymbolListDiffPrinter(formatter);
        return """
                The expected symbol list does not match the actual:
                
                %s
                """.formatted(diffPrinter.printDiff(expected, actual));
    }

}