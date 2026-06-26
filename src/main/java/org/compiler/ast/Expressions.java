package org.compiler.ast;
import java.util.List;

public class Expressions {
    public static class Ari extends Expr {
        public String op; public Expr left; public Expr right;
        public Ari(String op, Expr left, Expr right) { this.op = op; this.left = left; this.right = right; }
    }
    public static class Rel extends Expr {
        public String op; public Expr left; public Expr right;
        public Rel(String op, Expr left, Expr right) { this.op = op; this.left = left; this.right = right; }
    }

    public static class Logical extends Expr {
        public String op; public Expr left; public Expr right;
        public Logical(String op, Expr left, Expr right) { this.op = op; this.left = left; this.right = right; }
    }

    public static class Not extends Expr {
        public Expr expr;
        public Not(Expr expr) { this.expr = expr; }
    }
    public static class IdNode extends Expr {
        public String name;
        public IdNode(String name) { this.name = name; }
    }
    public static class Num extends Expr {
        public int value;
        public Num(int value) { this.value = value; }
    }

    public static class BoolLit extends Expr {
        public boolean value;
        public BoolLit(boolean value) { this.value = value; }
    }

    public static class This extends Expr { }

    public static class NewObject extends Expr {
        public String className;
        public NewObject(String className) { this.className = className; }
    }

    public static class NewArray extends Expr {
        public Expr size;
        public NewArray(Expr size) { this.size = size; }
    }

    public static class ArrayAccess extends Expr {
        public Expr array; public Expr index;
        public ArrayAccess(Expr array, Expr index) { this.array = array; this.index = index; }
    }

    public static class ArrayLength extends Expr {
        public Expr array;
        public ArrayLength(Expr array) { this.array = array; }
    }

    public static class MethodCall extends Expr {
        public Expr object; public String methodName; public List<Expr> args;
        public MethodCall(Expr object, String methodName, List<Expr> args) { this.object = object; this.methodName = methodName; this.args = args; }
    }
}