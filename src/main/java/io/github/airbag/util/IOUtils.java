package io.github.airbag.util;

import io.github.airbag.token.TokenFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeFormatter;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IOUtils {

    //private constructor
    private IOUtils() {
    }

    public static List<Token> readTokens(Path path, TokenFormatter formatter) throws IOException {
        return formatter.parseList(Files.readString(path));
    }

    public static void writeTokens(Path path, TokenFormatter formatter, List<? extends Token> symbolList) throws IOException {
        writeTokens(path, formatter, symbolList, "%n".formatted());
    }

    public static void writeTokens(Path path, TokenFormatter formatter, List<? extends Token> symbolList, String del) throws IOException {
        Files.writeString(path, formatter.formatList(symbolList, del));
    }

    public static DerivationTree readTree(Path path, TreeFormatter formatter) throws IOException {
        return formatter.parse(Files.readString(path));
    }

    public static void writeTree(Path path, TreeFormatter formatter, DerivationTree tree) throws IOException {
        Files.writeString(path, formatter.format(tree));
    }
}