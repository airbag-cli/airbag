package io.github.airbag.token;

/**
 * Specifies the different ways to format and parse a symbol.
 */
public enum TypeFormat {

    /**
     * Indicates that only integers are parsed and formatted as types.
     */
    INTEGER_ONLY,

    /**
     * A strict version that only tries to parse symbolic types.
     */
    SYMBOLIC_ONLY,

    /**
     * A strict version that only tries to parse literal types.
     */
    LITERAL_ONLY,

    /**
     * Tries to parse as symbolic first, then literal, and finally integer.
     */
    SYMBOLIC_FIRST,

    /**
     * Tries to parse as literal first, then symbolic, and finally integer.
     */
    LITERAL_FIRST;
}