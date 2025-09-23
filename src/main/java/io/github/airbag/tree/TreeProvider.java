package io.github.airbag.tree;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

import java.lang.reflect.InvocationTargetException;

public class TreeProvider {

    private final Parser parser;

    private TreeFormatter formatter;

    public TreeProvider(Class<? extends Parser> parserClass) {
        try {
            parser = parserClass.getConstructor(TokenStream.class).newInstance((TokenStream) null);
            formatter = TreeFormatter.SIMPLE.withRecognizer(parser);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to instantiate the provided Parser class. " +
                                               "Ensure it's a valid ANTLR-generated parser with a public constructor accepting a TokenStream.",
                    e);
        }
    }

    public TreeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(TreeFormatter formatter) {
        this.formatter = formatter.withRecognizer(parser);
    }
}