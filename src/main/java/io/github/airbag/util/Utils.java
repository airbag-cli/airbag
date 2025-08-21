package io.github.airbag.util;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

/**
 * A utility class for string manipulation.
 * <p>
 * This class provides static methods for escaping and unescaping special characters in strings.
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utils() {
    }

    /**
     * Escapes special characters in a string.
     * <p>
     * This method replaces newline (\n), carriage return (\r), and tab (\t)
     * characters with their escaped string representations.
     *
     * @param txt The string to escape.
     * @return The escaped string.
     */
    public static String escape(String txt) {
        txt = txt.replace("\n", "\\n");
        txt = txt.replace("\r", "\\r");
        return txt.replace("\t", "\\t");
    }

    /**
     * Unescapes special characters in a string.
     * <p>
     * This method replaces the escaped string representations of newline (\n),
     * carriage return (\r), and tab (\t) with their actual characters.
     *
     * @param txt The string to unescape.
     * @return The unescaped string.
     */
    public static String unescape(String txt) {
        txt = txt.replace("\\n", "\n");
        txt = txt.replace("\\r", "\r");
        return txt.replace("\\t", "\t");
    }

    /**
     * Checks if two lists are equal using a custom predicate.
     *
     * @param list1 the first list.
     * @param list2 the second list.
     * @param predicate the predicate to use for comparing elements.
     * @param <T> the type of the elements in the lists.
     * @return {@code true} if the lists are equal, {@code false} otherwise.
     */
    public static <T> boolean listEquals(List<? extends T> list1, List<? extends T> list2, BiPredicate<? super T, ? super T> predicate) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }
        return IntStream.range(0, list1.size())
                .allMatch(i -> predicate.test(list1.get(i), list2.get(i)));
    }
}