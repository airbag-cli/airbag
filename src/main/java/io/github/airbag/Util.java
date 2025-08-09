package io.github.airbag;

/**
 * A utility class for string manipulation.
 * <p>
 * This class provides static methods for escaping and unescaping special characters in strings.
 */
public class Util {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Util() {
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
    public static String Unescape(String txt) {
        txt = txt.replace("\\n", "\n");
        txt = txt.replace("\\r", "\r");
        return txt.replace("\\t", "\t");
    }
}