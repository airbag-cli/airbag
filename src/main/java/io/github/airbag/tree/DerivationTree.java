package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public sealed interface DerivationTree permits Node, DerivationTree.Rule, DerivationTree.Terminal, DerivationTree.Error {

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
    default int depth() {
        int depth = 0;
        DerivationTree node = this;
        while (node.getParent() != node) {
            node = node.getParent();
            depth++;
        }
        return depth;
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
                .map(DerivationTree::depth)
                .max(Integer::compareTo)
                .orElse(0);
        return maxDepth - depth();
    }

    sealed interface Rule extends DerivationTree permits Node.Rule {

    }

    sealed interface Terminal extends DerivationTree permits Node.Terminal {

        Symbol symbol();
    }

    sealed interface Error extends DerivationTree permits Node.Error {

        Symbol symbol();
    }
}