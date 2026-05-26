package org.compiler.analyzer;

import org.compiler.domain.Grammar;
import org.compiler.domain.Symbol;
import org.compiler.enums.NonTerminalSymbol;
import org.compiler.enums.TerminalSymbol;
import org.compiler.domain.Token;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
    private static List<Token> tokens;
    private static Grammar grammar;
    private static int current = 0;
    private static int border = 0;

    public static void parse(List<Token> tokens) {
        Parser.tokens = tokens;
        Parser.grammar = new Grammar();
        Parser.current = 0;

        execute(NonTerminalSymbol.PROG);

        if (isAtEnd()) {
            System.out.println("success: code is lexically and syntactically correct!");
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

    private static void execute(NonTerminalSymbol symbol) {
        List<List<Symbol>> rules = grammar.getRules().get(symbol);

        TerminalSymbol lookahead = isAtEnd() ? null : tokens.get(current).type();
        List<Symbol> chosenRule = predictRule(symbol, rules, lookahead);

        // Se o lookahead não pegar nenhuma regra e não tiver a palavra vazia
        if(chosenRule == null){
            Token t = isAtEnd() ? tokens.get(tokens.size() -1 ) : tokens.get(current);
            String lexeme = isAtEnd() ? "EOF" : t.lexeme();
            Set<TerminalSymbol> expectedTokens = getExpectedTerminals(symbol, new java.util.HashSet<>());
            String expectedList = expectedTokens.stream()
                    .map(TerminalSymbol::name)
                    .collect(java.util.stream.Collectors.joining(", "));

            throw new RuntimeException(String.format(
                    "syntax error: unexpected token '%s' at line %d, column %d. Expected one of: [%s]",
                    lexeme, t.line(), t.column(), expectedList
            ));
        }

        for(Symbol s: chosenRule){
            if (s == NonTerminalSymbol.EMPTY) continue;
            if (s instanceof TerminalSymbol) {
                match((TerminalSymbol) s);
            } else {
                execute((NonTerminalSymbol) s);
            }
        }

    }
    private static List<Symbol> predictRule(NonTerminalSymbol symbol ,List<List<Symbol>> rules, TerminalSymbol lookahead){
        if(symbol == NonTerminalSymbol.DEF_VAR && lookahead == TerminalSymbol.ID){
            TerminalSymbol lookahead2 = (current + 1 < tokens.size()) ? tokens.get(current + 1).type(): null;
            if(lookahead2 == TerminalSymbol.ID){
                return rules.get(0);
            } else{
                for (List<Symbol> rule : rules){
                    if (rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)){
                        return rule;
                    }
                }
            }

        }
        // Tenta achar a regra que starta com o lookahead
        if(lookahead != null){
            for(List<Symbol> rule : rules){
                if (ruleStartsWith(rule, lookahead)) {
                    return rule;
                }
            }
        }
        // Se não tiver regra que case e a tenha uma transição vazia
        for (List<Symbol> rule : rules) {
            if (rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)) {
                boolean isExpressionTail = symbol.name().contains("EXP") || symbol.name().contains("REST");
                if (lookahead == null || isLegalFollower(symbol, lookahead) || isExpressionTail) {
                    return rule;
                }
            }
        }
        //erro de sintaxe
        return null;
    }
    private static boolean ruleStartsWith(List<Symbol> rule, TerminalSymbol lookahead){
        if(rule.isEmpty() || rule.get(0) == NonTerminalSymbol.EMPTY){
            return false;
        }
        Symbol firstSymbol = rule.get(0);

        if(firstSymbol instanceof TerminalSymbol){
            return firstSymbol == lookahead;
        }

        // se o primeiro símbolo não for terminal, pega recursivamente o primeiro dele
        return firstSetContains((NonTerminalSymbol) firstSymbol, lookahead);
    }

    private static boolean firstSetContains(NonTerminalSymbol nonTerminal, TerminalSymbol lookahead){
        List<List<Symbol>> rules = grammar.getRules().get(nonTerminal);
        for(List<Symbol> rule: rules){
            if(rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)){
                continue; // ignora regras vazias
            }
            Symbol first = rule.get(0);

            if(first instanceof TerminalSymbol){
                if(first == lookahead) return true;
            } else if (first instanceof NonTerminalSymbol){
                if(first != nonTerminal && firstSetContains((NonTerminalSymbol) first, lookahead)){
                    return true;
                }
            }
        }
        return false;
    }
    private static Set<TerminalSymbol> getFollowers(NonTerminalSymbol target, Set<NonTerminalSymbol> visited) {
        Set<TerminalSymbol> followers = new java.util.HashSet<>();

        if (visited.contains(target)) return followers;
        visited.add(target);

        for (java.util.Map.Entry<NonTerminalSymbol, List<List<Symbol>>> entry : grammar.getRules().entrySet()) {
            NonTerminalSymbol leftSide = entry.getKey();

            for (List<Symbol> rule : entry.getValue()) {
                for (int i = 0; i < rule.size(); i++) {
                    if (rule.get(i) == target) {
                        if (i + 1 < rule.size()) {
                            Symbol nextSymbol = rule.get(i + 1);
                            followers.addAll(getFirst(nextSymbol, new java.util.HashSet<>()));
                            if (nextSymbol instanceof NonTerminalSymbol && derivesEmpty((NonTerminalSymbol) nextSymbol)) {
                                if (leftSide != target) {
                                    followers.addAll(getFollowers(leftSide, visited));
                                }
                            }
                        } else {
                            if (leftSide != target) {
                                followers.addAll(getFollowers(leftSide, visited));
                            }
                        }
                    }
                }
            }
        }
        return followers;
    }

    private static Set<TerminalSymbol> getFirst(Symbol symbol, Set<NonTerminalSymbol> visited) {
        Set<TerminalSymbol> first = new HashSet<>();

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
    private static boolean isLegalFollower(NonTerminalSymbol symbol, TerminalSymbol lookahead) {
        return getFollowers(symbol, new HashSet<>()).contains(lookahead);
    }
    private static void match(TerminalSymbol expected) {
        if (!isAtEnd() && tokens.get(current).type() == expected) {
            current++;
        } else {
            Token t = isAtEnd() ? tokens.get(tokens.size() - 1) : tokens.get(current);
            String lexeme = isAtEnd() ? "EOF" : t.lexeme();
            throw new RuntimeException(String.format(
                    "syntax error: expected %s but found '%s' at line %d, column %d",
                    expected.name(),
                    lexeme,
                    t.line(),
                    t.column()
            ));
        }
    }

    private static boolean isAtEnd() {
        return current >= tokens.size();
    }

    private static Set<TerminalSymbol> getExpectedTerminals(NonTerminalSymbol nonTerminal, Set<NonTerminalSymbol> visited){
        Set<TerminalSymbol> expected = new HashSet<>();

        if(visited.contains(nonTerminal)) return expected;
        visited.add(nonTerminal);
        List<List<Symbol>> rules = grammar.getRules().get(nonTerminal);
        for(List<Symbol> rule: rules){
            if(rule.isEmpty() || (rule.size() == 1 && rule.get(0) == NonTerminalSymbol.EMPTY)){
                expected.addAll(getFollowers(nonTerminal, new HashSet<>()));
                continue;
            }
            Symbol first = rule.get(0);
            if(first instanceof TerminalSymbol){
                expected.add((TerminalSymbol) first);
            }else if(first instanceof NonTerminalSymbol){
                expected.addAll(getExpectedTerminals((NonTerminalSymbol) first, visited));
            }
        }
    return expected;
    }
}
