package io.github.airbag.tree;

import java.util.NavigableMap;

public record TreeFormatContext(NavigableMap<Integer, Node<?>> nodeMap) {
}
