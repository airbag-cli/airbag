package io.github.airbag.tree;


import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.Tree;

import java.util.Objects;
import java.util.function.BiPredicate;

public class Validator {

    private BiPredicate<Token, Token> equalizer;

    public Validator(BiPredicate<Token, Token> equalizer) {
        this.equalizer = Objects.requireNonNull(equalizer);
    }

    public boolean validate(Tree t1, Tree t2) {
        return validate(DerivationTree.from(t1), DerivationTree.from(t2));
    }

    public boolean validate(DerivationTree t1, DerivationTree t2) {
        return switch (t1) {
            case DerivationTree.Rule ruleNode -> {
                if (t2 instanceof DerivationTree.Rule otherRule) {
                    if (ruleNode.index() != otherRule.index()) {
                        yield false;
                    }
                    if (ruleNode.size() != otherRule.size()) {
                        yield false;
                    }
                    for (int i = 0; i < ruleNode.size(); i++) {
                        if (!validate(ruleNode.getChild(i), otherRule.getChild(i))) {
                            yield false;
                        }
                    }
                    yield true;
                } else {
                    yield false;
                }
            }
            case DerivationTree.Terminal terminalNode -> {
                if (t2 instanceof DerivationTree.Terminal otherTerminal) {
                    yield equalizer.test(terminalNode.symbol(), otherTerminal.symbol());
                } else {
                    yield false;
                }
            }
            case DerivationTree.Error errorNode -> {
                if (t2 instanceof DerivationTree.Error otherError) {
                    yield equalizer.test(errorNode.symbol(), otherError.symbol());
                } else {
                    yield false;
                }
            }
            case DerivationTree.Pattern patternNode -> {
                if (t2 instanceof DerivationTree.Rule otherRule) {
                    yield otherRule.index() == patternNode.index() &&
                          patternNode.getPattern().isMatch(otherRule);
                } else {
                    throw new RuntimeException("Pattern must match a rule node");
                }
            }
            case null -> throw new IllegalArgumentException();
        };
    }

    public BiPredicate<Token, Token> getEqualizer() {
        return equalizer;
    }

    public void setEqualizer(BiPredicate<Token, Token> equalizer) {
        this.equalizer = Objects.requireNonNull(equalizer);
    }
}