package org.compiler.analyzer;

import org.compiler.domain.Token;
import org.compiler.enums.TerminalSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Lexer {
    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();

        int position = 0;
        int line = 1;
        int column = 1;

        while (position < input.length()) {
            boolean skipped = true;

            while (skipped && position < input.length()) {
                skipped = false;

                char character = input.charAt(position);

                if (Character.isWhitespace(character)) {
                    if (character == '\n') {
                        line++;
                        column = 1;
                    } else {
                        column++;
                    }

                    position++;
                    skipped = true;

                    continue;
                }

                if (input.startsWith("//", position)) {
                    while (position < input.length() && input.charAt(position) != '\n') {
                        position++;
                        column++;
                    }

                    skipped = true;

                    continue;
                }

                if (input.startsWith("/*", position)) {
                    position += 2;
                    column += 2;

                    while (position < input.length() && !input.startsWith("*/", position)) {
                        if (input.charAt(position) == '\n') {
                            line++;
                            column = 1;
                        } else {
                            column++;
                        }

                        position++;
                    }

                    if (position < input.length()) {
                        position += 2;
                        column += 2;
                    }

                    skipped = true;
                }
            }

            if (position == input.length()) {
                return tokens;
            }

            boolean match = false;

            int tokenLine = line;
            int tokenColumn = column;

            for (TerminalSymbol type : TerminalSymbol.values()) {
                Matcher matcher = type.pattern.matcher(input).region(position, input.length());

                if (matcher.lookingAt()) {
                    String found = matcher.group();

                    for (int i = 0; i < found.length(); i++) {
                        if (found.charAt(i) == '\n') {
                            line++;
                            column = 1;
                        } else {
                            column++;
                        }
                    }

                    position += found.length();
                    match = true;

                    tokens.add(new Token(found, type, tokenLine, tokenColumn));

                    break;
                }
            }

            if (!match) {
                throw new RuntimeException("LEXICAL ERROR: invalid character found in input at line " + tokenLine + ", column " + tokenColumn + " {" + input.charAt(position) + "}");
            }
        }

        return tokens;
    }
}
