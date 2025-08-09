// Generated from Schema.g4 by ANTLR 4.13.1
package io.github.airbag.gen;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SchemaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SchemaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SchemaParser#schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema(SchemaParser.SchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link SchemaParser#tokenList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTokenList(SchemaParser.TokenListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SchemaParser#node}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNode(SchemaParser.NodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SchemaParser#rule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRule(SchemaParser.RuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link SchemaParser#token}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToken(SchemaParser.TokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link SchemaParser#error}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitError(SchemaParser.ErrorContext ctx);
}