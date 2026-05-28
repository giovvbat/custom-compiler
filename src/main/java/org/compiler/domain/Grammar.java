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

        // Prog -> MainC DefCl
        rules.put(NonTerminalSymbol.PROG,
                List.of(List.of(NonTerminalSymbol.MAIN_C, NonTerminalSymbol.DEF_CL)
        ));

        // MainC -> 'class' Id '{' 'public' 'static' 'void' 'main' '(' 'String' '[' ']' Id ')' '{' CMD '}' '}'
        rules.put(NonTerminalSymbol.MAIN_C, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, TerminalSymbol.PUBLIC,
                        TerminalSymbol.STATIC_VOID_MAIN, TerminalSymbol.PAREN_LEFT, TerminalSymbol.STRING, TerminalSymbol.SQUARE_BRACKET_LEFT,
                        TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.ID, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT,
                        NonTerminalSymbol.CMD, TerminalSymbol.CURLY_BRACKET_RIGHT, TerminalSymbol.CURLY_BRACKET_RIGHT)
        ));
        // DefCl -> 'class' Id DefClRest | EMPTY
        rules.put(NonTerminalSymbol.DEF_CL, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, NonTerminalSymbol.DEF_CL_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DefClRest -> '{' DefVar DefMet '}' DefCl | 'extends' Id '{' DefVar DefMet '}' DefCl
        rules.put(NonTerminalSymbol.DEF_CL_REST, List.of(
                List.of(TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.DEF_MET, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL),
                List.of(TerminalSymbol.EXTENDS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.DEF_MET, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL)
        ));

        // DefVar -> Type Id ';' DefVar | EMPTY
        rules.put(NonTerminalSymbol.DEF_VAR, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.DEF_VAR),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DefMet -> 'public' Type Id '(' DefMetRest | EMPTY
        rules.put(NonTerminalSymbol.DEF_MET, List.of(
                List.of(TerminalSymbol.PUBLIC, NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.DEF_MET_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DefMetRest -> Args ')' '{' DefVar CMD 'return' Exp ';' '}' DefMet
        //             | ')' '{' DefVar CMD 'return' Exp ';' '}' DefMet
        rules.put(NonTerminalSymbol.DEF_MET_REST, List.of(
                List.of(NonTerminalSymbol.ARGS, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.CMD, TerminalSymbol.RETURN, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_MET),
                List.of(TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.CMD, TerminalSymbol.RETURN, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_MET)
        ));
        // Type -> 'int' TypeRest | 'boolean' | Id
        rules.put(NonTerminalSymbol.TYPE, List.of(
                List.of(TerminalSymbol.INT_TYPE, NonTerminalSymbol.TYPE_REST),
                List.of(TerminalSymbol.BOOLEAN_TYPE),
                List.of(TerminalSymbol.ID)
        ));
        // TypeRest -> '[' ']' | EMPTY
        rules.put(NonTerminalSymbol.TYPE_REST, List.of(
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, TerminalSymbol.SQUARE_BRACKET_RIGHT),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // Args -> Type Id RestArgs
        rules.put(NonTerminalSymbol.ARGS, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS)
        ));

        rules.put(NonTerminalSymbol.REST_ARGS, List.of(
                List.of(TerminalSymbol.COMMA, NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // Cmd -> '{' CMD '}' | 'if' '(' Exp ')' Cmd 'else' Cmd | 'while' '(' Exp ')' Cmd
        //      | 'System.out.println' '(' Exp ')' ';' | Id CmdIdRest
        rules.put(NonTerminalSymbol.CMD, List.of(
                List.of(TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMD, TerminalSymbol.CURLY_BRACKET_RIGHT),
                List.of(TerminalSymbol.IF, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD, TerminalSymbol.ELSE, NonTerminalSymbol.CMD),
                List.of(TerminalSymbol.WHILE, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD),
                List.of(TerminalSymbol.SYSTEM_OUT_PRINTLN, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.ID, NonTerminalSymbol.CMD_ID_REST)
        ));

        // CmdIdRest -> '=' Exp ';' | '[' Exp ']' '=' Exp ';'
        rules.put(NonTerminalSymbol.CMD_ID_REST, List.of(
                List.of(TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON)
        ));

        rules.put(NonTerminalSymbol.EXP, List.of(
                List.of(NonTerminalSymbol.BASE_EXP, NonTerminalSymbol.EXP_REST)
        ));

        rules.put(NonTerminalSymbol.BASE_EXP, List.of(
                List.of(TerminalSymbol.NEW, NonTerminalSymbol.NEW_REST),
                List.of(TerminalSymbol.NOT, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT),
                List.of(TerminalSymbol.TRUE),
                List.of(TerminalSymbol.FALSE),
                List.of(TerminalSymbol.ID),
                List.of(TerminalSymbol.NUMBER),
                List.of(TerminalSymbol.THIS)
        ));

        rules.put(NonTerminalSymbol.NEW_REST, List.of(
                List.of(TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, TerminalSymbol.PAREN_RIGHT),
                List.of(TerminalSymbol.INT_TYPE, TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT)
        ));

        rules.put(NonTerminalSymbol.EXP_REST, List.of(
                List.of(TerminalSymbol.AND, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.GREATER, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.PLUS, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.MINUS, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.MULTIPLY, NonTerminalSymbol.EXP),
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT, NonTerminalSymbol.EXP_REST),
                List.of(TerminalSymbol.DOT, NonTerminalSymbol.DOT_REST, NonTerminalSymbol.EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.DOT_REST, List.of(
                List.of(TerminalSymbol.LENGTH),
                List.of(TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.LIST_EXP, TerminalSymbol.PAREN_RIGHT)
        ));

        rules.put(NonTerminalSymbol.LIST_EXP, List.of(
                List.of(NonTerminalSymbol.EXP, NonTerminalSymbol.REST_LIST_EXP),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.REST_LIST_EXP, List.of(
                List.of(TerminalSymbol.COMMA, NonTerminalSymbol.EXP, NonTerminalSymbol.REST_LIST_EXP),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        rules.put(NonTerminalSymbol.EMPTY, List.of());
        for (NonTerminalSymbol nt : NonTerminalSymbol.values()) {
            if (!rules.containsKey(nt)) {
                throw new RuntimeException("CRITICAL ERROR: Missing grammar mapping for -> " + nt.name());
            }
        }
    }
}
