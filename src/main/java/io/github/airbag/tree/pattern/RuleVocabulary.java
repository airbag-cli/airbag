package io.github.airbag.tree.pattern;

import org.antlr.v4.runtime.Vocabulary;

/**
 * An implementation of the {@link Vocabulary} interface that is used to represent the rule names of a parser.
 */
public class RuleVocabulary implements Vocabulary {

    private final String[] ruleNames;

    /**
     * Creates a new {@link RuleVocabulary} with the given rule names.
     * @param ruleNames The rule names.
     */
    public RuleVocabulary(String[] ruleNames) {
        this.ruleNames = ruleNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxTokenType() {
        return ruleNames.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLiteralName(int tokenType) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSymbolicName(int tokenType) {
        if (tokenType == -1) {
            return null;
        }
        return tokenType < ruleNames.length ? ruleNames[tokenType] : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(int tokenType) {
        return getSymbolicName(tokenType);
    }
}