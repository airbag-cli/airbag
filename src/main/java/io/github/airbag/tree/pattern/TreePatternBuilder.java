package io.github.airbag.tree.pattern;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolField;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.Trees;
import org.antlr.v4.runtime.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

public class TreePatternBuilder {

    private final List<TreePatternElement> treePatternList = new ArrayList<>();

    public TreePattern toPattern() {
        return new TreePattern(new CompositePatternElement(treePatternList.toArray(new TreePatternElement[0])));
    }

    public TreePatternBuilder appendSymbol(Symbol symbol) {
        return appendSymbol(symbol, SymbolField.equalizer(SymbolField.simple()));
    }

    public TreePatternBuilder appendSymbol(Symbol symbol, BiPredicate<Symbol, Symbol> equalizer) {
        treePatternList.add(new SymbolPatternElement(symbol, equalizer));
        return this;
    }

    public TreePatternBuilder appendRuleTag(int ruleIndex) {
        treePatternList.add(new RuleTagPatternElement(ruleIndex));
        return this;
    }

    public TreePatternBuilder appendRuleTag(int ruleIndex, String label) {
        treePatternList.add(new RuleTagPatternElement(ruleIndex, label));
        return this;
    }

    public TreePatternBuilder appendSymbolTag(int symbolIndex) {
        treePatternList.add(new SymbolTagPatternElement(symbolIndex));
        return this;
    }

    public TreePatternBuilder appendSymbolTag(int symbolIndex, String label) {
        treePatternList.add(new SymbolTagPatternElement(symbolIndex, label));
        return this;
    }

    public TreePatternBuilder appendWildcard() {
        treePatternList.add(new WildcardPatternElement());
        return this;
    }

    interface TreePatternElement {

        boolean match(TreePatternContext ctx);

    }

    static class CompositePatternElement implements TreePatternElement {

        private final TreePatternElement[] patternElements;

        public CompositePatternElement(TreePatternElement[] patternElements) {
            this.patternElements = patternElements;
        }

        @Override
        public boolean match(TreePatternContext ctx) {
            var t = ctx.getTree();
            for (int i = 0; i < Math.max(patternElements.length, t.size()); i++) {
                if (i == patternElements.length || i == t.size()) {
                    return false;
                }
                ctx.setTree(t.getChild(i));
                if (!patternElements[i].match(ctx)) {
                    return false;
                }
            }
            return true;
        }

        public TreePatternElement[] elements() {
            return patternElements;
        }
    }

    static class WildcardPatternElement implements TreePatternElement {

        @Override
        public boolean match(TreePatternContext ctx) {
            return true;
        }
    }

    static class SymbolPatternElement implements TreePatternElement {

        private final Symbol symbol;
        private final BiPredicate<Symbol, Symbol> equalizer;

        public SymbolPatternElement(Symbol symbol, BiPredicate<Symbol, Symbol> equalizer) {
            this.symbol = symbol;
            this.equalizer = equalizer;
        }

        @Override
        public boolean match(TreePatternContext ctx) {
            //TODO is this really the best?
            List<Symbol> symbolsList = Trees.getSymbols(ctx.getTree());
            return symbolsList.size() == 1 && equalizer.test(symbolsList.getFirst(), symbol);
        }

        public Symbol getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol.toString();
        }
    }

    static class RuleTagPatternElement implements TreePatternElement {

        private final int ruleIndex;
        private final String tag;

        public RuleTagPatternElement(int ruleIndex) {
            this.ruleIndex = ruleIndex;
            this.tag = null;
        }

        public RuleTagPatternElement(int ruleIndex, String tag) {
            this.ruleIndex = ruleIndex;
            this.tag = tag == null || tag.isEmpty() ? null : tag;
        }

        @Override
        public boolean match(TreePatternContext ctx) {
            boolean result = switch (ctx.getTree()) {
                case DerivationTree.Rule ruleNode -> ruleNode.index() == ruleIndex;
                default -> false;
            };
            if (result && tag != null) {
                ctx.addLabel(tag, ctx.getTree());
            }
            return result;
        }

        public int type() {
            return ruleIndex;
        }

        public String label() {
            return tag;
        }
    }

    static class SymbolTagPatternElement implements TreePatternElement {

        private final SymbolPatternElement symbolPattern;
        private final String tag;

        public SymbolTagPatternElement(int symbolIndex) {
            this(symbolIndex, null);
        }

        public SymbolTagPatternElement(int symbolIndex, String tag) {
            symbolPattern = new SymbolPatternElement(Symbol.of().type(symbolIndex).get(),
                    SymbolField.equalizer(Set.of(SymbolField.TYPE)));
            this.tag = tag == null || tag.isEmpty() ? null : tag;
        }

        @Override
        public boolean match(TreePatternContext ctx) {
            boolean result = symbolPattern.match(ctx);
            if (result && tag != null) {
                ctx.addLabel(tag, ctx.getTree());
            }
            return result;
        }

        public int type() {
            return symbolPattern.getSymbol().type();
        }

        public String label() {
            return tag;
        }
    }
}