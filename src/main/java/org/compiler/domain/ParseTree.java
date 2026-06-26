package org.compiler.domain;

import java.util.ArrayList;
import java.util.List;

public class ParseTree {
    public Symbol symbol;
    public Token token;
    public List<ParseTree> children;

    public ParseTree(Symbol symbol, Token token) {
        this.symbol = symbol;
        this.token = token;
        this.children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return symbol.toString() + (token != null ? " [" + token.lexeme() + "]" : "");
    }
}