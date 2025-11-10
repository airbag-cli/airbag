package io.github.airbag;

import com.github.difflib.algorithm.DiffAlgorithmFactory;
import com.github.difflib.algorithm.myers.MyersDiff;
import io.github.airbag.symbol.Symbol;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeDiffPrinter;
import io.github.airbag.tree.TreeFormatter;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

import java.util.function.BiPredicate;


public class TreeAssertionFailedError extends AssertionFailedError {

    private static final DiffAlgorithmFactory DEFAULT_DIFF = MyersDiff.factory();

    public TreeAssertionFailedError(DerivationTree expected, DerivationTree actual) {
        this(TreeFormatter.SIMPLE, expected, actual);
    }

    public TreeAssertionFailedError(TreeFormatter formatter,
                                    DerivationTree expected,
                                    DerivationTree actual) {
        super(message(formatter, expected, actual),
                ValueWrapper.create(expected, formatter.format(expected)),
                ValueWrapper.create(actual, formatter.format(actual)));
    }

    private static String message(TreeFormatter formatter, DerivationTree expected, DerivationTree actual) {
        return new TreeDiffPrinter(formatter).printDiff(expected, actual);
    }

}