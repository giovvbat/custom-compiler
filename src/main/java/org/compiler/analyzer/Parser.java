package org.compiler.analyzer;

import org.compiler.domain.Grammar;
import org.compiler.domain.Symbol;
import org.compiler.enums.NonTerminalSymbol;
import org.compiler.enums.TerminalSymbol;
import org.compiler.domain.Token;

import java.util.List;

public class Parser {
    private static List<Token> tokens;
    private static Grammar grammar;

    private static int current = 0;
    private static int border = 0;

    public static void parse(List<Token> tokens) {
        Parser.tokens = tokens;
        Parser.grammar = new Grammar();

        execute(NonTerminalSymbol.PROG);

        if (isAtEnd()) {
            System.out.println("SUCCESS: code is lexically and syntactically correct!");
        } else {
            throw new RuntimeException("SYNTAX ERROR: unexpected token found at line " + tokens.get(border).line() + ", column " + tokens.get(border).column() + " {" + tokens.get(border) + "}");
        }
    }

    private static void execute(NonTerminalSymbol symbol) {
        List<List<Symbol>> rules = grammar.getRules().get(symbol);

        if (current > border) {
            border = current;
        }

        for (int i = 0; i < rules.size(); i++) {
            boolean match = true;
            int oldCurrent = current;

            for (int j = 0; j < rules.get(i).size(); j++) {
                try {
                    if (rules.get(i).get(j) instanceof TerminalSymbol) {
                        match((TerminalSymbol) rules.get(i).get(j));
                    } else {
                        execute((NonTerminalSymbol) rules.get(i).get(j));
                    }
                } catch (RuntimeException exception) {
                    match = false;

                    if (i == rules.size() - 1) {
                        throw new RuntimeException(exception.getMessage());
                    }

                    break;
                }
            }

            if (match) {
                return;
            } else {
                current = oldCurrent;
            }
        }
    }

    private static void match(TerminalSymbol expected) {
        if (!isAtEnd() && tokens.get(current).type() == expected) {
            current++;

            if (current > border) {
                border = current;
            }
        } else {
            throw new RuntimeException("SYNTAX ERROR: unexpected token found at line " + tokens.get(border).line() + ", column " + tokens.get(border).column() + " {" + tokens.get(border) + "}");
        }
    }

    private static boolean isAtEnd() {
        return current >= tokens.size();
    }
}
