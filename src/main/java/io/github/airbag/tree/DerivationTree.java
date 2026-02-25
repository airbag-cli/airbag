package io.github.airbag.tree;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public sealed interface DerivationTree extends Tree permits Node, DerivationTree.Rule, DerivationTree.Terminal, DerivationTree.Error, DerivationTree.Pattern {

    static DerivationTree from(Tree tree) {
        if (tree instanceof DerivationTree derivationTree) {
            return derivationTree;
        } else if (tree instanceof ParseTree parseTree) {
            return from(parseTree);
        } else {
            throw new IllegalArgumentException("Unknown implementation of Tree");
        }
    }

    static DerivationTree from(ParseTree parseTree) {
        return from(null, parseTree);
    }

    static DerivationTree from(DerivationTree parent, ParseTree parseTree) {
        switch (parseTree) {
            case RuleNode ruleNode -> {
                DerivationTree node = Node.Rule.attachTo(parent,
                        ruleNode.getRuleContext().getRuleIndex());
                for (int i = 0; i < ruleNode.getChildCount(); i++) {
                    from(node, ruleNode.getChild(i));
                }
                return node;
            }
            case ErrorNode errorNode -> {
                return Node.Error.attachTo(parent, new CommonToken(errorNode.getSymbol()));
            }
            case TerminalNode terminalNode -> {
                return Node.Terminal.attachTo(parent, new CommonToken(terminalNode.getSymbol()));
            }
            default -> throw new IllegalStateException("Unexpected value: " + parseTree);
        }
    }

    static ParseTree toParseTree(ParserRuleContext parent, DerivationTree d) {
        switch(d) {
            case Error error -> {
                var errorNode = new ErrorNodeImpl(error.symbol());
                parent.addErrorNode(errorNode);
                return errorNode;
            }
            case Pattern pattern -> {
                throw new IllegalArgumentException("Cannot convert pattern nodes");
            }
            case Rule rule -> {
                var ruleNode = new ParserRuleContext(parent, -1) {
                    @Override
                    public int getRuleIndex() {
                        return rule.index();
                    }
                };
                for (var child : d.children()) {
                    toParseTree(ruleNode, child);
                }
                return ruleNode;
            }
            case Terminal terminal -> {
                var terminalNode = new TerminalNodeImpl(terminal.symbol());
                parent.addChild(terminalNode);
                return terminalNode;
            }
        }
    }

    int index();

    DerivationTree getParent();

    List<DerivationTree> children();

    default int size() {
        return children().size();
    }

    default DerivationTree getChild(int i) {
        return children().get(i);
    }

    /**
     * Get the distance to root
     *
     * @return the distance to root
     */
     static int depth(Tree t) {
        int depth = 0;
        Tree node = t;
        while (node != null && node.getParent() != node) {
            node = node.getParent();
            depth++;
        }
        return depth;
    }

    default int depth() {
         return depth(this);
    }



    /**
     * The biggest distance to any subnode
     *
     * @return the biggest distance to any subnode
     */
    default int height() {
        Set<DerivationTree> terminalNodes = new HashSet<>(children());
        while (terminalNodes.stream().anyMatch(node -> node.size() != 0)) {
            for (var node : terminalNodes) {
                if (node.size() != 0) {
                    terminalNodes.remove(node);
                    terminalNodes.addAll(node.children());
                }
            }
        }
        int maxDepth = terminalNodes.stream()
                .map(n -> n.depth())
                .max(Integer::compareTo)
                .orElse(0);
        return maxDepth - depth();
    }

    sealed interface Rule extends DerivationTree permits Node.Rule {

    }

    sealed interface Terminal extends DerivationTree permits Node.Terminal {

        Token symbol();
    }

    sealed interface Error extends DerivationTree permits Node.Error {

        Token symbol();
    }

    sealed interface Pattern extends DerivationTree permits Node.Pattern {

        io.github.airbag.tree.pattern.Pattern getPattern();
    }
}