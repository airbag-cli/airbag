// Generated from Schema.g4 by ANTLR 4.13.1
package io.github.airbag.gen;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SchemaParser}.
 */
public interface SchemaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SchemaParser#schema}.
	 * @param ctx the parse tree
	 */
	void enterSchema(SchemaParser.SchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#schema}.
	 * @param ctx the parse tree
	 */
	void exitSchema(SchemaParser.SchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#tokenList}.
	 * @param ctx the parse tree
	 */
	void enterTokenList(SchemaParser.TokenListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#tokenList}.
	 * @param ctx the parse tree
	 */
	void exitTokenList(SchemaParser.TokenListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#node}.
	 * @param ctx the parse tree
	 */
	void enterNode(SchemaParser.NodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#node}.
	 * @param ctx the parse tree
	 */
	void exitNode(SchemaParser.NodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#rule}.
	 * @param ctx the parse tree
	 */
	void enterRule(SchemaParser.RuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#rule}.
	 * @param ctx the parse tree
	 */
	void exitRule(SchemaParser.RuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#token}.
	 * @param ctx the parse tree
	 */
	void enterToken(SchemaParser.TokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#token}.
	 * @param ctx the parse tree
	 */
	void exitToken(SchemaParser.TokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link SchemaParser#error}.
	 * @param ctx the parse tree
	 */
	void enterError(SchemaParser.ErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SchemaParser#error}.
	 * @param ctx the parse tree
	 */
	void exitError(SchemaParser.ErrorContext ctx);
}