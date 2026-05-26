package org.compiler.enums;

import org.compiler.domain.Symbol;

public enum NonTerminalSymbol implements Symbol {
    // basic
    PROG,
    MAIN_C,
    CMDS,
    CMD,
    TYPE,

    // args
    ARGS,
    REST_ARGS,

    // empty
    EMPTY,

    // definitions
    DEF_CL,
    DEF_VAR,
    DEF_MET,

    // expressions
    EXP,
    BASE_EXP,
    NEW_REST,
    EXP_REST,
    DOT_REST,
    LIST_EXP,
    REST_LIST_EXP,

    DEF_CL_REST,
    TYPE_REST,
    CMD_ID_REST,
    CMD_IF_REST;
}
