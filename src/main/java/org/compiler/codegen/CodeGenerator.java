package org.compiler.codegen;

import org.compiler.ast.*;
import org.compiler.semantic.SymbolTable;
import org.compiler.semantic.SymbolTable.Entry;

public class CodeGenerator {

    private final SymbolFactory symbols;

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbols = new SymbolFactory(symbolTable);
    }

    public static class ExprResult {
        public final CodeList code;
        public final Entry place;

        public ExprResult(CodeList code, Entry place) {
            this.code = code;
            this.place = place;
        }
    }

    public CodeList generate(Structure.Program program) {
        CodeList code = CodeList.create();
        for (Structure.ClassNode classNode : program.classes) {
            code = CodeList.concat(code, genClass(classNode));
        }
        return code;
    }

    private CodeList genClass(Structure.ClassNode classNode) {
        CodeList code = CodeList.create();
        for (Structure.MethodNode method : classNode.methods) {
            code = CodeList.concat(code, genMethod(classNode, method));
        }
        return code;
    }

    private CodeList genMethod(Structure.ClassNode classNode, Structure.MethodNode method) {
        CodeList code = CodeList.create();
        Entry label = symbols.symbol(classNode.name + "." + method.name, "method", "method");
        code.emit(Operation.LABEL, null, null, label);

        if (method.body != null) {
            code = CodeList.concat(code, genStmt(method.body));
        }
        if (method.returnExp != null) {
            ExprResult ret = genExpr(method.returnExp);
            code = CodeList.concat(code, ret.code);
            code.emit(Operation.RETURN, ret.place, null, null);
        }
        return code;
    }

    private CodeList genStmt(Stmt stmt) {
        if (stmt == null) {
            return CodeList.create();
        }
        if (stmt instanceof Statements.Seq seq) {
            CodeList code = CodeList.create();
            for (Stmt s : seq.stmts) {
                code = CodeList.concat(code, genStmt(s));
            }
            return code;
        }
        if (stmt instanceof Statements.Assign assign) {
            return genAssign(assign);
        }
        if (stmt instanceof Statements.ArrayAssign arrayAssign) {
            return genArrayAssign(arrayAssign);
        }
        if (stmt instanceof Statements.If ifStmt) {
            return genIf(ifStmt);
        }
        if (stmt instanceof Statements.While whileStmt) {
            return genWhile(whileStmt);
        }
        if (stmt instanceof Statements.Print print) {
            ExprResult value = genExpr(print.expr);
            CodeList code = value.code;
            code.emit(Operation.PRINT, value.place, null, null);
            return code;
        }
        if (stmt instanceof Statements.Return ret) {
            ExprResult value = genExpr(ret.expr);
            CodeList code = value.code;
            code.emit(Operation.RETURN, value.place, null, null);
            return code;
        }
        return CodeList.create();
    }

    private CodeList genAssign(Statements.Assign assign) {
        ExprResult value = genExpr(assign.expr);
        CodeList code = value.code;
        code.emit(Operation.ASSIGN, value.place, null, symbols.variable(assign.id));
        return code;
    }

    private CodeList genArrayAssign(Statements.ArrayAssign assign) {
        ExprResult index = genExpr(assign.index);
        ExprResult value = genExpr(assign.value);
        CodeList code = CodeList.concat(index.code, value.code);
        code.emit(Operation.ARRAY_STORE, index.place, value.place, symbols.variable(assign.id));
        return code;
    }

    private CodeList genIf(Statements.If ifStmt) {
        ExprResult cond = genExpr(ifStmt.cond);
        CodeList code = cond.code;

        if (ifStmt.elseStmt == null) {
            Entry endLabel = symbols.newLabel();
            code.emit(Operation.IF_FALSE, cond.place, null, endLabel);
            code = CodeList.concat(code, genStmt(ifStmt.thenStmt));
            code.emit(Operation.LABEL, null, null, endLabel);
            return code;
        }

        Entry elseLabel = symbols.newLabel();
        Entry endLabel = symbols.newLabel();
        code.emit(Operation.IF_FALSE, cond.place, null, elseLabel);
        code = CodeList.concat(code, genStmt(ifStmt.thenStmt));
        code.emit(Operation.GOTO, null, null, endLabel);
        code.emit(Operation.LABEL, null, null, elseLabel);
        code = CodeList.concat(code, genStmt(ifStmt.elseStmt));
        code.emit(Operation.LABEL, null, null, endLabel);
        return code;
    }

    private CodeList genWhile(Statements.While whileStmt) {
        Entry startLabel = symbols.newLabel();
        Entry endLabel = symbols.newLabel();

        CodeList code = CodeList.create();
        code.emit(Operation.LABEL, null, null, startLabel);
        ExprResult cond = genExpr(whileStmt.cond);
        code = CodeList.concat(code, cond.code);
        code.emit(Operation.IF_FALSE, cond.place, null, endLabel);
        code = CodeList.concat(code, genStmt(whileStmt.body));
        code.emit(Operation.GOTO, null, null, startLabel);
        code.emit(Operation.LABEL, null, null, endLabel);
        return code;
    }

    private ExprResult genExpr(Expr expr) {
        if (expr instanceof Expressions.Num num) {
            return new ExprResult(CodeList.create(), symbols.constant(String.valueOf(num.value)));
        }
        if (expr instanceof Expressions.BoolLit bool) {
            return new ExprResult(CodeList.create(), symbols.constant(bool.value ? "1" : "0"));
        }
        if (expr instanceof Expressions.IdNode id) {
            return new ExprResult(CodeList.create(), symbols.variable(id.name));
        }
        if (expr instanceof Expressions.This) {
            return new ExprResult(CodeList.create(), symbols.symbol("this", "this", "var"));
        }
        if (expr instanceof Expressions.Ari ari) {
            return genBinary(Operation.fromToken(ari.op), ari.left, ari.right);
        }
        if (expr instanceof Expressions.Rel rel) {
            return genBinary(Operation.fromToken(rel.op), rel.left, rel.right);
        }
        if (expr instanceof Expressions.Logical logical) {
            return genBinary(Operation.fromToken(logical.op), logical.left, logical.right);
        }
        if (expr instanceof Expressions.Not not) {
            ExprResult operand = genExpr(not.expr);
            Entry temp = symbols.newTemp();
            operand.code.emit(Operation.NOT, operand.place, null, temp);
            return new ExprResult(operand.code, temp);
        }
        if (expr instanceof Expressions.NewObject newObject) {
            Entry temp = symbols.newTemp(newObject.className);
            CodeList code = CodeList.create();
            code.emit(Operation.NEW, symbols.symbol(newObject.className, newObject.className, "class"), null, temp);
            return new ExprResult(code, temp);
        }
        if (expr instanceof Expressions.NewArray newArray) {
            ExprResult size = genExpr(newArray.size);
            Entry temp = symbols.newTemp("int[]");
            size.code.emit(Operation.NEW_ARRAY, size.place, null, temp);
            return new ExprResult(size.code, temp);
        }
        if (expr instanceof Expressions.ArrayAccess access) {
            ExprResult array = genExpr(access.array);
            ExprResult index = genExpr(access.index);
            CodeList code = CodeList.concat(array.code, index.code);
            Entry temp = symbols.newTemp();
            code.emit(Operation.ARRAY_LOAD, array.place, index.place, temp);
            return new ExprResult(code, temp);
        }
        if (expr instanceof Expressions.ArrayLength length) {
            ExprResult array = genExpr(length.array);
            Entry temp = symbols.newTemp();
            array.code.emit(Operation.LENGTH, array.place, null, temp);
            return new ExprResult(array.code, temp);
        }
        if (expr instanceof Expressions.MethodCall call) {
            return genMethodCall(call);
        }
        return new ExprResult(CodeList.create(), symbols.constant("0"));
    }

    private ExprResult genBinary(Operation op, Expr left, Expr right) {
        ExprResult leftResult = genExpr(left);
        ExprResult rightResult = genExpr(right);
        CodeList code = CodeList.concat(leftResult.code, rightResult.code);
        Entry temp = symbols.newTemp();
        code.emit(op, leftResult.place, rightResult.place, temp);
        return new ExprResult(code, temp);
    }

    private ExprResult genMethodCall(Expressions.MethodCall call) {
        ExprResult object = genExpr(call.object);
        CodeList code = object.code;

        java.util.List<Entry> argPlaces = new java.util.ArrayList<>();
        for (Expr arg : call.args) {
            ExprResult argResult = genExpr(arg);
            code = CodeList.concat(code, argResult.code);
            argPlaces.add(argResult.place);
        }

        code.emit(Operation.PARAM, object.place, null, null);
        for (Entry argPlace : argPlaces) {
            code.emit(Operation.PARAM, argPlace, null, null);
        }

        Entry temp = symbols.newTemp();
        Entry method = symbols.symbol(call.methodName, "method", "method");
        Entry argCount = symbols.constant(String.valueOf(call.args.size() + 1));
        code.emit(Operation.CALL, method, argCount, temp);
        return new ExprResult(code, temp);
    }
}
