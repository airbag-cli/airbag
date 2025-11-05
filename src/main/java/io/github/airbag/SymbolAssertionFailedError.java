package io.github.airbag;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SymbolAssertionFailedError extends AssertionFailedError {

    public SymbolAssertionFailedError(Symbol expected, Symbol actual) {
        super(generateDiff(expected.toString(), actual.toString()), expected, actual);
    }

    public SymbolAssertionFailedError(SymbolFormatter formatter, Symbol expected, Symbol actual) {
        super(generateDiff(formatter.format(expected), formatter.format(actual)),
                ValueWrapper.create(expected, formatter.format(expected)),
                ValueWrapper.create(actual, formatter.format(actual)));
    }

    public SymbolAssertionFailedError(SymbolFormatter formatter,
                                      List<Symbol> expectedList,
                                      List<Symbol> actualList) {
        super(generateDiff(
                        expectedList.stream().map(formatter::format).collect(Collectors.toList()),
                        actualList.stream().map(formatter::format).collect(Collectors.toList())),
                ValueWrapper.create(expectedList,
                        expectedList.stream().map(formatter::format).collect(
                                Collectors.joining("\n"))),
                ValueWrapper.create(actualList,
                        actualList.stream().map(formatter::format).collect(
                                Collectors.joining("\n"))));
    }

    private static String generateDiff(String expected, String actual) {
        return generateDiff(Arrays.asList(expected.split("\n")), Arrays.asList(actual.split("\n")));
    }

    private static String generateDiff(List<String> expected, List<String> actual) {
        DiffRowGenerator generator = DiffRowGenerator.create()
                .build();
        List<DiffRow> rows = generator.generateDiffRows(expected, actual);

        int maxOldWidth = Math.max("Expected".length(), rows.stream().map(DiffRow::getOldLine).mapToInt(String::length).max().orElse(0));
        int maxNewWidth = Math.max("Actual".length(), rows.stream().map(DiffRow::getNewLine).mapToInt(String::length).max().orElse(0));

        StringBuilder table = buildTable(maxOldWidth, maxNewWidth, rows);

        return table.toString();
    }

    private static StringBuilder buildTable(int maxOldWidth,
                                                  int maxNewWidth,
                                                  List<DiffRow> rows) {
        StringBuilder table = new StringBuilder();table.append("%n".formatted());
        String border = "+-" + "-".repeat(maxOldWidth) + "-+-" + "-".repeat(maxNewWidth) + "-+\n";

        table.append(border);
        table.append(String.format("| %-" + maxOldWidth + "s | %-" + maxNewWidth + "s |\n", "Expected", "Actual"));
        table.append(border);

        for (DiffRow row : rows) {
            table.append(String.format("| %-" + maxOldWidth + "s | %-" + maxNewWidth + "s |\n", row.getOldLine(), row.getNewLine()));
        }

        table.append(border);
        return table;
    }
}