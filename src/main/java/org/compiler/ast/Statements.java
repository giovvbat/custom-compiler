package org.compiler.ast;
import java.util.List;

public class Statements {
    public static class Seq extends Stmt {
        public List<Stmt> stmts;
        public Seq(List<Stmt> stmts) { this.stmts = stmts; }
    }
    public static class If extends Stmt {
        public Expr cond; public Stmt thenStmt; public Stmt elseStmt;
        public If(Expr cond, Stmt thenStmt, Stmt elseStmt) { this.cond = cond; this.thenStmt = thenStmt; this.elseStmt = elseStmt; }
    }
    public static class While extends Stmt {
        public Expr cond; public Stmt body;
        public While(Expr cond, Stmt body) { this.cond = cond; this.body = body; }
    }
    public static class Print extends Stmt {
        public Expr expr;
        public Print(Expr expr) { this.expr = expr; }
    }
    public static class Assign extends Stmt {
        public String id; public Expr expr;
        public Assign(String id, Expr expr) { this.id = id; this.expr = expr; }
    }
    public static class ArrayAssign extends Stmt {
        public String id; public Expr index; public Expr value;
        public ArrayAssign(String id, Expr index, Expr value) { this.id = id; this.index = index; this.value = value; }
    }
    public static class Return extends Stmt {
        public Expr expr;
        public Return(Expr expr) { this.expr = expr; }
    }
}