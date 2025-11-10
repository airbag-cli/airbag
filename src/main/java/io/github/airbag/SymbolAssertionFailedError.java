package io.github.airbag;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.symbol.SymbolListDiffPrinter;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class SymbolAssertionFailedError extends AssertionFailedError {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    public SymbolAssertionFailedError(Symbol expected, Symbol actual) {
        this(SymbolFormatter.SIMPLE, expected, actual);
    }

    public SymbolAssertionFailedError(SymbolFormatter formatter, Symbol expected, Symbol actual) {
        super(message(formatter, List.of(expected), List.of(actual)),
                ValueWrapper.create(expected, formatter.format(expected)),
                ValueWrapper.create(actual, formatter.format(actual)));
    }

    public SymbolAssertionFailedError(SymbolFormatter formatter,
                                      List<Symbol> expectedList,
                                      List<Symbol> actualList) {
        super(message(formatter, expectedList, actualList),
                ValueWrapper.create(expectedList,
                        expectedList.stream()
                                .map(formatter::format)
                                .collect(Collectors.joining("\n"))),
                ValueWrapper.create(actualList,
                        actualList.stream()
                                .map(formatter::format)
                                .collect(Collectors.joining("\n"))));
    }

    private static String message(SymbolFormatter formatter, List<Symbol> expected, List<Symbol> actual) {
        SymbolListDiffPrinter diffPrinter = new SymbolListDiffPrinter(formatter);
        return """
                The expected symbol list does not match the actual:
                
                %s
                """.formatted(diffPrinter.printDiff(expected, actual));
    }

}