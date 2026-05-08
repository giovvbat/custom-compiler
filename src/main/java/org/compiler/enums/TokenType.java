package org.compiler.enums;

// adicionar restante dos regex
public enum TokenType {
    IDENTIFIER("[a-zA-Z]\\w*");
    // DOUBLE_TYPE,
    // CHAR_TYPE,
    // BOOLEAN_TYPE,
    // STRING_TYPE,
    // DOUBLE_VALUE,
    // CHAR_VALUE,
    // BOOLEAN_VALUE,
    // STRING_VALUE,
    // PLUS,
    // EQUALS,
    // CMP,
    // FOR,
    // IF,
    // EOF;

    private final String regex;

    TokenType(String s) {
        regex = s;
    }
}
