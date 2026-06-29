package org.compiler.ast;

import java.util.List;

public class Structure {
    public static class Program extends Node {
        public List<ClassNode> classes;
        public Program(List<ClassNode> classes) { this.classes = classes; }
    }

    public static class FieldNode extends Node {
        public String type;
        public String name;
        public FieldNode(String type, String name) { this.type = type; this.name = name; }
    }

    public static class ClassNode extends Node {
        public String name;
        public String parentName; // Null if no inheritance
        public List<FieldNode> fields;
        public List<MethodNode> methods;
        public ClassNode(String name, String parentName, List<FieldNode> fields, List<MethodNode> methods) {
            this.name = name; this.parentName = parentName; this.fields = fields; this.methods = methods;
        }
    }

    public static class MethodNode extends Node {
        public String returnType;
        public String name;
        public List<FieldNode> params;
        public List<FieldNode> localVars;
        public Statements.Seq body;
        public Expr returnExp;

        public MethodNode(String returnType, String name, List<FieldNode> params, List<FieldNode> localVars, Statements.Seq body, Expr returnExp) {
            this.returnType = returnType; this.name = name; this.params = params; this.localVars = localVars; this.body = body; this.returnExp = returnExp;
        }
    }
}