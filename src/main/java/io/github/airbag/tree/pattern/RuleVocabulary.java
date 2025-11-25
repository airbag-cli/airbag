package io.github.airbag.tree.pattern;

import org.antlr.v4.runtime.Vocabulary;

public class RuleVocabulary implements Vocabulary {

    private final String[] ruleNames;
    public RuleVocabulary(String[] ruleNames) {
        this.ruleNames = ruleNames;
    }

    @Override
    public int getMaxTokenType() {
        return ruleNames.length;
    }

    @Override
    public String getLiteralName(int tokenType) {
        return null;
    }

    @Override
    public String getSymbolicName(int tokenType) {
        if (tokenType == -1) {
            return null;
        }
        return tokenType < ruleNames.length ? ruleNames[tokenType] : null;
    }

    @Override
    public String getDisplayName(int tokenType) {
        return getSymbolicName(tokenType);
    }
}