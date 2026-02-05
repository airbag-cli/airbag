package io.github.airbag.util;

import java.util.Arrays;
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
     * Checks if two lists are equal using a custom predicate.
     *
     * @param list1     the first list.
     * @param list2     the second list.
     * @param predicate the predicate to use for comparing elements.
     * @param <T>       the type of the elements in the lists.
     * @return {@code true} if the lists are equal, {@code false} otherwise.
     */
    public static <T> boolean listEquals(List<? extends T> list1,
                                         List<? extends T> list2,
                                         BiPredicate<? super T, ? super T> predicate) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }
        return IntStream.range(0, list1.size())
                .allMatch(i -> predicate.test(list1.get(i), list2.get(i)));
    }

    /**
     * Concat two arrays.
     *
     * @param array1 the first array
     * @param array2 the second array
     * @param <T>    the type of the array.
     * @return an array that is the concatenation of the input arrays.
     */
    public static <T> T[] concat(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}