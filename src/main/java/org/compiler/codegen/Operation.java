package org.compiler.codegen;

public enum Operation {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    EQ("=="),
    NE("!="),
    AND("&&"),
    OR("||"),
    NOT("!"),
    ASSIGN("="),
    LABEL(":"),
    GOTO("goto"),
    IF_FALSE("ifFalse"),
    PARAM("param"),
    CALL("call"),
    RETURN("return"),
    PRINT("print"),
    NEW("new"),
    NEW_ARRAY("newarray"),
    ARRAY_LOAD("load"),
    ARRAY_STORE("store"),
    LENGTH("length");

    private final String symbol;

    Operation(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static Operation fromToken(String token) {
        switch (token) {
            case "+": return ADD;
            case "-": return SUB;
            case "*": return MUL;
            case "/": return DIV;
            case "<": return LT;
            case ">": return GT;
            case "<=": return LE;
            case ">=": return GE;
            case "==": return EQ;
            case "!=": return NE;
            case "&&": return AND;
            case "||": return OR;
            default: throw new IllegalArgumentException("unknown operator: " + token);
        }
    }
}
