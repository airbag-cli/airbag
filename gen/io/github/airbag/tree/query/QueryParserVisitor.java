// Generated from /home/agoss/Code/airbag/src/main/java/io/github/airbag/tree/query/QueryParser.g4 by ANTLR 4.13.2
package io.github.airbag.tree.query;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link QueryParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface QueryParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link QueryParser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(QueryParser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link QueryParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(QueryParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link QueryParser#navigator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNavigator(QueryParser.NavigatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link QueryParser#filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter(QueryParser.FilterContext ctx);
}