package org.compiler.semantic;

import org.compiler.ast.*;

import java.util.*;

public class SemanticAnalyzer {

    static class MethodData {
        String returnType;
        Map<String, String> params = new LinkedHashMap<>();
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
    private static boolean suggest = false;
    private static String fileName = "";
    private static Node currentContextNode = null;

    public static void analyze(Structure.Program program, String file, boolean showSuggestions) {
        classTable.clear();
        suggest = showSuggestions;
        fileName = file;

        for (Structure.ClassNode cNode : program.classes) {
            if (classTable.containsKey(cNode.name)) {
                throwError("duplicate class declaration '" + cNode.name + "'", cNode, "Rename the class or remove the duplicate definition.");
            }

            if (!cNode.name.equals(program.classes.get(0).name) && cNode.fields.isEmpty() && cNode.methods.isEmpty()) {
                throwError("class '" + cNode.name + "' is empty (no attributes or methods)", cNode, "Add fields or methods, or remove the class entirely.");
            }

            ClassData classData = new ClassData();
            classData.parent = cNode.parentName;

            for (Structure.FieldNode f : cNode.fields) {
                if (classData.fields.containsKey(f.name)) throwError("duplicate field '" + f.name + "' in class '" + cNode.name + "'", f, "Rename or remove the duplicate variable.");
                classData.fields.put(f.name, f.type);
            }

            for (Structure.MethodNode m : cNode.methods) {
                if (classData.methods.containsKey(m.name)) throwError("duplicate method '" + m.name + "' in class '" + cNode.name + "'", m, "Rename the method.");
                MethodData mData = new MethodData();
                mData.returnType = m.returnType;

                for (Structure.FieldNode p : m.params) {
                    if (mData.params.containsKey(p.name)) throwError("duplicate parameter '" + p.name + "' in method '" + m.name + "'", p, "Rename the parameter.");
                    mData.params.put(p.name, p.type);
                }
                for (Structure.FieldNode l : m.localVars) {
                    if (mData.locals.containsKey(l.name) || mData.params.containsKey(l.name)) throwError("duplicate local variable '" + l.name + "' in method '" + m.name + "'", l, "Rename the local variable so it doesn't conflict.");
                    mData.locals.put(l.name, l.type);
                }

                classData.methods.put(m.name, mData);
            }
            classTable.put(cNode.name, classData);
        }
        for (String className : classTable.keySet()) {
            ClassData cd = classTable.get(className);
            if (cd.parent != null && classTable.containsKey(cd.parent)) {
                for (String methodName : cd.methods.keySet()) {
                    MethodData localMethod = cd.methods.get(methodName);

                    String currParent = cd.parent;
                    while (currParent != null && classTable.containsKey(currParent)) {
                        ClassData parentData = classTable.get(currParent);

                        if (parentData.methods.containsKey(methodName)) {
                            MethodData parentMethod = parentData.methods.get(methodName);


                            if (!localMethod.returnType.equals(parentMethod.returnType)) {
                                throwError("invalid method override: return type '" + localMethod.returnType +
                                        "' does not match parent type '" + parentMethod.returnType + "' in method '" + methodName + "'", null, "Overridden methods must have the exact same return type.");
                            }

                            if (localMethod.params.size() != parentMethod.params.size()) {
                                throwError("invalid method override: method '" + methodName + "' has different number of parameters than parent", null, "Overridden methods must have the same arguments.");
                            }

                            List<String> localParamTypes = new ArrayList<>(localMethod.params.values());
                            List<String> parentParamTypes = new ArrayList<>(parentMethod.params.values());
                            for (int i = 0; i < localParamTypes.size(); i++) {
                                if (!localParamTypes.get(i).equals(parentParamTypes.get(i))) {
                                    throwError("invalid method override: parameter type mismatch in method '" + methodName + "'", null, "Overridden methods must have identical parameter types.");
                                }
                            }
                            break;
                        }
                        currParent = parentData.parent;
                    }
                }
            }
        }

        for (Structure.ClassNode cNode : program.classes) {
            currentClass = cNode.name;
            for (Structure.MethodNode mNode : cNode.methods) {
                currentMethod = mNode.name;

                if (mNode.body != null) checkStmt(mNode.body);

                if (mNode.returnExp != null) {
                    currentContextNode = new Statements.Return(mNode.returnExp);
                    String retType = evalType(mNode.returnExp);
                    if (!isAssignable(mNode.returnType, retType)) {
                        throwError("return type mismatch in method '" + mNode.name + "'. Expected " + mNode.returnType + ", got " + retType, mNode.returnExp, "Ensure the expression matches the declared return type in the method signature.");
                    }
                } else if (!mNode.returnType.equals("void")) {
                    throwError("missing return statement in method '" + mNode.name + "'", mNode, "Add a 'return' statement at the end of the method.");
                }
            }
        }
        System.out.println("success: code is semantically correct!");
    }

    private static void throwError(String message, Node node, String suggestion) {
        StringBuilder errorMsg = new StringBuilder();

        if (node != null && node.line > 0) {
            String nodeType = node.getClass().getSimpleName();
            errorMsg.append(fileName).append(":").append(node.line).append(": error: ").
                    append(message).append(" : [Type: ").append(nodeType).
                    append(", Column: ").append(node.column).append("]\n");

            Node targetToReconstruct = (currentContextNode != null) ? currentContextNode : node;
            String snippet = reconstruct(targetToReconstruct);

            if (!snippet.isEmpty()) {
                errorMsg.append("    ").append(snippet).append("\n");
            }
        } else {
            errorMsg.append("semantic error: ").append(message).append("\n");
        }

        if (suggest && suggestion != null) {
            errorMsg.append("  -> Suggestion: ").append(suggestion);
        }

        throw new RuntimeException(errorMsg.toString());
    }

    private static String reconstruct(Node node) {
        if (node == null) return "";

        if (node instanceof Expressions.Num n) return String.valueOf(n.value);
        if (node instanceof Expressions.BoolLit b) return String.valueOf(b.value);
        if (node instanceof Expressions.IdNode id) return id.name;
        if (node instanceof Expressions.This) return "this";

        if (node instanceof Expressions.Ari a)
            return reconstruct(a.left) + " " + a.op + " " + reconstruct(a.right);
        if (node instanceof Expressions.Rel r)
            return reconstruct(r.left) + " " + r.op + " " + reconstruct(r.right);
        if (node instanceof Expressions.Logical l)
            return reconstruct(l.left) + " " + l.op + " " + reconstruct(l.right);
        if (node instanceof Expressions.Not n)
            return "!" + reconstruct(n.expr);

        if (node instanceof Expressions.MethodCall mc) {
            StringBuilder args = new StringBuilder();
            for (int i = 0; i < mc.args.size(); i++) {
                args.append(reconstruct(mc.args.get(i)));
                if (i < mc.args.size() - 1) args.append(", ");
            }
            return reconstruct(mc.object) + "." + mc.methodName + "(" + args + ")";
        }

        if (node instanceof Expressions.ArrayAccess aa)
            return reconstruct(aa.array) + "[" + reconstruct(aa.index) + "]";
        if (node instanceof Expressions.ArrayLength al)
            return reconstruct(al.array) + ".length";
        if (node instanceof Expressions.NewArray na)
            return "new int[" + reconstruct(na.size) + "]";
        if (node instanceof Expressions.NewObject no)
            return "new " + no.className + "()";


        if (node instanceof Statements.If i) return "if (" + reconstruct(i.cond) + ") { ... }";
        if (node instanceof Statements.While w) return "while (" + reconstruct(w.cond) + ") { ... }";
        if (node instanceof Statements.Assign a)
            return a.id + " = " + reconstruct(a.expr);
        if (node instanceof Statements.ArrayAssign aa)
            return aa.id + "[" + reconstruct(aa.index) + "] = " + reconstruct(aa.value);
        if (node instanceof Statements.Print p)
            return "System.out.println(" + reconstruct(p.expr) + ");";
        if (node instanceof Statements.Return r)
            return "return " + reconstruct(r.expr) + ";";

        return "";
    }

    private static void checkStmt(Stmt s) {
        if (s == null) return;

        Node previousContext = currentContextNode;

        if (!(s instanceof Statements.Seq)) {
            currentContextNode = s;
        }
        if (s instanceof Statements.Seq seq) {
            for (Stmt stmt : seq.stmts) checkStmt(stmt);
        } else if (s instanceof Statements.If i) {
            if (!evalType(i.cond).equals("boolean")) throwError("'if' condition must be boolean", i.cond, "Ensure the condition evaluates to true or false.");
            checkStmt(i.thenStmt);
            checkStmt(i.elseStmt);
        } else if (s instanceof Statements.While w) {
            if (!evalType(w.cond).equals("boolean")) throwError("'while' condition must be boolean", w.cond, "Ensure the condition evaluates to true or false.");
            checkStmt(w.body);
        } else if (s instanceof Statements.Print p) {
            evalType(p.expr);
        } else if (s instanceof Statements.Assign a) {
            String targetType = lookupVarType(a.id, a);
            String exprType = evalType(a.expr);
            if (!isAssignable(targetType, exprType)) {
                throwError("cannot assign " + exprType + " to " + targetType + " variable '" + a.id + "'", a, "Cast the value or change the variable type to match.");
            }
        } else if (s instanceof Statements.ArrayAssign a) {
            if (!lookupVarType(a.id, a).equals("int[]")) throwError("variable '" + a.id + "' is not an array", a, "Only arrays can be indexed with [].");
            if (!evalType(a.index).equals("int")) throwError("array index must evaluate to int", a.index, "Use an integer variable or literal for the index.");
            if (!evalType(a.value).equals("int")) throwError("array assignment requires int value", a.value, "Only ints can be stored in an int array.");
        }
        currentContextNode = previousContext;
    }

    private static String evalType(Expr e) {
        if (e instanceof Expressions.Num) return "int";
        if (e instanceof Expressions.BoolLit) return "boolean";
        if (e instanceof Expressions.This) return currentClass;
        if (e instanceof Expressions.IdNode id) return lookupVarType(id.name, id);

        if (e instanceof Expressions.Ari a) {
            if (evalType(a.left).equals("int") && evalType(a.right).equals("int")) return "int";
            throwError("arithmetic operators require int operands", a, "Check variables inside the arithmetic operation to ensure they are of type 'int'.");
        }
        if (e instanceof Expressions.Rel r) {
            if (evalType(r.left).equals("int") && evalType(r.right).equals("int")) return "boolean";
            throwError("relational operators require int operands", r, "Ensure you are comparing integer values.");
        }
        if (e instanceof Expressions.Logical l) {
            if (evalType(l.left).equals("boolean") && evalType(l.right).equals("boolean")) return "boolean";
            throwError("logical operators require boolean operands", l, "Ensure both sides evaluate to true or false.");
        }
        if (e instanceof Expressions.Not n) {
            if (evalType(n.expr).equals("boolean")) return "boolean";
            throwError("'!' operator requires boolean operand", n, "You can only negate a boolean value.");
        }
        if (e instanceof Expressions.NewArray na) {
            if (!evalType(na.size).equals("int")) throwError("array size must evaluate to int", na.size, "Provide an integer for the array size.");
            return "int[]";
        }
        if (e instanceof Expressions.NewObject no) {
            if (!classTable.containsKey(no.className)) throwError("unknown class '" + no.className + "'", no, "Check the spelling of the class being instantiated.");
            return no.className;
        }
        if (e instanceof Expressions.ArrayAccess aa) {
            if (!evalType(aa.array).equals("int[]")) throwError("array access '[]' requires int[]", aa, "Check the variable type you are trying to index.");
            if (!evalType(aa.index).equals("int")) throwError("array index must be int", aa.index, "Provide an integer for the index.");
            return "int";
        }
        if (e instanceof Expressions.ArrayLength al) {
            if (!evalType(al.array).equals("int[]")) throwError("'.length' requires int[]", al, "The .length property is only available on arrays.");
            return "int";
        }
        if (e instanceof Expressions.MethodCall mc) {
            String objType = evalType(mc.object);
            MethodData mData = lookupMethod(objType, mc.methodName, mc);
            if (mc.args.size() != mData.params.size()) {
                throwError("method '" + mc.methodName + "' expects " + mData.params.size() + " argument(s), but got " + mc.args.size(), mc, "Ensure you are passing the correct amount of arguments.");
            }
            int i = 0;
            for (String paramType : mData.params.values()) {
                String argType = evalType(mc.args.get(i));
                if (!isAssignable(paramType, argType)) {
                    throwError("argument type mismatch in method '" + mc.methodName + "' at position " + (i + 1) + ". Expected " + paramType + ", got " + argType, mc.args.get(i), "Change the argument to match the required type.");
                }
                i++;
            }

            return mData.returnType;
        }

        throwError("unable to evaluate expression type", e, "Check for syntax or type errors in the expression.");
        return null;
    }

    private static String lookupVarType(String id, Node context) {
        MethodData m = classTable.get(currentClass).methods.get(currentMethod);
        if (m.locals.containsKey(id)) return m.locals.get(id);
        if (m.params.containsKey(id)) return m.params.get(id);

        String curr = currentClass;
        while (curr != null && classTable.containsKey(curr)) {
            if (classTable.get(curr).fields.containsKey(id)) return classTable.get(curr).fields.get(id);
            curr = classTable.get(curr).parent;
        }

        throwError("undeclared variable '" + id + "'", context, "Declare the variable before using it, or check for typos.");
        return null;
    }

    private static MethodData lookupMethod(String className, String methodName, Node context) {
        String curr = className;
        while (curr != null && classTable.containsKey(curr)) {
            if (classTable.get(curr).methods.containsKey(methodName)) return classTable.get(curr).methods.get(methodName);
            curr = classTable.get(curr).parent;
        }
        throwError("method '" + methodName + "' not found in class '" + className + "'", context, "Check the method name and the class it belongs to.");
        return null;
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