package org.compiler.analyzer;

import org.compiler.domain.Grammar;
import org.compiler.domain.Symbol;
import org.compiler.domain.ParseTree;
import org.compiler.enums.NonTerminalSymbol;
import org.compiler.enums.TerminalSymbol;
import org.compiler.domain.Token;
import java.util.EnumSet;
import org.compiler.semantic.SymbolTable;

import java.util.List;
import java.util.Set;

public class Parser {
    private static List<Token> tokens;
    private static Grammar grammar;
    private static int current = 0;
    private static String lastEvaluatedType = "";
    private static boolean showSuggestions;
    public static SymbolTable symbolTable;

    public static ParseTree parse(List<Token> tokens, boolean suggestionsFlag) {
        Parser.tokens = tokens;
        Parser.grammar = new Grammar();
        Parser.current = 0;
        Parser.showSuggestions = suggestionsFlag;
        Parser.symbolTable = new SymbolTable();

        ParseTree root = execute(NonTerminalSymbol.PROG, EnumSet.noneOf(TerminalSymbol.class));

        if (isAtEnd()) {
            System.out.println("success: code is lexically and syntactically correct!");
            return root;
        } else {
            Token t = tokens.get(current);
            throw new RuntimeException(String.format(
                    "syntax error: unparsed tokens remaining starting at line %d, column %d {%s}",
                    t.line(),
                    t.column(),
                    t.lexeme()
            ));
        }
    }

    private static ParseTree execute(NonTerminalSymbol symbol, Set<TerminalSymbol> localFollowers) {
        List<List<Symbol>> rules = grammar.getRules().get(symbol);

        TerminalSymbol lookahead = isAtEnd() ? null : tokens.get(current).type();
        List<Symbol> chosenRule = predictRule(symbol, rules, lookahead, localFollowers);

        // Se o lookahead não pegar nenhuma regra e não tiver a palavra vazia
        if(chosenRule == null){
            Token t = isAtEnd() ? tokens.get(tokens.size() -1 ) : tokens.get(current);
            String lexeme = isAtEnd() ? "EOF" : t.lexeme();

            Set<TerminalSymbol> expectedTokens = EnumSet.noneOf(TerminalSymbol.class);
            expectedTokens.addAll(getFirst(symbol, EnumSet.noneOf(NonTerminalSymbol.class)));
            if (derivesEmpty(symbol) || expectedTokens.isEmpty()) {
                expectedTokens.addAll(localFollowers);
            }

            String expectedList = expectedTokens.stream()
                    .map(TerminalSymbol::name)
                    .collect(java.util.stream.Collectors.joining(", "));

            throw new RuntimeException(String.format(
                    "syntax error: unexpected token '%s' at line %d, column %d. Expected one of: [%s]",
                    lexeme, t.line(), t.column(), expectedList
            ));
        }
        ParseTree node = new ParseTree(symbol, null);
        for (int i = 0; i < chosenRule.size(); i++) {
            Symbol s = chosenRule.get(i);

            if (s == NonTerminalSymbol.EMPTY) continue;

            boolean openScope = false;
            boolean closeScope = false;

            if (symbol == NonTerminalSymbol.MAIN_C) {
                if (i == 2 || i == 5) openScope = true; // Class scope and Main Method scope
                if (i == 13 || i == 14) closeScope = true;
            } else if (symbol == NonTerminalSymbol.DEF_CL_REST) {
                if (chosenRule.get(0) == TerminalSymbol.CURLY_BRACKET_LEFT && i == 0) openScope = true;
                if (chosenRule.get(0) == TerminalSymbol.CURLY_BRACKET_LEFT && i == 3) closeScope = true;
                if (chosenRule.get(0) == TerminalSymbol.EXTENDS && i == 2) openScope = true;
                if (chosenRule.get(0) == TerminalSymbol.EXTENDS && i == 5) closeScope = true;
            } else if (symbol == NonTerminalSymbol.DEF_MET) {
                if (i == 3) openScope = true; // Open method scope at '('
                if (i == 11) closeScope = true; // Close method scope at '}'
            } else if ((symbol == NonTerminalSymbol.NON_ID_CMD || symbol == NonTerminalSymbol.CMD)
                    && chosenRule.get(0) == TerminalSymbol.CURLY_BRACKET_LEFT) {
                if (i == 0) openScope = true;
                if (i == 2) closeScope = true;
            }

            if(openScope) symbolTable.enterScope();

            int startTokenIdx = current;

            if (s instanceof TerminalSymbol) {
                Token t = isAtEnd() ? tokens.get(tokens.size() - 1) : tokens.get(current);
                match((TerminalSymbol) s);
                node.children.add(new ParseTree(s, t));
            } else {
                Set<TerminalSymbol> nextFollowers = EnumSet.noneOf(TerminalSymbol.class);
                boolean allDeriveEmpty = true;
                for (int j = i + 1; j < chosenRule.size(); j++) {
                    Symbol nextSymbol = chosenRule.get(j);
                    nextFollowers.addAll(getFirst(nextSymbol, EnumSet.noneOf(NonTerminalSymbol.class)));
                    if (!(nextSymbol instanceof NonTerminalSymbol) || !derivesEmpty((NonTerminalSymbol) nextSymbol)) {
                        allDeriveEmpty = false;
                        break;
                    }
                }
                if (allDeriveEmpty) {
                    nextFollowers.addAll(localFollowers);
                }
                ParseTree child = execute((NonTerminalSymbol) s, nextFollowers);
                if(child != null){
                    node.children.add(child);
                }
            }
            if (closeScope) symbolTable.exitScope();

            if (s == NonTerminalSymbol.TYPE || s == NonTerminalSymbol.TYPE_REST) {
                StringBuilder typeBuilder = new StringBuilder();
                for (int k = startTokenIdx; k < current; k++) {
                    typeBuilder.append(tokens.get(k).lexeme());
                }
                if (s == NonTerminalSymbol.TYPE) lastEvaluatedType = typeBuilder.toString();
                else if (s == NonTerminalSymbol.TYPE_REST) lastEvaluatedType = "int" + typeBuilder.toString();
            }
            if (s == TerminalSymbol.ID && current > 0) {
                String lexeme = tokens.get(current - 1).lexeme();
                boolean inserted = true;

                // Declarations
                if (symbol == NonTerminalSymbol.MAIN_C) {
                    if (i == 1) inserted = symbolTable.put(lexeme, "class", "CLASS");
                    else if (i == 9) inserted = symbolTable.put(lexeme, "String[]", "PARAM");
                } else if (symbol == NonTerminalSymbol.DEF_CL && i == 1) {
                    inserted = symbolTable.put(lexeme, "class", "CLASS");
                } else if (symbol == NonTerminalSymbol.DEF_MET && i == 2) {
                    inserted = symbolTable.put(lexeme, lastEvaluatedType, "METHOD");
                } else if (symbol == NonTerminalSymbol.DEF_VAR && i == 1) {
                    inserted = symbolTable.put(lexeme, lastEvaluatedType, "VAR");
                } else if (symbol == NonTerminalSymbol.ARGS && i == 1) {
                    inserted = symbolTable.put(lexeme, lastEvaluatedType, "PARAM");
                } else if (symbol == NonTerminalSymbol.REST_ARGS && i == 2) {
                    inserted = symbolTable.put(lexeme, lastEvaluatedType, "PARAM");
                } else if (symbol == NonTerminalSymbol.VARS_THEN_CMDS) {
                    if (chosenRule.get(0) == TerminalSymbol.INT_TYPE && i == 2) {
                        inserted = symbolTable.put(lexeme, lastEvaluatedType, "VAR");
                    } else if (chosenRule.get(0) == TerminalSymbol.BOOLEAN_TYPE && i == 1) {
                        inserted = symbolTable.put(lexeme, "boolean", "VAR");
                    }
                }
            }
        }
        return node;
    }
    private static List<Symbol> predictRule(NonTerminalSymbol symbol ,List<List<Symbol>> rules, TerminalSymbol lookahead,
                                            Set<TerminalSymbol> localFollowers){
        if(lookahead != null){
            for(List<Symbol> rule : rules){
                if (ruleStartsWith(rule, lookahead)) {

                    if (lookahead == TerminalSymbol.NEW && rule.size() > 1 && (current + 1) < tokens.size()) {
                        TerminalSymbol nextToken = tokens.get(current + 1).type();
                        if (rule.get(1) == TerminalSymbol.ID && nextToken != TerminalSymbol.ID) continue;
                        if (rule.get(1) == TerminalSymbol.INT_TYPE && nextToken != TerminalSymbol.INT_TYPE) continue;
                    }
                    return rule;
                }
            }
        }

        for (List<Symbol> rule : rules) {
            if (rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)) {
                boolean isExpressionTail = symbol.name().contains("EXP") || symbol.name().contains("REST");
                if (lookahead == null || localFollowers.contains(lookahead) || isExpressionTail) {
                    return rule;
                }
            }
        }

        return null;
    }
    private static boolean ruleStartsWith(List<Symbol> rule, TerminalSymbol lookahead){
        if(rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)){
            return false;
        }
        for (Symbol symbol : rule) {
            if (symbol instanceof TerminalSymbol) {
                return symbol == lookahead;
            }
            NonTerminalSymbol nt = (NonTerminalSymbol) symbol;
            Set<TerminalSymbol> firstSet = getFirst(nt, EnumSet.noneOf(NonTerminalSymbol.class));

            if (firstSet.contains(lookahead)) {
                return true;
            }

            if (!derivesEmpty(nt)) {
                return false;
            }
        }

        return false;
    }

    private static Set<TerminalSymbol> getFirst(Symbol symbol, Set<NonTerminalSymbol> visited) {
        Set<TerminalSymbol> first = EnumSet.noneOf(TerminalSymbol.class);

        if (symbol instanceof TerminalSymbol) {
            first.add((TerminalSymbol) symbol);
            return first;
        }

        NonTerminalSymbol nt = (NonTerminalSymbol) symbol;
        if (visited.contains(nt)) return first;
        visited.add(nt);

        for (List<Symbol> rule : grammar.getRules().get(nt)) {
            if (rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)) {
                continue;
            }
            first.addAll(getFirst(rule.get(0), visited));
        }
        return first;
    }
    private static boolean derivesEmpty(NonTerminalSymbol nt) {
        for (List<Symbol> rule : grammar.getRules().get(nt)) {
            if (rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)) {
                return true;
            }
        }
        return false;
    }

    private static void match(TerminalSymbol expected) {
        if (!isAtEnd() && tokens.get(current).type() == expected) {
            current++;
        } else {
            Token t = isAtEnd() ? tokens.get(tokens.size() - 1) : tokens.get(current);
            String lexeme = isAtEnd() ? "EOF" : t.lexeme();
            String errorMessage = String.format(
                    "syntax error: expected %s but found '%s' at line %d, column %d",
                    expected.name(),
                    lexeme,
                    t.line(),
                    t.column()
            );
            if (showSuggestions) {
                errorMessage += "\n  -> Suggestion: Replace '" + lexeme + "' with a valid " + expected.name() + " token.";
                if (expected == TerminalSymbol.SEMI_COLON) {
                    errorMessage += " Did you forget a semi-colon at the end of the previous statement?";
                }
            }
             throw new RuntimeException(errorMessage);
        }
    }

    private static boolean isAtEnd() {
        return current >= tokens.size();
    }

}