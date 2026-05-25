package org.compiler.domain;

import org.compiler.enums.TerminalSymbol;

public record Token(String lexeme, TerminalSymbol type, int line, int column) {
    @Override
    public String toString() {
        return String.format("(\"%s\", %s)", lexeme, type.name());
    }
}
