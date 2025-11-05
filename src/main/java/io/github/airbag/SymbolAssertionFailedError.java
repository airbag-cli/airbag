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
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class SymbolAssertionFailedError extends AssertionFailedError {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    public SymbolAssertionFailedError(Symbol expected, Symbol actual) {
        super(generateDiff(SymbolFormatter.ANTLR, List.of(expected), List.of(actual)),
                expected,
                actual);
    }

    public SymbolAssertionFailedError(SymbolFormatter formatter, Symbol expected, Symbol actual) {
        super(generateDiff(formatter, List.of(expected), List.of(actual)),
                ValueWrapper.create(expected, formatter.format(expected)),
                ValueWrapper.create(actual, formatter.format(actual)));
    }


    public SymbolAssertionFailedError(SymbolFormatter formatter,
                                      List<Symbol> expectedList,
                                      List<Symbol> actualList) {
        super(generateDiff(formatter, expectedList, actualList),
                ValueWrapper.create(expectedList,
                        expectedList.stream()
                                .map(formatter::format)
                                .collect(Collectors.joining("\n"))),
                ValueWrapper.create(actualList,
                        actualList.stream()
                                .map(formatter::format)
                                .collect(Collectors.joining("\n"))));
    }

    private static String generateDiff(SymbolFormatter formatter,
                                       List<Symbol> expected,
                                       List<Symbol> actual) {
        BiPredicate<Symbol, Symbol> equalizer = SymbolField.equalizer(formatter.getFields());
        Patch<Symbol> diffs = DiffUtils.diff(expected,
                actual,
                DEFAULT_DIFF.create(equalizer),
                null,
                true);
        int expectedWidth = Math.max("Expected".length(),
                expected.stream().map(formatter::format).mapToInt(String::length).max().orElse(0));
        int actualWidth = Math.max("Actual".length(),
                actual.stream().map(formatter::format).mapToInt(String::length).max().orElse(0));
        StringBuilder table = new StringBuilder();
        int indexWidth = ((Double) Math.ceil(Math.log10(Math.max(expected.size(),
                actual.size())))).intValue();
        indexWidth = Math.max(indexWidth, 1);
        String border = "+-" + "-".repeat(indexWidth) + "-+---+-" +
                        "-".repeat(expectedWidth) +
                        "-+-" +
                        "-".repeat(actualWidth) +
                        "-+";
        String newLine = "%n".formatted();
        table.append("The expected symbol list does not match the actual:");
        table.append(newLine).append(newLine).append(border).append(newLine);
        table.append(String.format("| %-" + indexWidth + "s | d | %-" +
                                   expectedWidth +
                                   "s | %-" +
                                   actualWidth +
                                   "s |%n",
                "i",
                "Expected",
                "Actual"));
        table.append(border).append(newLine);
        for (var delta : diffs.getDeltas()) {
            String line = "| %%-%dd | %%s | %%-%ds | %%-%ds |%n".formatted(indexWidth ,expectedWidth, actualWidth);
            switch (delta.getType()) {
                case CHANGE -> {
                    for (int i = 0; i <
                                    Math.max(delta.getSource().size(),
                                            delta.getTarget().size()); i++) {
                        Symbol source = i < delta.getSource().size() ?
                                delta.getSource().getLines().get(i) :
                                null;
                        Symbol target = i < delta.getTarget().size() ?
                                delta.getTarget().getLines().get(i) :
                                null;
                        table.append(line.formatted(delta.getSource().getPosition() + i,
                                "~",
                                source == null ? "" : formatter.format(source),
                                target == null ? "" : formatter.format(target)));
                    }
                }
                case DELETE -> {
                    var lines = delta.getSource().getLines();
                    for (int i = 0; i < lines.size(); i++) {
                        table.append(line.formatted(delta.getSource().getPosition() + i,
                                "-",
                                formatter.format(lines.get(i)),
                                ""));
                    }
                }
                case INSERT -> {
                    var lines = delta.getTarget().getLines();
                    int position = delta.getTarget().getPosition();
                    for (int i = 0; i < lines.size(); i++) {
                        table.append(line.formatted(position + i,
                                "+",
                                "",
                                formatter.format(lines.get(i))));
                    }
                }
                case EQUAL -> {
                    var lines = delta.getSource().getLines();
                    int position = delta.getSource().getPosition();
                    for (int i = 0; i < lines.size(); i++) {
                        table.append(line.formatted(position + i,
                                " ",
                                formatter.format(lines.get(i)),
                                formatter.format(lines.get(i))));
                    }
                }
            }
        }
        table.append(border).append(newLine);
        return table.toString();
    }

}