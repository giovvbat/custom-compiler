package org.compiler.enums;

import org.compiler.domain.Symbol;

import java.util.regex.Pattern;

public enum TerminalSymbol implements Symbol {
    // keywords
    CLASS("class\\b"),
    PUBLIC("public\\b"),
    STATIC_VOID_MAIN("static void main\\b"),
    STRING("String\\b"),
    RETURN("return\\b"),
    INT_TYPE("int\\b"),
    BOOLEAN_TYPE("boolean\\b"),
    IF("if\\b"),
    ELSE("else\\b"),
    WHILE("while\\b"),
    SYSTEM_OUT_PRINTLN("System\\.out\\.println\\b"),
    NEW("new\\b"),
    TRUE("true\\b"),
    FALSE("false\\b"),
    THIS("this\\b"),
    LENGTH("length\\b"),
    EXTENDS("extends\\b"),

    PAREN_LEFT("\\("),
    PAREN_RIGHT("\\)"),
    CURLY_BRACKET_LEFT("\\{"),
    CURLY_BRACKET_RIGHT("\\}"),
    SQUARE_BRACKET_LEFT("\\["),
    SQUARE_BRACKET_RIGHT("\\]"),
    SEMI_COLON(";"),
    COMMA(","),
    EQUALS("="),
    AND("&&"),
    GREATER(">"),
    LESS("<"),
    PLUS("\\+"),
    MINUS("-"),
    MULTIPLY("\\*"),
    DOT("\\."),
    NOT("!"),

    NUMBER("[0-9]+"),

    ID("[a-zA-Z]\\w*");

    public final String regex;
    public final Pattern pattern;

    TerminalSymbol(String rule) {
        this.regex = rule;
        this.pattern = Pattern.compile(rule);
    }
}
