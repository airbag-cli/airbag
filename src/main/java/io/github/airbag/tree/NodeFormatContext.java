package io.github.airbag.tree;

import io.github.airbag.token.TokenFormatter;
import io.github.airbag.tree.pattern.PatternFormatter;
import org.antlr.v4.runtime.Recognizer;

public class NodeFormatContext {

    //Later final
    private DerivationTree node;
    private final TokenFormatter symbolFormatter;
    private final PatternFormatter patternFormatter;
    private final Recognizer<?,?> recognizer;



    public NodeFormatContext(TokenFormatter symbolFormatter, PatternFormatter patternFormatter, Recognizer<?, ?> recognizer) {
        this.symbolFormatter = symbolFormatter;
        this.patternFormatter = patternFormatter;
        this.recognizer = recognizer;
    }

    public TokenFormatter symbolFormatter() {
        return symbolFormatter;
    }

    public PatternFormatter patternFormatter() {
        return patternFormatter;
    }

    public DerivationTree node() {
        return node;
    }

    public void setNode(DerivationTree node) {
        this.node = node;
    }

    public Recognizer<?, ?> recognizer() {
        return recognizer;
    }

}