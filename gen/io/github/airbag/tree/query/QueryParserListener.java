// Generated from /home/agoss/Code/airbag/src/main/java/io/github/airbag/tree/query/QueryParser.g4 by ANTLR 4.13.2
package io.github.airbag.tree.query;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QueryParser}.
 */
public interface QueryParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QueryParser#path}.
	 * @param ctx the parse tree
	 */
	void enterPath(QueryParser.PathContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#path}.
	 * @param ctx the parse tree
	 */
	void exitPath(QueryParser.PathContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(QueryParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(QueryParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#navigator}.
	 * @param ctx the parse tree
	 */
	void enterNavigator(QueryParser.NavigatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#navigator}.
	 * @param ctx the parse tree
	 */
	void exitNavigator(QueryParser.NavigatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link QueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(QueryParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link QueryParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(QueryParser.FilterContext ctx);
}