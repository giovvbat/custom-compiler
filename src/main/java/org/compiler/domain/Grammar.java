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

        // PROG -> MAIN_C DEF_CL
        rules.put(NonTerminalSymbol.PROG,
                List.of(List.of(NonTerminalSymbol.MAIN_C, NonTerminalSymbol.DEF_CL)
                ));

        // MAIN_C -> 'class' Id '{' 'public' 'static' 'void' 'main' '(' 'String' '[' ']' Id ')' '{' CMDS '}' '}'
        rules.put(NonTerminalSymbol.MAIN_C, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, TerminalSymbol.PUBLIC,
                        TerminalSymbol.STATIC_VOID_MAIN, TerminalSymbol.PAREN_LEFT, TerminalSymbol.STRING, TerminalSymbol.SQUARE_BRACKET_LEFT,
                        TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.ID, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT,
                        NonTerminalSymbol.CMDS  , TerminalSymbol.CURLY_BRACKET_RIGHT, TerminalSymbol.CURLY_BRACKET_RIGHT)
        ));

        // DEF_CL -> 'class' Id DEF_CL_REST | EMPTY
        rules.put(NonTerminalSymbol.DEF_CL, List.of(
                List.of(TerminalSymbol.CLASS, TerminalSymbol.ID, NonTerminalSymbol.DEF_CL_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DEF_CL_REST -> '{' DEF_VAR DEF_MET '}' DEF_CL | 'extends' Id '{' DEF_VAR DEF_MET '}' DEF_CL
        rules.put(NonTerminalSymbol.DEF_CL_REST, List.of(
                List.of(TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.DEF_MET, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL),
                List.of(TerminalSymbol.EXTENDS, TerminalSymbol.ID, TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.DEF_VAR, NonTerminalSymbol.DEF_MET, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_CL)
        ));

        // DEF_VAR -> TYPE Id ';' DEF_VAR | EMPTY
        rules.put(NonTerminalSymbol.DEF_VAR, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.DEF_VAR),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DEF_MET -> 'public' TYPE Id '(' ARGS ')' '{' VARS_THEN_CMDS 'return' EXP ';' '}' DEF_MET | EMPTY
        rules.put(NonTerminalSymbol.DEF_MET, List.of(
                List.of(TerminalSymbol.PUBLIC, NonTerminalSymbol.TYPE, TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.ARGS, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.CURLY_BRACKET_LEFT,
                        NonTerminalSymbol.VARS_THEN_CMDS,
                        TerminalSymbol.RETURN, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON, TerminalSymbol.CURLY_BRACKET_RIGHT, NonTerminalSymbol.DEF_MET),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // VARS_THEN_CMDS -> 'int' TYPE_REST Id ';' VARS_THEN_CMDS
        //                 | 'boolean' Id ';' VARS_THEN_CMDS
        //                 | Id ID_START_REST
        //                 | NON_ID_CMD CMDS
        //                 | EMPTY
        rules.put(NonTerminalSymbol.VARS_THEN_CMDS, List.of(
                List.of(TerminalSymbol.INT_TYPE, NonTerminalSymbol.TYPE_REST, TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.VARS_THEN_CMDS),
                List.of(TerminalSymbol.BOOLEAN_TYPE, TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.VARS_THEN_CMDS),

                List.of(TerminalSymbol.ID, NonTerminalSymbol.ID_START_REST),

                List.of(NonTerminalSymbol.NON_ID_CMD, NonTerminalSymbol.CMDS),

                List.of(NonTerminalSymbol.EMPTY)
        ));

        // ID_START_REST -> Id ';' VARS_THEN_CMDS | CMD_ID_REST CMDS
        rules.put(NonTerminalSymbol.ID_START_REST, List.of(
                List.of(TerminalSymbol.ID, TerminalSymbol.SEMI_COLON, NonTerminalSymbol.VARS_THEN_CMDS),

                List.of(NonTerminalSymbol.CMD_ID_REST, NonTerminalSymbol.CMDS)
        ));

        // NON_ID_CMD -> '{' CMDS '}'
        //             | 'System.out.println' '(' EXP ')' ';'
        //             | 'while' '(' EXP ')' CMD
        //             | 'if' '(' EXP ')' CMD CMD_IF_REST
        rules.put(NonTerminalSymbol.NON_ID_CMD, List.of(
                List.of(TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT),
                List.of(TerminalSymbol.SYSTEM_OUT_PRINTLN, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.WHILE, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD),
                List.of(TerminalSymbol.IF, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD, NonTerminalSymbol.CMD_IF_REST)
        ));

        // CMDS -> CMD CMDS | EMPTY
        rules.put(NonTerminalSymbol.CMDS, List.of(
                List.of(NonTerminalSymbol.CMD, NonTerminalSymbol.CMDS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // CMD -> '{' CMDS '}'
        //      | 'System.out.println' '(' EXP ')' ';'
        //      | 'while' '(' EXP ')' CMD
        //      | Id CMD_ID_REST
        //      | 'if' '(' EXP ')' CMD CMD_IF_REST
        rules.put(NonTerminalSymbol.CMD, List.of(
                List.of(TerminalSymbol.CURLY_BRACKET_LEFT, NonTerminalSymbol.CMDS, TerminalSymbol.CURLY_BRACKET_RIGHT),
                List.of(TerminalSymbol.SYSTEM_OUT_PRINTLN, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.WHILE, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD),
                List.of(TerminalSymbol.ID, NonTerminalSymbol.CMD_ID_REST),
                List.of(TerminalSymbol.IF, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.CMD, NonTerminalSymbol.CMD_IF_REST)
        ));

        // CMD_ID_REST -> '=' EXP ';' | '[' EXP ']' '=' EXP ';'
        rules.put(NonTerminalSymbol.CMD_ID_REST, List.of(
                List.of(TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON),
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT, TerminalSymbol.EQUALS, NonTerminalSymbol.EXP, TerminalSymbol.SEMI_COLON)
        ));

        // CMD_IF_REST -> 'else' CMD | EMPTY
        rules.put(NonTerminalSymbol.CMD_IF_REST, List.of(
                List.of(TerminalSymbol.ELSE, NonTerminalSymbol.CMD),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // TYPE -> 'int' TYPE_REST | 'boolean' | Id
        rules.put(NonTerminalSymbol.TYPE, List.of(
                List.of(TerminalSymbol.INT_TYPE, NonTerminalSymbol.TYPE_REST),
                List.of(TerminalSymbol.BOOLEAN_TYPE),
                List.of(TerminalSymbol.ID)
        ));

        // TYPE_REST -> '[' ']' | EMPTY
        rules.put(NonTerminalSymbol.TYPE_REST, List.of(
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, TerminalSymbol.SQUARE_BRACKET_RIGHT),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // ARGS -> TYPE Id REST_ARGS | EMPTY
        rules.put(NonTerminalSymbol.ARGS, List.of(
                List.of(NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // REST_ARGS -> ',' TYPE Id REST_ARGS | EMPTY
        rules.put(NonTerminalSymbol.REST_ARGS, List.of(
                List.of(TerminalSymbol.COMMA, NonTerminalSymbol.TYPE, TerminalSymbol.ID, NonTerminalSymbol.REST_ARGS),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // EXP -> AND_EXP
        rules.put(NonTerminalSymbol.EXP, List.of(List.of(NonTerminalSymbol.AND_EXP)));

        // AND_EXP -> REL_EXP AND_EXP_REST
        rules.put(NonTerminalSymbol.AND_EXP, List.of(List.of(NonTerminalSymbol.REL_EXP, NonTerminalSymbol.AND_EXP_REST)));

        // AND_EXP_REST -> '&&' REL_EXP AND_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.AND_EXP_REST, List.of(
                List.of(TerminalSymbol.AND, NonTerminalSymbol.REL_EXP, NonTerminalSymbol.AND_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // REL_EXP -> ADD_EXP REL_EXP_REST
        rules.put(NonTerminalSymbol.REL_EXP, List.of(List.of(NonTerminalSymbol.ADD_EXP, NonTerminalSymbol.REL_EXP_REST)));

        // REL_EXP_REST -> '<' ADD_EXP REL_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.REL_EXP_REST, List.of(
                List.of(TerminalSymbol.LESS, NonTerminalSymbol.ADD_EXP, NonTerminalSymbol.REL_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // ADD_EXP -> MUL_EXP ADD_EXP_REST
        rules.put(NonTerminalSymbol.ADD_EXP, List.of(List.of(NonTerminalSymbol.MUL_EXP, NonTerminalSymbol.ADD_EXP_REST)));

        // ADD_EXP_REST -> '+' MUL_EXP ADD_EXP_REST | '-' MUL_EXP ADD_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.ADD_EXP_REST, List.of(
                List.of(TerminalSymbol.PLUS, NonTerminalSymbol.MUL_EXP, NonTerminalSymbol.ADD_EXP_REST),
                List.of(TerminalSymbol.MINUS, NonTerminalSymbol.MUL_EXP, NonTerminalSymbol.ADD_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // MUL_EXP -> UN_EXP MUL_EXP_REST
        rules.put(NonTerminalSymbol.MUL_EXP, List.of(List.of(NonTerminalSymbol.UN_EXP, NonTerminalSymbol.MUL_EXP_REST)));

        // MUL_EXP_REST -> '*' UN_EXP MUL_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.MUL_EXP_REST, List.of(
                List.of(TerminalSymbol.MULTIPLY, NonTerminalSymbol.UN_EXP, NonTerminalSymbol.MUL_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // UN_EXP -> '!' UN_EXP | PSF_EXP
        rules.put(NonTerminalSymbol.UN_EXP, List.of(
                List.of(TerminalSymbol.NOT, NonTerminalSymbol.UN_EXP),
                List.of(NonTerminalSymbol.PSF_EXP)
        ));

        // PSF_EXP -> PRI_EXP PSF_EXP_REST
        rules.put(NonTerminalSymbol.PSF_EXP, List.of(List.of(NonTerminalSymbol.PRI_EXP, NonTerminalSymbol.PSF_EXP_REST)));

        // PSF_EXP_REST -> '[' EXP ']' PSF_EXP_REST | '.' DOT_REST | EMPTY
        rules.put(NonTerminalSymbol.PSF_EXP_REST, List.of(
                List.of(TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT, NonTerminalSymbol.PSF_EXP_REST),
                List.of(TerminalSymbol.DOT, NonTerminalSymbol.DOT_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // DOT_REST -> 'length' PSF_EXP_REST | Id '(' L_EXP ')' PSF_EXP_REST
        rules.put(NonTerminalSymbol.DOT_REST, List.of(
                List.of(TerminalSymbol.LENGTH, NonTerminalSymbol.PSF_EXP_REST),
                List.of(TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.L_EXP, TerminalSymbol.PAREN_RIGHT, NonTerminalSymbol.PSF_EXP_REST)
        ));

        // PRI_EXP -> '(' EXP ')' | 'true' | 'false' | Id | Number | 'this' | 'new' Id '(' ')' | 'new' 'int' '[' EXP ']'
        rules.put(NonTerminalSymbol.PRI_EXP, List.of(
                List.of(TerminalSymbol.PAREN_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.PAREN_RIGHT),
                List.of(TerminalSymbol.TRUE),
                List.of(TerminalSymbol.FALSE),
                List.of(TerminalSymbol.ID),
                List.of(TerminalSymbol.NUMBER),
                List.of(TerminalSymbol.THIS),
                List.of(TerminalSymbol.NEW, TerminalSymbol.ID, TerminalSymbol.PAREN_LEFT, TerminalSymbol.PAREN_RIGHT),
                List.of(TerminalSymbol.NEW, TerminalSymbol.INT_TYPE, TerminalSymbol.SQUARE_BRACKET_LEFT, NonTerminalSymbol.EXP, TerminalSymbol.SQUARE_BRACKET_RIGHT)
        ));

        // L_EXP -> EXP L_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.L_EXP, List.of(
                List.of(NonTerminalSymbol.EXP, NonTerminalSymbol.L_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // L_EXP_REST -> ',' EXP L_EXP_REST | EMPTY
        rules.put(NonTerminalSymbol.L_EXP_REST, List.of(
                List.of(TerminalSymbol.COMMA, NonTerminalSymbol.EXP, NonTerminalSymbol.L_EXP_REST),
                List.of(NonTerminalSymbol.EMPTY)
        ));

        // EMPTY ->
        rules.put(NonTerminalSymbol.EMPTY, List.of());
    }
}