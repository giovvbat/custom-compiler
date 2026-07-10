package org.compiler.codegen;

public class Instruction3AC {
    public String op;
    public String arg1;
    public String arg2;
    public String result;

    public Instruction3AC(String op, String arg1, String arg2, String result) {
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    @Override
    public String toString() {
        if (op.equals("LABEL")) return result + ":";
        if (op.equals("BEGIN_PROC")) return "beginproc";
        if (op.equals("END_PROC")) return "endproc";
        if (op.isEmpty()) return "";

        if (op.equals("GOTO")) return "goto " + result;
        if (op.equals("IF_FALSE_GOTO")) return "ifFalse " + arg1 + " goto " + result;
        if (op.equals("PARAM")) return "param " + arg1;

        if (op.equals("CALL")) {
            if (result != null) return result + " = call " + arg1 + ", " + arg2;
            return "call " + arg1 + ", " + arg2;
        }

        if (op.equals("RETURN")) return "return" + (arg1 != null ? " " + arg1 : "");
        if (op.equals("PRINT")) return "print " + arg1;

        if (op.equals("ASSIGN")) {
            if (arg2 != null) return result + " = " + arg1 + "[" + arg2 + "]";
            return result + " = " + arg1;
        }

        if (op.equals("ARRAY_ASSIGN")) return result + "[" + arg1 + "] = " + arg2;
        if (op.equals("LENGTH")) return result + " = " + arg1 + ".length()";


        if (arg2 != null) return result + " = " + arg1 + " " + op + " " + arg2;


        return result + " = " + op + " " + arg1;
    }
}