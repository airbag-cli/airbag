package io.github.airbag.tree;

/**
 * A validation node that represents a rule in the parse tree.
 * <p>
 * This validation node is used to match against a {@link org.antlr.v4.runtime.tree.RuleNode} in the parse tree.
 */
public class RuleValidationNode extends AbstractValidationNode {

    /**
     * The index of the rule this schema node represents.
     */
    private final int ruleIndex;

    /**
     * Constructs a new RuleValidationNode with the specified rule index.
     * This constructor is private to enforce the use of the static factory methods.
     *
     * @param ruleIndex The index of the rule this schema node represents.
     */
    private RuleValidationNode(int ruleIndex) {
        this(null, ruleIndex);
    }

    /**
     * Constructs a new RuleValidationNode with the specified parent and rule index.
     * This constructor is private to enforce the use of the static factory methods.
     *
     * @param parent    The parent of this schema node.
     * @param ruleIndex The index of the rule this schema node represents.
     */
    private RuleValidationNode(ValidationTree parent, int ruleIndex) {
        super(parent);
        this.ruleIndex = ruleIndex;
    }

    /**
     * Creates a new root RuleValidationNode with the specified rule index.
     *
     * @param ruleIndex The index of the rule this schema node represents.
     * @return The new RuleValidationNode.
     */
    public static RuleValidationNode root(int ruleIndex) {
        return new RuleValidationNode(ruleIndex);
    }

    /**
     * Creates a new RuleValidationNode with the specified parent and rule index.
     *
     * @param parent    The parent of this schema node.
     * @param ruleIndex The index of the rule this schema node represents.
     * @return The new RuleValidationNode.
     */
    public static RuleValidationNode attachTo(ValidationTree parent, int ruleIndex) {
        return new RuleValidationNode(parent, ruleIndex);
    }


    /**
     * Returns the index of the rule this schema node represents.
     *
     * @return The index of the rule this schema node represents.
     */
    @Override
    public Integer getPayload() {
        return ruleIndex;
    }
}
