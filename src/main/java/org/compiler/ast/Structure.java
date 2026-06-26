package org.compiler.ast;

import java.util.List;

public class Structure {
    public static class Program extends Node {
        public List<ClassNode> classes;
        public Program(List<ClassNode> classes) { this.classes = classes; }
    }

    public static class ClassNode extends Node {
        public String name;
        public List<MethodNode> methods;
        public ClassNode(String name, List<MethodNode> methods) { this.name = name; this.methods = methods; }
    }

    public static class MethodNode extends Node {
        public String name;
        public Statements.Seq body;
        public Expr returnExp;
        public MethodNode(String name, Statements.Seq body, Expr returnExp) { this.name = name; this.body = body; this.returnExp = returnExp; }
    }
}