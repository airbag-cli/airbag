package io.github.airbag.tree;

import java.util.Map;
import java.util.NavigableMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public record TreeParseContext(NavigableMap<Integer, Node<?>> nodeMap, Map<String, BiFunction<? super Node<?>, Object, ? extends Node<?>>> connectors) {
}
