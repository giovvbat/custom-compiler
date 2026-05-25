package org.compiler.analyzer;

import org.compiler.domain.Token;
import org.compiler.enums.TerminalSymbol;

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

            if (position == input.length()) {
                return tokens;
            }

            boolean match = false;

            for (TerminalSymbol type : TerminalSymbol.values()) {
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
                throw new RuntimeException("lexical error: invalid character found in input at position " + position + " {" + input.charAt(position) + "}");
            }
        }

        return tokens;
    }
}
