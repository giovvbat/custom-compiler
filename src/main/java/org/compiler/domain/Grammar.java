package org.compiler.domain;

import lombok.Getter;
import org.compiler.enums.NonTerminalSymbol;
import org.compiler.enums.TerminalSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Grammar {
    private final Map<NonTerminalSymbol, List<List<Symbol>>> rules;

    public Grammar() {
        rules = new HashMap<>();

        rules.put(NonTerminalSymbol.PROG,
                List.of(List.of(NonTerminalSymbol.MAIN_C, NonTerminalSymbol.DEF_CL)
        ));

        rules.put(NonTerminalSymbol.MAIN_C, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, TerminalSymbol.PUBLIC, TerminalSymbol.STATIC_VOID_MAIN, TerminalSymbol.PAREN_LEFT, TerminalSymbol.STRING, TerminalSymbol.SQUARE_BRACKET_LEFT, TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.ID, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT, TerminalSymbol.CURLY_BRACKET_RIGHT)
        ));

        rules.put(NonTerminalSymbol.DEF_CL, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL),
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, TerminalSymbol.EXTENDS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.DEF_MET, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.DEF_VAR, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.DEF_VAR),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.DEF_MET, List.of(
                List.of(TerminalSymbol.PUBLIC, NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.ARGS, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.CMDS, TerminalSymbol.RETURN, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_MET),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.CMDS, List.of(
                List.of(NonTerminalSymbol.CMD, NonTerminalSymbol.CMDS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.CMD, List.of(
                List.of(TerminalSymbol.SYSTEM_OUT_PRINTLN, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.ID, TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.ID, TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.WHILE, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT),
                List.of(TerminalSymbol.IF, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT, TerminalSymbol.ELSE, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT),
                List.of(TerminalSymbol.IF, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT)
        ));

        rules.put(NonTerminalSymbol.TYPE, List.of(
                List.of(TerminalSymbol.INT_TYPE, TerminalSymbol.SQUARE_BRACKET_LEFT, TerminalSymbol.SQUARE_BRACKET_RIGHT),
                List.of(TerminalSymbol.BOOLEAN_TYPE),
                List.of(TerminalSymbol.INT_TYPE),
                List.of(TerminalSymbol.ID)
        ));

        rules.put(NonTerminalSymbol.ARGS, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.REST_ARGS, List.of(
                List.of(TerminalSymbol.COMMA, NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.EXP, List.of(
                List.of(TerminalSymbol.ID),
                List.of(TerminalSymbol.TRUE),
                List.of(TerminalSymbol.FALSE),
                List.of(TerminalSymbol.NUMBER)
        ));

        rules.put(NonTerminalSymbol.EMPTY, List.of());
    }
}
