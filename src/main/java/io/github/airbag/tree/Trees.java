package io.github.airbag.tree;

import io.github.airbag.token.Tokens;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Trees {

    private Trees() {
    }

    public static String format(ParseTree parserTree, Recognizer<?, ?> recognizer) {
        switch(parserTree) {
            case RuleNode ruleNode -> {
                StringBuilder sb = new StringBuilder();
                sb.append("(%s".formatted(recognizer.getRuleNames()[ruleNode.getRuleContext().getRuleIndex()]));
                for (int i = 0; i < ruleNode.getChildCount(); i++) {
                    sb.append(" %s".formatted(format(ruleNode.getChild(i), recognizer)));
                }
                sb.append(")");
                return sb.toString();
            }
            case ErrorNode errorNode -> {
                return "(<error> %s)".formatted(Tokens.format(errorNode.getSymbol(),
                        recognizer.getVocabulary()));
            }
            case TerminalNode terminalNode -> {
                return Tokens.format(terminalNode.getSymbol(), recognizer.getVocabulary());
            }
            case null, default -> throw new RuntimeException();
        }
    }

    public static String format(ValidationTree validationTree, Recognizer<?, ?> recognizer) {
        switch(validationTree) {
            case RuleValidationNode ruleNode -> {
                StringBuilder sb = new StringBuilder();
                sb.append("(%s".formatted(recognizer.getRuleNames()[ruleNode.getPayload()]));
                for (int i = 0; i < ruleNode.getChildCount(); i++) {
                    sb.append(" %s".formatted(format(ruleNode.getChild(i), recognizer)));
                }
                sb.append(")");
                return sb.toString();
            }
            case ErrorValidationNode errorNode -> {
                return "(<error> %s)".formatted(Tokens.format(errorNode.getPayload(),
                        recognizer.getVocabulary()));
            }
            case TerminalValidationNode terminalNode -> {
                return Tokens.format(terminalNode.getPayload(), recognizer.getVocabulary());
            }
            case null, default -> throw new RuntimeException();
        }
    }
}
