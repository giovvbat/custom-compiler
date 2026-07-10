package org.compiler.codegen;

import org.compiler.semantic.SymbolTable.Entry;

public class Instruction {
    public Operation op;
    public Entry arg1;
    public Entry arg2;
    public Entry result;
    public Instruction next;

    public Instruction(Operation op, Entry arg1, Entry arg2, Entry result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    private static String name(Entry e) {
        return e == null ? "" : e.name();
    }

    @Override
    public String toString() {
        String a1 = name(arg1);
        String a2 = name(arg2);
        String res = name(result);

        switch (op) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case LT:
            case GT:
            case LE:
            case GE:
            case EQ:
            case NE:
            case AND:
            case OR:
                return String.format("%s = %s %s %s", res, a1, op.symbol(), a2);
            case NOT:
                return String.format("%s = ! %s", res, a1);
            case ASSIGN:
                return String.format("%s = %s", res, a1);
            case LABEL:
                return res + ":";
            case GOTO:
                return "goto " + res;
            case IF_FALSE:
                return String.format("ifFalse %s goto %s", a1, res);
            case PARAM:
                return "param " + a1;
            case CALL:
                return String.format("%s = call %s, %s", res, a1, a2);
            case RETURN:
                return "return " + a1;
            case PRINT:
                return "print " + a1;
            case NEW:
                return String.format("%s = new %s", res, a1);
            case NEW_ARRAY:
                return String.format("%s = new int[%s]", res, a1);
            case ARRAY_LOAD:
                return String.format("%s = %s[%s]", res, a1, a2);
            case ARRAY_STORE:
                return String.format("%s[%s] = %s", res, a1, a2);
            case LENGTH:
                return String.format("%s = length %s", res, a1);
            default:
                return op.name();
        }
    }
}
