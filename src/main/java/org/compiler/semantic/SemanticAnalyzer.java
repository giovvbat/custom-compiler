package org.compiler.semantic;

import org.compiler.ast.*;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {

    static class MethodData {
        String returnType;
        Map<String, String> params = new HashMap<>();
        Map<String, String> locals = new HashMap<>();
    }

    static class ClassData {
        String parent;
        Map<String, String> fields = new HashMap<>();
        Map<String, MethodData> methods = new HashMap<>();
    }

    private static Map<String, ClassData> classTable = new HashMap<>();
    private static String currentClass = null;
    private static String currentMethod = null;

    public static void analyze(Structure.Program program, boolean printSymbolTable) {
        classTable.clear();

        for (Structure.ClassNode cNode : program.classes) {
            if (classTable.containsKey(cNode.name)) {
                throw new RuntimeException("semantic error: duplicate class declaration '" + cNode.name + "'");
            }

            if (!cNode.name.equals(program.classes.get(0).name) && cNode.fields.isEmpty() && cNode.methods.isEmpty()) {
                throw new RuntimeException("semantic error: class '" + cNode.name + "' is empty (no attributes or methods)");
            }

            ClassData classData = new ClassData();
            classData.parent = cNode.parentName;

            for (Structure.FieldNode f : cNode.fields) {
                if (classData.fields.containsKey(f.name)) throw new RuntimeException("semantic error: duplicate field '" + f.name + "' in class '" + cNode.name + "'");
                classData.fields.put(f.name, f.type);
            }

            for (Structure.MethodNode m : cNode.methods) {
                if (classData.methods.containsKey(m.name)) throw new RuntimeException("semantic error: duplicate method '" + m.name + "' in class '" + cNode.name + "'");
                MethodData mData = new MethodData();
                mData.returnType = m.returnType;

                for (Structure.FieldNode p : m.params) {
                    if (mData.params.containsKey(p.name)) throw new RuntimeException("semantic error: duplicate parameter '" + p.name + "' in method '" + m.name + "'");
                    mData.params.put(p.name, p.type);
                }
                for (Structure.FieldNode l : m.localVars) {
                    if (mData.locals.containsKey(l.name) || mData.params.containsKey(l.name)) throw new RuntimeException("semantic error: duplicate local variable '" + l.name + "' in method '" + m.name + "'");
                    mData.locals.put(l.name, l.type);
                }

                classData.methods.put(m.name, mData);
            }
            classTable.put(cNode.name, classData);
        }

        if (printSymbolTable) {
            printTable();
        }

        for (Structure.ClassNode cNode : program.classes) {
            currentClass = cNode.name;
            for (Structure.MethodNode mNode : cNode.methods) {
                currentMethod = mNode.name;

                if (mNode.body != null) checkStmt(mNode.body);

                if (mNode.returnExp != null) {
                    String retType = evalType(mNode.returnExp);
                    if (!isAssignable(mNode.returnType, retType)) {
                        throw new RuntimeException("semantic error: return type mismatch in method '" + mNode.name + "'. Expected " + mNode.returnType + ", got " + retType);
                    }
                } else if (!mNode.returnType.equals("void")) {
                    throw new RuntimeException("semantic error: missing return statement in method '" + mNode.name + "'");
                }
            }
        }
        System.out.println("success: code is semantically correct!");
    }

    private static void printTable() {
        System.out.println("\n=== GLOBAL SYMBOL TABLE ===");
        for (String cName : classTable.keySet()) {
            ClassData cd = classTable.get(cName);
            String parentStr = cd.parent != null ? " extends " + cd.parent : "";
            System.out.println("Class: " + cName + parentStr);
            for (String f : cd.fields.keySet()) System.out.println("  Attribute: " + cd.fields.get(f) + " " + f);
            for (String m : cd.methods.keySet()) {
                System.out.println("  Method: " + m + " -> " + cd.methods.get(m).returnType);
                MethodData md = cd.methods.get(m);
                for (String p : md.params.keySet()) System.out.println("    Param: " + md.params.get(p) + " " + p);
                for (String l : md.locals.keySet()) System.out.println("    Local: " + md.locals.get(l) + " " + l);
            }
        }
        System.out.println("===========================\n");
    }

    private static void checkStmt(Stmt s) {
        if (s == null) return;

        if (s instanceof Statements.Seq seq) {
            for (Stmt stmt : seq.stmts) checkStmt(stmt);
        } else if (s instanceof Statements.If i) {
            if (!evalType(i.cond).equals("boolean")) throw new RuntimeException("semantic error: 'if' condition must be boolean");
            checkStmt(i.thenStmt);
            checkStmt(i.elseStmt);
        } else if (s instanceof Statements.While w) {
            if (!evalType(w.cond).equals("boolean")) throw new RuntimeException("semantic error: 'while' condition must be boolean");
            checkStmt(w.body);
        } else if (s instanceof Statements.Print p) {
            evalType(p.expr);
        } else if (s instanceof Statements.Assign a) {
            String targetType = lookupVarType(a.id);
            String exprType = evalType(a.expr);
            if (!isAssignable(targetType, exprType)) {
                throw new RuntimeException("semantic error: cannot assign " + exprType + " to " + targetType + " variable '" + a.id + "'");
            }
        } else if (s instanceof Statements.ArrayAssign a) {
            if (!lookupVarType(a.id).equals("int[]")) throw new RuntimeException("semantic error: variable '" + a.id + "' is not an array");
            if (!evalType(a.index).equals("int")) throw new RuntimeException("semantic error: array index must evaluate to int");
            if (!evalType(a.value).equals("int")) throw new RuntimeException("semantic error: array assignment requires int value");
        }
    }

    private static String evalType(Expr e) {
        if (e instanceof Expressions.Num) return "int";
        if (e instanceof Expressions.BoolLit) return "boolean";
        if (e instanceof Expressions.This) return currentClass;
        if (e instanceof Expressions.IdNode id) return lookupVarType(id.name);

        if (e instanceof Expressions.Ari a) {
            if (evalType(a.left).equals("int") && evalType(a.right).equals("int")) return "int";
            throw new RuntimeException("semantic error: arithmetic operators require int operands");
        }
        if (e instanceof Expressions.Rel r) {
            if (evalType(r.left).equals("int") && evalType(r.right).equals("int")) return "boolean";
            throw new RuntimeException("semantic error: relational operators require int operands");
        }
        if (e instanceof Expressions.Logical l) {
            if (evalType(l.left).equals("boolean") && evalType(l.right).equals("boolean")) return "boolean";
            throw new RuntimeException("semantic error: logical operators require boolean operands");
        }
        if (e instanceof Expressions.Not n) {
            if (evalType(n.expr).equals("boolean")) return "boolean";
            throw new RuntimeException("semantic error: '!' operator requires boolean operand");
        }
        if (e instanceof Expressions.NewArray na) {
            if (!evalType(na.size).equals("int")) throw new RuntimeException("semantic error: array size must evaluate to int");
            return "int[]";
        }
        if (e instanceof Expressions.NewObject no) {
            if (!classTable.containsKey(no.className)) throw new RuntimeException("semantic error: unknown class '" + no.className + "'");
            return no.className;
        }
        if (e instanceof Expressions.ArrayAccess aa) {
            if (!evalType(aa.array).equals("int[]")) throw new RuntimeException("semantic error: array access '[]' requires int[]");
            if (!evalType(aa.index).equals("int")) throw new RuntimeException("semantic error: array index must be int");
            return "int";
        }
        if (e instanceof Expressions.ArrayLength al) {
            if (!evalType(al.array).equals("int[]")) throw new RuntimeException("semantic error: '.length' requires int[]");
            return "int";
        }
        if (e instanceof Expressions.MethodCall mc) {
            String objType = evalType(mc.object);
            MethodData mData = lookupMethod(objType, mc.methodName);
            return mData.returnType;
        }

        throw new RuntimeException("semantic error: unable to evaluate expression type");
    }

    private static String lookupVarType(String id) {
        MethodData m = classTable.get(currentClass).methods.get(currentMethod);

        // local variables inside method
        if (m.locals.containsKey(id)) return m.locals.get(id);

        // Method parameters
        if (m.params.containsKey(id)) return m.params.get(id);

        // Class Attributes
        String curr = currentClass;
        while (curr != null && classTable.containsKey(curr)) {
            if (classTable.get(curr).fields.containsKey(id)) return classTable.get(curr).fields.get(id);
            curr = classTable.get(curr).parent;
        }

        throw new RuntimeException("semantic error: undeclared variable '" + id + "'");
    }

    private static MethodData lookupMethod(String className, String methodName) {
        String curr = className;
        while (curr != null && classTable.containsKey(curr)) {
            if (classTable.get(curr).methods.containsKey(methodName)) return classTable.get(curr).methods.get(methodName);
            curr = classTable.get(curr).parent;
        }
        throw new RuntimeException("semantic error: method '" + methodName + "' not found in class '" + className + "'");
    }

    private static boolean isAssignable(String target, String source) {
        if (target.equals(source)) return true;
        if (target.equals("int") || target.equals("boolean") || target.equals("int[]") ||
                source.equals("int") || source.equals("boolean") || source.equals("int[]")) return false;

        String curr = source;
        while (curr != null && classTable.containsKey(curr)) {
            if (curr.equals(target)) return true;
            curr = classTable.get(curr).parent;
        }
        return false;
    }
}