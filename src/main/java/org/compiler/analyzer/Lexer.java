package org.compiler.analyzer;

public class Lexer {
    public static String clean(String input) {
        return input.replaceAll("//.*\\n", "").replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("\\s+", " ").replaceAll("(?<=\\W) | (?=\\W)", "");
    }
}
