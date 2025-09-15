package io.github.airbag.tree;

import io.github.airbag.symbol.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;

/**
 * A concrete syntax tree (CST) is a tree that represents the syntactic structure of a string according to some formal
 * grammar. It is a representation of the source code that is close to the original text, including all the
 * details of the syntax, such as punctuation and whitespace.
 * <p>
 * This sealed class can only be one of three subtypes: {@link Rule}, {@link Terminal}, or {@link Error}.
 */
public sealed class ConcreteSyntaxTree extends AbstractNode<ConcreteSyntaxTree> implements DerivationTree<ConcreteSyntaxTree> permits ConcreteSyntaxTree.Rule, ConcreteSyntaxTree.Terminal, ConcreteSyntaxTree.Error {

    /**
     * Private constructor to enforce creation through the static factory methods.
     *
     * @param parent The parent of this node in the tree.
     * @param index  The index of this node (rule index or token type).
     */
    private ConcreteSyntaxTree(AbstractNode<ConcreteSyntaxTree> parent, int index) {
        super(parent, index);
    }

    /**
     * Creates a {@link ConcreteSyntaxTree} from an ANTLR {@link ParseTree}.
     * This is the main entry point for converting an ANTLR parse result into our CST representation.
     *
     * @param parseTree The root of the ANTLR parse tree.
     * @return The root of our newly created {@link ConcreteSyntaxTree}.
     */
    public static ConcreteSyntaxTree from(ParseTree parseTree) {
        return from(null, parseTree);
    }

    /**
     * Recursively constructs the CST from an ANTLR parse tree.
     *
     * @param parent    The parent {@link Rule} node in our CST.
     * @param parseTree The current ANTLR {@link ParseTree} node to convert.
     * @return The corresponding {@link ConcreteSyntaxTree} node.
     */
    private static ConcreteSyntaxTree from(ConcreteSyntaxTree.Rule parent, ParseTree parseTree) {
        return switch (parseTree) {
            case RuleNode ruleNode -> {
                ConcreteSyntaxTree.Rule rule = ConcreteSyntaxTree.Rule.attachTo(parent, ruleNode.getRuleContext().getRuleIndex());
                for (int i = 0; i < ruleNode.getChildCount(); i++) {
                    from(rule, ruleNode.getChild(i));
                }
                yield rule;
            }
            case ErrorNode errorNode -> Error.attachTo(parent, new Symbol(errorNode.getSymbol()));
            case TerminalNode terminalNode -> Terminal.attachTo(parent, new Symbol(terminalNode.getSymbol()));
            default -> throw new IllegalArgumentException("Unknown type of node %s".formatted(parseTree));
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node<ConcreteSyntaxTree> toNode() {
        return this;
    }

    @Override
    public ConcreteSyntaxTree toTree() {
        return this;
    }

    /**
     * Converts this {@link ConcreteSyntaxTree} back into an ANTLR {@link ParseTree}.
     * This can be useful for interoperability with other tools that expect ANTLR's format.
     *
     * @return The root of the generated ANTLR {@link ParseTree}.
     */
    public ParseTree toParseTree() {
        return toParseTree(null);
    }

    private ParseTree toParseTree(ParserRuleContext parent) {
        return switch (this) {
            case ConcreteSyntaxTree.Rule rule -> {
                ParserRuleContext ruleNode = new ParserRuleContext() {
                    @Override
                    public int getRuleIndex() {
                        return rule.index();
                    }
                };
                ruleNode.setParent(parent);
                if (parent != null) {
                    parent.addChild(ruleNode);
                }
                for (Node<ConcreteSyntaxTree> child : this) {
                    child.toTree().toParseTree(ruleNode);
                }
                yield ruleNode;
            }
            case ConcreteSyntaxTree.Terminal terminal -> {
                TerminalNode terminalNode = new TerminalNodeImpl(terminal.getSymbol().toToken());
                if (parent != null) {
                    parent.addChild(terminalNode);
                }
                terminalNode.setParent(parent);
                yield terminalNode;
            }
            case ConcreteSyntaxTree.Error error -> {
                ErrorNode errorNode = new ErrorNodeImpl(error.getSymbol().toToken());
                if (parent != null) {
                    parent.addChild(errorNode);
                }
                errorNode.setParent(parent);
                yield errorNode;
            }
            default -> throw new IllegalStateException("This should be an exhaustive switch over a sealed class");
        };
    }


    /**
     * Represents a rule node in the CST, corresponding to a non-terminal symbol in the grammar.
     */
    public final static class Rule extends ConcreteSyntaxTree implements Node.Rule<ConcreteSyntaxTree> {

        /**
         * Constructs a new Rule node.
         *
         * @param parent The parent node.
         * @param index  The rule index.
         */
        private Rule(AbstractNode<ConcreteSyntaxTree> parent, int index) {
            super(parent, index);
        }

        /**
         * Creates a new root Rule node.
         *
         * @param index The rule index.
         * @return The new root node.
         */
        public static ConcreteSyntaxTree.Rule root(int index) {
            return new ConcreteSyntaxTree.Rule(null, index);
        }

        /**
         * Creates a new Rule node and attaches it to a parent.
         *
         * @param parent The parent rule node.
         * @param index  The rule index.
         * @return The new attached node.
         */
        public static ConcreteSyntaxTree.Rule attachTo(ConcreteSyntaxTree parent, int index) {
            return new ConcreteSyntaxTree.Rule(parent, index);
        }
    }

    /**
     * Represents a terminal node in the CST, corresponding to a token in the input.
     */
    public final static class Terminal extends ConcreteSyntaxTree implements Node.Terminal<ConcreteSyntaxTree> {

        /**
         * The underlying symbol for this terminal.
         */
        private final Symbol symbol;

        /**
         * Constructs a new Terminal node.
         *
         * @param parent The parent node.
         * @param symbol The symbol for this terminal.
         */
        private Terminal(AbstractNode<ConcreteSyntaxTree> parent, Symbol symbol) {
            super(parent, symbol.type());
            this.symbol = symbol;
        }

        /**
         * Creates a new root Terminal node.
         *
         * @param symbol The symbol.
         * @return The new root node.
         */
        public static ConcreteSyntaxTree.Terminal root(Symbol symbol) {
            return new ConcreteSyntaxTree.Terminal(null, symbol);
        }

        /**
         * Creates a new Terminal node and attaches it to a parent.
         *
         * @param parent The parent rule node.
         * @param symbol The symbol.
         * @return The new attached node.
         */
        public static ConcreteSyntaxTree.Terminal attachTo(ConcreteSyntaxTree parent, Symbol symbol) {
            return new ConcreteSyntaxTree.Terminal(parent, symbol);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Symbol getSymbol() {
            return symbol;
        }
    }

    /**
     * Represents an error node in the CST, indicating a syntax error.
     */
    public final static class Error extends ConcreteSyntaxTree implements Node.Error<ConcreteSyntaxTree> {

        /**
         * The underlying symbol where the error occurred.
         */
        private final Symbol symbol;

        /**
         * Constructs a new Error node.
         *
         * @param parent The parent node.
         * @param symbol The symbol at the error.
         */
        private Error(AbstractNode<ConcreteSyntaxTree> parent, Symbol symbol) {
            super(parent, symbol.type());
            this.symbol = symbol;
        }

        /**
         * Creates a new root Error node.
         *
         * @param symbol The symbol.
         * @return The new root node.
         */
        public static ConcreteSyntaxTree.Error root(Symbol symbol) {
            return new ConcreteSyntaxTree.Error(null, symbol);
        }

        /**
         * Creates a new Error node and attaches it to a parent.
         *
         * @param parent The parent rule node.
         * @param symbol The symbol.
         * @return The new attached node.
         */
        public static ConcreteSyntaxTree.Error attachTo(ConcreteSyntaxTree parent, Symbol symbol) {
            return new ConcreteSyntaxTree.Error(parent, symbol);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Symbol getSymbol() {
            return symbol;
        }
    }
}