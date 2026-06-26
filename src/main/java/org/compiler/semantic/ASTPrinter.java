package org.compiler.semantic;

import org.compiler.ast.Expressions;
import org.compiler.ast.Node;
import org.compiler.ast.Statements;
import org.compiler.ast.Structure;

import java.util.ArrayList;
import java.util.List;

public class ASTPrinter {

    // Main entry point
    public static void print(Node node) {
        print(node, "", true);
    }

    // Recursive tree drawer
    private static void print(Node node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "└── " : "├── ") + getNodeString(node));

        List<Node> children = getChildren(node);
        for (int i = 0; i < children.size() - 1; i++) {
            print(children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }
        if (!children.isEmpty()) {
            print(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true);
        }
    }


    private static String getNodeString(Node node) {
        if (node instanceof Statements.Seq) return "Block (Seq)";
        if (node instanceof Statements.If) return "If";
        if (node instanceof Statements.While) return "While";
        if (node instanceof Statements.Assign a) return "Assign: " + a.id;
        if (node instanceof Statements.ArrayAssign a) return "ArrayAssign: " + a.id;
        if (node instanceof Statements.Print) return "Print";
        if (node instanceof Structure.Program) return "Program";
        if (node instanceof Structure.ClassNode c) return "Class: " + c.name;
        if (node instanceof Structure.MethodNode m) return "Method: " + m.name;

        if (node instanceof Expressions.Ari a) return "AriOp '" + a.op + "'";
        if (node instanceof Expressions.Rel r) return "RelOp '" + r.op + "'";
        if (node instanceof Expressions.Logical l) return "Logical '" + l.op + "'";
        if (node instanceof Expressions.Num n) return "Num: " + n.value;
        if (node instanceof Expressions.IdNode id) return "Id: " + id.name;
        if (node instanceof Expressions.BoolLit b) return "Bool: " + b.value;
        if (node instanceof Expressions.Not) return "Not (!)";
        if (node instanceof Expressions.MethodCall m) return "MethodCall: " + m.methodName;
        if (node instanceof Expressions.NewObject n) return "NewObject: " + n.className;
        if (node instanceof Expressions.NewArray) return "NewArray";
        if (node instanceof Expressions.ArrayAccess) return "ArrayAccess";
        if (node instanceof Expressions.ArrayLength) return "ArrayLength";
        if (node instanceof Expressions.This) return "This";
        if (node instanceof Statements.Return) return "Return";


        return node.getClass().getSimpleName();
    }


    private static List<Node> getChildren(Node node) {
        List<Node> children = new ArrayList<>();

        if (node instanceof Structure.Program p) children.addAll(p.classes);
        else if (node instanceof Structure.ClassNode c) children.addAll(c.methods);

        else if (node instanceof Structure.MethodNode m) {
            if (m.body != null) children.add(m.body);
            if (m.returnExp != null) children.add(new Statements.Return(m.returnExp));
        }

        else if (node instanceof Statements.Seq s) {
            if (s.stmts != null) children.addAll(s.stmts);
        }

        else if (node instanceof Statements.If i) {
            if (i.cond != null) children.add(i.cond);
            if (i.thenStmt != null) children.add(i.thenStmt);
            if (i.elseStmt != null) children.add(i.elseStmt);
        }
        else if (node instanceof Statements.While w) {
            if (w.cond != null) children.add(w.cond);
            if (w.body != null) children.add(w.body);
        }
        else if (node instanceof Statements.Assign a) {
            if (a.expr != null) children.add(a.expr);
        }
        else if (node instanceof Statements.ArrayAssign a) {
            if (a.index != null) children.add(a.index);
            if (a.value != null) children.add(a.value);
        }
        else if (node instanceof Statements.Print p) {
            if (p.expr != null) children.add(p.expr);
        }
        else if (node instanceof Expressions.Ari a) {
            if (a.left != null) children.add(a.left);
            if (a.right != null) children.add(a.right);
        }
        else if (node instanceof Expressions.Rel r) {
            if (r.left != null) children.add(r.left);
            if (r.right != null) children.add(r.right);
        }
        else if (node instanceof Expressions.Logical l) {
            if (l.left != null) children.add(l.left);
            if (l.right != null) children.add(l.right);
        }
        else if (node instanceof Expressions.Not n) {
            if (n.expr != null) children.add(n.expr);
        }
        else if (node instanceof Expressions.MethodCall m) {
            if (m.object != null) children.add(m.object);
            children.addAll(m.args);
        }
        else if (node instanceof Expressions.NewArray n) {
            if (n.size != null) children.add(n.size);
        }
        else if (node instanceof Expressions.ArrayAccess a) {
            if (a.array != null) children.add(a.array);
            if (a.index != null) children.add(a.index);
        }
        else if (node instanceof Expressions.ArrayLength a) {
            if (a.array != null) children.add(a.array);
        }
        else if (node instanceof Statements.Return r) {
            if (r.expr != null) children.add(r.expr);
        }

        return children;
    }
}