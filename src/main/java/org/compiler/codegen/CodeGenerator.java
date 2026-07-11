package org.compiler.codegen;

import org.compiler.ast.Expr;
import org.compiler.ast.Expressions;
import org.compiler.ast.Statements;
import org.compiler.ast.Stmt;
import org.compiler.ast.Structure;
import org.compiler.semantic.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private int tempCounter = 0;
    private int labelCounter = 0;
    private SymbolTable localSymTable;

    public CodeGenerator() {
        this.localSymTable = new SymbolTable();
    }


    private String newTemp(String type) {
        String tempName = "t" + (++tempCounter);
        localSymTable.put(tempName, type, "TEMP");
        return tempName;
    }

    private String newLabel() {
        String labelName = "L" + (++labelCounter);
        localSymTable.put(labelName, "label", "LABEL");
        return labelName;
    }

    public List<Instruction3AC> generate(Structure.Program program) {
        List<Instruction3AC> code = new ArrayList<>();

        for (Structure.ClassNode cNode : program.classes) {
            for (Structure.MethodNode mNode : cNode.methods) {
                code.add(new Instruction3AC("LABEL", null, null, mNode.name));
                code.add(new Instruction3AC("BEGIN_PROC", null, null, null));

                generateStmt(mNode.body, code);

                if (mNode.returnExp != null) {
                    String retVal = generateExpr(mNode.returnExp, code);
                    code.add(new Instruction3AC("RETURN", retVal, null, null));
                } else if (mNode.returnType.equals("void") && !mNode.name.equals("main")) {
                    code.add(new Instruction3AC("RETURN", null, null, null));
                }
                code.add(new Instruction3AC("END_PROC", null, null, null));
                code.add(new Instruction3AC("", null, null, ""));
            }
        }
        return code;
    }

    private void generateStmt(Stmt stmt, List<Instruction3AC> code) {
        if (stmt == null) return;

        if (stmt instanceof Statements.Seq seq) {
            for (Stmt s : seq.stmts) generateStmt(s, code);
        }
        else if (stmt instanceof Statements.Assign assign) {
            String rightSide = generateExpr(assign.expr, code);
            code.add(new Instruction3AC("ASSIGN", rightSide, null, assign.id));
        }
        else if (stmt instanceof Statements.ArrayAssign arrayAssign) {
            String index = generateExpr(arrayAssign.index, code);
            String value = generateExpr(arrayAssign.value, code);
            code.add(new Instruction3AC("ARRAY_ASSIGN", index, value, arrayAssign.id));
        }
        else if (stmt instanceof Statements.If ifStmt) {
            String cond = generateExpr(ifStmt.cond, code);

            String labelTrue = newLabel();
            String labelFalse = newLabel();
            String labelEnd = newLabel();

            code.add(new Instruction3AC("IF_GOTO", cond, null, labelTrue));
            code.add(new Instruction3AC("GOTO", null, null, labelFalse));


            code.add(new Instruction3AC("LABEL", null, null, labelTrue));
            generateStmt(ifStmt.thenStmt, code);
            code.add(new Instruction3AC("GOTO", null, null, labelEnd));


            code.add(new Instruction3AC("LABEL", null, null, labelFalse));
            if (ifStmt.elseStmt != null) {
                generateStmt(ifStmt.elseStmt, code);
            }


            code.add(new Instruction3AC("LABEL", null, null, labelEnd));
        }
        else if (stmt instanceof Statements.While whileStmt) {
            String labelTest = newLabel();
            String labelTrue = newLabel();
            String labelEnd = newLabel();


            code.add(new Instruction3AC("LABEL", null, null, labelTest));
            String cond = generateExpr(whileStmt.cond, code);


            code.add(new Instruction3AC("IF_GOTO", cond, null, labelTrue));
            code.add(new Instruction3AC("GOTO", null, null, labelEnd));


            code.add(new Instruction3AC("LABEL", null, null, labelTrue));
            generateStmt(whileStmt.body, code);


            code.add(new Instruction3AC("GOTO", null, null, labelTest));


            code.add(new Instruction3AC("LABEL", null, null, labelEnd));
        }
        else if (stmt instanceof Statements.Print printStmt) {
            String val = generateExpr(printStmt.expr, code);
            code.add(new Instruction3AC("PRINT", val, null, null));
        }
    }

    private String generateExpr(Expr expr, List<Instruction3AC> code) {
        if (expr instanceof Expressions.Num num) {
            return String.valueOf(num.value);
        }
        if (expr instanceof Expressions.BoolLit bool) {
            return bool.value ? "true" : "false";
        }
        if (expr instanceof Expressions.IdNode id) {
            return id.name;
        }
        if (expr instanceof Expressions.This) {
            return "this";
        }

        if (expr instanceof Expressions.Ari ari) {
            String left = generateExpr(ari.left, code);
            String right = generateExpr(ari.right, code);
            String temp = newTemp("int");
            code.add(new Instruction3AC(ari.op, left, right, temp));
            return temp;
        }
        if (expr instanceof Expressions.Rel rel) {
            String left = generateExpr(rel.left, code);
            String right = generateExpr(rel.right, code);
            String temp = newTemp("boolean");
            code.add(new Instruction3AC(rel.op, left, right, temp));
            return temp;
        }
        if (expr instanceof Expressions.Logical log) {
            String left = generateExpr(log.left, code);
            String right = generateExpr(log.right, code);
            String temp = newTemp("boolean");
            code.add(new Instruction3AC(log.op, left, right, temp));
            return temp;
        }
        if (expr instanceof Expressions.Not not) {
            String val = generateExpr(not.expr, code);
            String temp = newTemp("boolean");
            code.add(new Instruction3AC("!", val, null, temp));
            return temp;
        }
        if (expr instanceof Expressions.NewArray newArray) {
            String size = generateExpr(newArray.size, code);
            String temp = newTemp("int[]");

            code.add(new Instruction3AC("NEW_ARRAY", size, null, temp));
            return temp;
        }
        if (expr instanceof Expressions.NewObject newObj) {
            String temp = newTemp(newObj.className);

            code.add(new Instruction3AC("NEW", newObj.className, null, temp));
            return temp;
        }
        if (expr instanceof Expressions.ArrayAccess arrAcc) {
            String arr = generateExpr(arrAcc.array, code);
            String idx = generateExpr(arrAcc.index, code);
            String temp = newTemp("int");
            code.add(new Instruction3AC("ASSIGN", arr, idx, temp));
            return temp;
        }
        if (expr instanceof Expressions.ArrayLength arrLen) {
            String arr = generateExpr(arrLen.array, code);
            String temp = newTemp("int");
            code.add(new Instruction3AC("LENGTH", arr, null, temp));
            return temp;
        }
        if (expr instanceof Expressions.MethodCall call) {
            String obj = generateExpr(call.object, code);


            for (Expr arg : call.args) {
                String argTemp = generateExpr(arg, code);
                code.add(new Instruction3AC("PARAM", argTemp, null, null));
            }
            code.add(new Instruction3AC("PARAM", obj, null, null));

            String temp = newTemp("var");
            code.add(new Instruction3AC("CALL", call.methodName, String.valueOf(call.args.size() + 1), temp));
            return temp;
        }

        return "";
    }
}