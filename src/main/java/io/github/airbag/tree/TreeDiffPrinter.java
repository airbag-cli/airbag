package io.github.airbag.tree;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class TreeDiffPrinter {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    private final BiPredicate<DerivationTree, DerivationTree> nodeEqualizer;
    private final TreeFormatter formatter;


    public TreeDiffPrinter(TreeFormatter formatter) {
        var symbolEqualizer = SymbolField.equalizer(formatter.getSymbolFormatter().getFields());
        nodeEqualizer = getNodeEqualizer(symbolEqualizer);
        this.formatter = formatter;
    }

    private BiPredicate<DerivationTree, DerivationTree> getNodeEqualizer(BiPredicate<Symbol, Symbol> symbolEqualizer) {
        return (t1, t2) -> {
            if (!(t1 instanceof DerivationTree.Terminal || t1 instanceof DerivationTree.Error)) {
                return t1.matches(t2);
            } else {
                return t1.matches(t2) && symbolEqualizer.test(getSymbol(t1), getSymbol(t2));
            }
        };
    }

    private Symbol getSymbol(DerivationTree t) {
        return switch (t) {
            case DerivationTree.Terminal terminal -> terminal.symbol();
            case DerivationTree.Error terminal -> terminal.symbol();
            default -> null;
        };
    }

    public String printDiff(DerivationTree expected, DerivationTree actual) {
        Patch<DerivationTree> diffs = DiffUtils.diff(Trees.getDescendants(true, expected),
                Trees.getDescendants(true, actual),
                DEFAULT_DIFF.create(nodeEqualizer),
                null,
                true);
        List<TableRow> tableRows = getTableRows(diffs);
        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                tableRows,
                List.of(new Column().header("index")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(TableRow::numberString),
                        new Column().header("delta")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.CENTER)
                                .with(TableRow::deltaString),
                        new Column().header("expected")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.LEFT)
                                .with(row -> {
                                    if (row.expected() == null) {
                                        return "";
                                    }
                                    String formattedNode = formatter.formatNode(row.expected()).strip();
                                    int depth = row.expected().depth();
                                    return "  ".repeat(depth) + formattedNode;
                                }),
                        new Column().header("actual")
                                .headerAlign(HorizontalAlign.CENTER)
                                .dataAlign(HorizontalAlign.LEFT)
                                .with(row -> {
                                    if (row.actual() == null) {
                                        return "";
                                    }
                                    String formattedNode = formatter.formatNode(row.actual()).strip();
                                    int depth = row.actual().depth();
                                    return "  ".repeat(depth) + formattedNode;
                                })));
    }

    private static List<TableRow> getTableRows(Patch<DerivationTree> diffs) {
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

    private record TableRow(int number, DeltaType deltaType, DerivationTree expected, DerivationTree actual) {

        public String numberString() {
            return String.valueOf(number);
        }

        public String deltaString() {
            return switch (deltaType) {
                case CHANGE -> "~";
                case DELETE -> "-";
                case INSERT -> "+";
                case EQUAL -> " ";
            };
        }
    }
}