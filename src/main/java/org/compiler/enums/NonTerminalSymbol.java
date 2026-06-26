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

    //Para fatoração à esquerda
    DEF_CL_REST,
    DEF_MET_REST,
    TYPE_REST,
    CMD_ID_REST,
    CMD_IF_REST,
    VARS_THEN_CMDS,
    ID_START_REST,
    NON_ID_CMD,

    EXP,
    AND_EXP,
    AND_EXP_REST,
    REL_EXP,
    REL_EXP_REST,
    ADD_EXP,
    ADD_EXP_REST,
    MUL_EXP,
    MUL_EXP_REST,
    UN_EXP,
    PSF_EXP,
    PSF_EXP_REST,
    PRI_EXP,
    L_EXP,
    L_EXP_REST,
    DOT_REST,

}
