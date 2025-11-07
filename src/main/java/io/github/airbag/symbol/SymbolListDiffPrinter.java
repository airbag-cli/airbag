package io.github.airbag.symbol;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import java.util.ArrayList;
import java.util.List;

public class SymbolListDiffPrinter {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    private final SymbolFormatter formatter;

    public SymbolListDiffPrinter(SymbolFormatter formatter) {
        this.formatter = formatter;
    }

    public String printDiff(List<Symbol> expected, List<Symbol> actual) {
        Patch<Symbol> diffs = DiffUtils.diff(expected,
                actual,
                DEFAULT_DIFF.create(SymbolField.equalizer(formatter.getFields())),
                null,
                true);
        List<TableRow> tableRows = getTableRows(diffs);
        return buildTable(tableRows);
    }

    private String buildTable(List<TableRow> tableRows) {
        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                tableRows,
                List.of(new Column().header("index")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(TableRow::indexString),
                        new Column().header("delta")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(TableRow::deltaString),
                        new Column().header("expected")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(row -> row.expected() == null ?
                                        "" :
                                        formatter.format(row.expected())),
                        new Column().header("actual")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(row -> row.actual() == null ?
                                        "" :
                                        formatter.format(row.actual()))));
    }

    private static List<TableRow> getTableRows(Patch<Symbol> diffs) {
        List<TableRow> tableRows = new ArrayList<>();
        for (var patch : diffs.getDeltas()) {
            switch (patch.getType()) {
                case CHANGE -> {
                    var source = patch.getSource();
                    var target = patch.getTarget();
                    int position = Math.min(source.getPosition(), target.getPosition());
                    for (int i = 0; i < Math.max(source.size(), target.size()); i++) {
                        var sourceLine = i < source.size() ? source.getLines().get(i) : null;
                        var targetLine = i < target.size() ? target.getLines().get(i) : null;
                        tableRows.add(new TableRow(position + i,
                                DeltaType.CHANGE,
                                sourceLine,
                                targetLine));
                    }
                }
                case DELETE -> {
                    var source = patch.getSource();
                    int position = source.getPosition();
                    for (int i = 0; i < source.size(); i++) {
                        var sourceLine = source.getLines().get(i);
                        tableRows.add(new TableRow(position + i,
                                DeltaType.DELETE,
                                sourceLine,
                                null));
                    }
                }
                case INSERT -> {
                    var target = patch.getTarget();
                    int position = target.getPosition();
                    for (int i = 0; i < target.size(); i++) {
                        var targetLine = target.getLines().get(i);
                        tableRows.add(new TableRow(position + i,
                                DeltaType.INSERT,
                                null,
                                targetLine));
                    }
                }
                case EQUAL -> {
                    var source = patch.getSource();
                    var target = patch.getTarget();
                    int position = Math.min(source.getPosition(), target.getPosition());
                    for (int i = 0; i < Math.max(source.size(), target.size()); i++) {
                        var sourceLine = source.getLines().get(i);
                        var targetLine = target.getLines().get(i);
                        tableRows.add(new TableRow(position + i,
                                DeltaType.EQUAL,
                                sourceLine,
                                targetLine));
                    }
                }
            }
        }
        return tableRows;
    }

    private record TableRow(int index, DeltaType delta, Symbol expected, Symbol actual) {

        public String indexString() {
            return String.valueOf(index);
        }

        public String deltaString() {
            return switch (delta) {
                case CHANGE -> "~";
                case DELETE -> "-";
                case INSERT -> "+";
                case EQUAL -> " ";
            };
        }

    }
}