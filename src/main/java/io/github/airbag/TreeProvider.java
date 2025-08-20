package io.github.airbag;

import io.github.airbag.gen.ValidationTreeBaseVisitor;
import io.github.airbag.gen.ValidationTreeLexer;
import io.github.airbag.gen.ValidationTreeParser;
import io.github.airbag.gen.ValidationTreeVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Provides mechanisms to construct ANTLR {@link ParseTree} and {@link ValidationTree} instances.
 * <p>
 * This class serves two main purposes:
 * <ol>
 *   <li>Generating a standard ANTLR {@link ParseTree} from a source of tokens, given a specific parser class and a starting rule.
 *       This is useful for creating parse trees programmatically from token streams. See {@link #fromSource(TokenSource, String)}.</li>
 *   <li>Constructing a {@link ValidationTree}, which is a custom tree structure used for validation, from a string-based specification.
 *       This allows for defining expected tree structures in a concise, human-readable format. See {@link #fromSpec(String)}.</li>
 * </ol>
 * The provider is initialized with a specific ANTLR-generated {@link Parser} class, which provides the necessary vocabulary
 * and rule context for creating these trees.
 */
public class TreeProvider {

    /**
     * The ANTLR parser instance used to resolve rule indices and token types.
     */
    private final Parser parser;

    /**
     * Constructs a TreeProvider for a specific ANTLR parser.
     *
     * @param parserClass The class of the ANTLR-generated parser (e.g., {@code MyParser.class}).
     *                    This parser's vocabulary and rule definitions are used for tree construction.
     * @throws RuntimeException if the parser cannot be instantiated via reflection.
     */
    public TreeProvider(Class<? extends Parser> parserClass) {
        try {
            parser = parserClass.getConstructor(TokenStream.class).newInstance((TokenStream) null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link ParseTree} from a {@link TokenSource} by invoking a specific parser rule.
     * <p>
     * This method uses reflection to find and execute the parser rule method (e.g., {@code myRule()}) on the configured parser instance.
     *
     * @param source The source of tokens to be parsed.
     * @param rule   The name of the parser rule to use as the entry point for parsing (must match a method name in the parser class).
     * @return The resulting {@link ParseTree}.
     * @throws RuntimeException if the specified rule method cannot be found or invoked.
     */
    public ParseTree fromSource(TokenSource source, String rule) {
        parser.setTokenStream(new CommonTokenStream(source));
        try {
            Method ruleMethod = parser.getClass().getMethod(rule);
            return (ParseTree) ruleMethod.invoke(parser);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A convenience method that creates a {@link ParseTree} from a list of {@link Token}s.
     *
     * @param listSource A list of tokens that will be wrapped in a {@link ListTokenSource}.
     * @param rule       The name of the parser rule to use as the entry point.
     * @return The resulting {@link ParseTree}.
     * @see #fromSource(TokenSource, String)
     */
    public ParseTree fromSource(List<? extends Token> listSource, String rule) {
        return fromSource(new ListTokenSource(listSource), rule);
    }

    /**
     * Parses a string specification and builds a {@link ValidationTree}.
     * <p>
     * The specification uses a Lisp-like S-expression syntax, defined in {@code ValidationTree.g4},
     * to represent the desired tree structure.
     *
     * <h3>Syntax</h3>
     * <ul>
     *   <li><b>Rule Node:</b> A rule is represented by its name (lowercase) followed by its children, all enclosed in parentheses.
     *       <br>Format: {@code (ruleName child1 child2 ...)}</li>
     *   <li><b>Token Node:</b> A token can be written in two ways:
     *     <ul>
     *       <li><b>Full Form:</b> The token type (uppercase) and its text (in single quotes), enclosed in parentheses.
     *           <br>Format: {@code (TOKEN_TYPE 'text')}</li>
     *       <li><b>Shorthand:</b> Just the token text in single quotes. This is useful for keywords or operators where the text
     *           is self-evident.
     *           <br>Format: {@code 'text'}</li>
     *     </ul>
     *   </li>
     *   <li><b>Error Node:</b> An error is indicated by wrapping a token node within an {@code <error>} tag.
     *       <br>Format: {@code (<error> tokenNode)}</li>
     *   <li><b>EOF Node:</b> The end-of-file is represented by the literal {@code EOF}.</li>
     * </ul>
     *
     * <h3>Example</h3>
     * <p>
     * Given a grammar for simple expressions like in {@code Expression.g4}:
     * <pre>
     * grammar Expression;
     * stat: expr NEWLINE | ID '=' expr NEWLINE;
     * expr: INT;
     * ...
     * </pre>
     * A statement like {@code x = 42\n} would be parsed into a tree. The specification for this tree would be:
     * <pre>
     * (stat (ID 'x') '=' (expr (INT '42')) (NEWLINE '\n'))
     * </pre>
     *
     * @param input The string containing the tree specification.
     * @return The root of the constructed {@link ValidationTree}.
     */
    public ValidationTree fromSpec(String input) {
        ValidationTreeLexer validationTreeLexer = new ValidationTreeLexer(CharStreams.fromString(input));
        ValidationTreeParser validationTreeParser = new ValidationTreeParser(new CommonTokenStream(validationTreeLexer));
        ValidationTreeVisitor<ValidationTree> visitor = new ValidationTreeBaseVisitor<>() {

            ValidationTree currentNode;
            int tokenCount = 0;

            @Override
            public ValidationTree visitValidationTree(ValidationTreeParser.ValidationTreeContext ctx) {
                // The root of the specification is the single node it contains.
                return visitNode(ctx.node());
            }

            @Override
            public ValidationTree visitRule(ValidationTreeParser.RuleContext ctx) {
                // When a rule is visited, create a new RuleValidationNode and make it the current node.
                currentNode = RuleValidationNode.attachTo(currentNode, parser.getRuleIndex(ctx.RULE().getText()));
                // Visit all children of this rule, which will be attached to the new current node.
                for (var child : ctx.node()) {
                    visitNode(child);
                }
                // After visiting all children, move back up to the parent to continue building the tree at the correct level.
                currentNode = currentNode.getParent();
                return currentNode.isRoot() ? currentNode :  currentNode.getChild(currentNode.getChildCount() - 1);
            }

            @Override
            public ValidationTree visitToken(ValidationTreeParser.TokenContext ctx) {
                // When a token is visited, construct it and attach it as a terminal node.
                Token token = constructToken(ctx);
                return TerminalValidationNode.attachTo(currentNode, token);
            }

            @Override
            public ValidationTree visitError(ValidationTreeParser.ErrorContext ctx) {
                // When an error node is visited, construct the underlying token and attach it as an error node.
                Token token = constructToken(ctx.token());
                return ErrorValidationNode.attachTo(currentNode, token);
            }

            private Token constructToken(ValidationTreeParser.TokenContext tokenCtx) {
                if (tokenCtx.EOF_KEYWORD() != null) {
                    var eof = new CommonToken(Token.EOF, "<EOF>");
                    eof.setTokenIndex(tokenCount++);
                    return eof;
                }
                String typeString;
                String text;
                if (tokenCtx.TOKEN() == null) {
                    // This is a shorthand token, like ';'
                    typeString = tokenCtx.STRING().getText();
                    text = unquote(typeString);
                } else {
                    // This is a full token, like (ID 'myVar')
                    typeString = tokenCtx.TOKEN().getText();
                    text = unquote(tokenCtx.STRING().getText());
                }
                var token = new CommonToken(Tokens.getTokenType(typeString, parser.getVocabulary()),
                        text);
                token.setTokenIndex(tokenCount++);
                return token;
            }

            private String unquote(String s) {
                if (s != null && s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) {
                    return s.substring(1, s.length() - 1);
                }
                return s;
            }
        };
        return visitor.visitValidationTree(validationTreeParser.validationTree());
    }
}
