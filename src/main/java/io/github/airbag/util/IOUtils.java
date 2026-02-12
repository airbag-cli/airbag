package io.github.airbag.util;

import io.github.airbag.symbol.Symbol;
import io.github.airbag.symbol.SymbolFormatter;
import io.github.airbag.tree.DerivationTree;
import io.github.airbag.tree.TreeFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IOUtils {

    //private constructor
    private IOUtils() {
    }

    public static List<Symbol> readSymbolFile(Path path, SymbolFormatter formatter) throws IOException {
        return formatter.parseList(Files.readString(path));
    }

    public static void writeSymbolFile(Path path, SymbolFormatter formatter, List<Symbol> symbolList) throws IOException {
        writeSymbolFile(path, formatter, symbolList, "%n".formatted());
    }

    public static void writeSymbolFile(Path path, SymbolFormatter formatter, List<Symbol> symbolList, String del) throws IOException {
        Files.writeString(path, formatter.formatList(symbolList, del));
    }

    public static DerivationTree readTreeFile(Path path, TreeFormatter formatter) throws IOException {
        return formatter.parse(Files.readString(path));
    }

    public static void writeTreeFile(Path path, TreeFormatter formatter, DerivationTree tree) throws IOException {
        Files.writeString(path, formatter.format(tree));
    }
}