package org.compiler.domain;

import org.compiler.enums.TokenType;

public record Token(String lexeme, TokenType type) {
    @Override
    public String toString() {
        return String.format("(\"%s\", %s)", lexeme, type.name());
    }
}
