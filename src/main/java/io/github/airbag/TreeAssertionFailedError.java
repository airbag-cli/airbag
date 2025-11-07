package io.github.airbag;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import com.github.difflib.patch.Patch;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeFormatter;
import io.github.airbag.tree.query.QueryElement;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.List;
import java.util.function.BiPredicate;


public class TreeAssertionFailedError extends AssertionFailedError {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    public TreeAssertionFailedError(DerivationTree expected, DerivationTree actual) {
        this(TreeFormatter.SIMPLE, expected, actual);
    }

    public TreeAssertionFailedError(TreeFormatter formatter,
                                    DerivationTree expected,
                                    DerivationTree actual) {
        super(generateDiff(formatter, expected, actual),
                ValueWrapper.create(expected, formatter.format(expected)),
                ValueWrapper.create(actual, formatter.format(actual)));
    }

    private static String generateDiff(TreeFormatter formatter,
                                       DerivationTree expected,
                                       DerivationTree actual) {
        List<DerivationTree> flatExpected = QueryElement.getDescendants(true, expected);
        List<DerivationTree> flatActual = QueryElement.getDescendants(true, actual);
        BiPredicate<Symbol, Symbol> symbolEqualizer = SymbolField.equalizer(formatter.getSymbolFormatter()
                .getFields());
        Patch<DerivationTree> diffs = DiffUtils.diff(flatExpected,
                flatActual,
                DEFAULT_DIFF.create(equalizer(symbolEqualizer)),
                null,
                true);
        return "";
    }

    private static BiPredicate<DerivationTree, DerivationTree> equalizer(BiPredicate<Symbol, Symbol> symbolEqualizer) {
        return (t1, t2) -> {
            if (t1.index() != t2.index()) {
                return false;
            }
            if (t1 instanceof DerivationTree.Terminal terminal1) {
                if (t2 instanceof DerivationTree.Terminal terminal2) {
                    return symbolEqualizer.test(terminal1.symbol(), terminal2.symbol());
                } else {
                    return false;
                }
            } else if (t1 instanceof DerivationTree.Error error1) {
                if (t2 instanceof DerivationTree.Error error2) {
                    return symbolEqualizer.test(error1.symbol(), error2.symbol());
                } else {
                    return false;
                }
            } else {
                return true;
            }
        };
    }

}