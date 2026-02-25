package io.github.airbag.token;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class TokenListDiffPrinter {

    private final TokenFormatter formatter;

    public TokenListDiffPrinter(TokenFormatter formatter) {
        this.formatter = formatter;
    }

    public String printDiff(List<? extends Token> expected, List<? extends Token> actual) {
        var fields = formatter.getFields();
        BiPredicate<Token, Token> equalizer = (t1, t2) -> {
            for (var field : fields) {
                if (!Objects.equals(field.access(t1), field.access(t2))) {
                    return false;
                }
            }
            return true;
        };
        List<Token> e = expected.stream().map(Token.class::cast).toList();
        List<Token> a = actual.stream().map(Token.class::cast).toList();
        Patch<Token> diffs = DiffUtils.diff(e,
                a,
                MyersDiff.factory().create(equalizer),
                null,
                true);
        List<TableRow> tableRows = getTableRows(diffs);
        return buildTable(tableRows);
    }

    private String buildTable(List<TableRow> tableRows) {
        return AsciiTable.getTable(AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS,
                tableRows,
                List.of(new Column().header("getTokenIndex")
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

    private static List<TableRow> getTableRows(Patch<Token> diffs) {
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

    private record TableRow(int index, DeltaType delta, Token expected, Token actual) {

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