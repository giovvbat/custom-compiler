package org.compiler.analyzer;

import org.compiler.domain.Token;
import org.compiler.enums.TokenType;
import org.compiler.exception.LexicalErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Lexer {
    public static String clean(String input) {
        return input.replaceAll("//.*\\n", "").replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("\\s+", " ").replaceAll("(?<=\\W) | (?=\\W)", "");
    }

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
                position++;
            }

            boolean match = false;

            for (TokenType type : TokenType.values()) {
                Matcher matcher = type.pattern.matcher(input).region(position, input.length());

                if (matcher.lookingAt()) {
                    String found = matcher.group();

                    position += found.length();
                    match = true;

                    tokens.add(new Token(found, type));

                    break;
                }
            }

            if (!match) {
                throw new LexicalErrorException("invalid character '" + input.charAt(position) + "' caused a lexical error");
            }
        }

        return tokens;
    }
}
