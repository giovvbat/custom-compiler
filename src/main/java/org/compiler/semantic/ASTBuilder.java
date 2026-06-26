package org.compiler.semantic;

import org.compiler.domain.ParseTree;
import org.compiler.domain.Symbol;
import org.compiler.enums.NonTerminalSymbol;
import org.compiler.enums.TerminalSymbol;
import org.compiler.ast.*;
import java.util.ArrayList;
import java.util.List;


public class ASTBuilder {


    public static Node build(ParseTree cstNode) {
        if (cstNode == null) return null;

        if (cstNode.symbol == NonTerminalSymbol.PROG) {
            List<Structure.ClassNode> classes = new ArrayList<>();
            classes.add(buildMainClass(cstNode.children.get(0))); // MAIN_C
            classes.addAll(buildClasses(cstNode.children.get(1))); // DEF_CL
            return new Structure.Program(classes);
        }
        if (cstNode.symbol == NonTerminalSymbol.CMD) {
            return buildStmt(cstNode);
        } else if (cstNode.symbol == NonTerminalSymbol.EXP) {
            return buildExpr(cstNode);
        }
        return null;
    }
    private static ParseTree findChildBySymbolDeep(ParseTree parent, Symbol target) {
        if (parent == null) return null;
        if (parent.symbol == target) return parent;
        for (ParseTree child : parent.children) {
            ParseTree result = findChildBySymbolDeep(child, target);
            if (result != null) return result;
        }
        return null;
    }

    private static ParseTree findReturnExp(ParseTree parent) {
        if (parent == null) return null;
        for (int i = 0; i < parent.children.size() - 1; i++) {
            if (parent.children.get(i).symbol == TerminalSymbol.RETURN) {
                return parent.children.get(i + 1);
            }
        }
        for (ParseTree child : parent.children) {
            ParseTree res = findReturnExp(child);
            if (res != null) return res;
        }
        return null;
    }
    private static Structure.ClassNode buildMainClass(ParseTree mainNode) {
        String name = mainNode.children.get(1).token.lexeme();

        ParseTree cmdsNode = findChildBySymbolDeep(mainNode, NonTerminalSymbol.CMDS);
        List<Stmt> stmts = findStmts(cmdsNode);

        Structure.MethodNode mainMethod = new Structure.MethodNode("main", new Statements.Seq(stmts), null);
        return new Structure.ClassNode(name, List.of(mainMethod));
    }

    private static List<Structure.ClassNode> buildClasses(ParseTree defClNode) {
        List<Structure.ClassNode> classes = new ArrayList<>();
        if (defClNode.children.isEmpty() || defClNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return classes;

        String name = defClNode.children.get(1).token.lexeme();
        ParseTree rest = defClNode.children.get(2);

        ParseTree methodsNode = rest.children.get(0).symbol == TerminalSymbol.EXTENDS ? rest.children.get(4) : rest.children.get(2);
        ParseTree nextClass = rest.children.get(0).symbol == TerminalSymbol.EXTENDS ? rest.children.get(6) : rest.children.get(4);

        List<Structure.MethodNode> methods = buildMethods(methodsNode);
        classes.add(new Structure.ClassNode(name, methods));
        classes.addAll(buildClasses(nextClass));
        return classes;
    }

    private static List<Structure.MethodNode> buildMethods(ParseTree defMetNode) {
        List<Structure.MethodNode> methods = new ArrayList<>();
        if (defMetNode.children.isEmpty() || defMetNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return methods;

        String name = defMetNode.children.get(2).token.lexeme();

        ParseTree varsNode = findChildBySymbolDeep(defMetNode, NonTerminalSymbol.VARS_THEN_CMDS);
        ParseTree expNode = findReturnExp(defMetNode);

        List<Stmt> stmts = findStmts(varsNode);
        Expr returnExp = buildExpr(expNode);

        methods.add(new Structure.MethodNode(name, new Statements.Seq(stmts), returnExp));
        methods.addAll(buildMethods(defMetNode.children.get(12)));
        return methods;
    }

    private static Stmt buildStmt(ParseTree cmdNode) {
        ParseTree firstChild = cmdNode.children.get(0);

        if (firstChild.symbol == TerminalSymbol.CURLY_BRACKET_LEFT) {
            return new Statements.Seq(findStmts(cmdNode.children.get(1)));
        }
        if (firstChild.symbol == TerminalSymbol.SYSTEM_OUT_PRINTLN) {
            return new Statements.Print(buildExpr(cmdNode.children.get(2)));
        }
        if (firstChild.symbol == TerminalSymbol.WHILE) {
            Expr cond = buildExpr(cmdNode.children.get(2));
            Stmt body = buildStmt(cmdNode.children.get(4));
            return new Statements.While(cond, body);
        }
        if (firstChild.symbol == TerminalSymbol.IF) {
            Expr cond = buildExpr(cmdNode.children.get(2));
            Stmt thenStmt = buildStmt(cmdNode.children.get(4));
            ParseTree cmdIfRest = cmdNode.children.get(5);
            Stmt elseStmt = cmdIfRest.children.isEmpty() ? null : buildStmt(cmdIfRest.children.get(1));
            return new Statements.If(cond, thenStmt, elseStmt);
        }
        if (firstChild.symbol == TerminalSymbol.ID) {
            String id = firstChild.token.lexeme();
            ParseTree cmdIdRest = cmdNode.children.get(1);
            ParseTree firstRest = cmdIdRest.children.get(0);

            if (firstRest.symbol == TerminalSymbol.EQUALS) {

                return new Statements.Assign(id, buildExpr(cmdIdRest.children.get(1)));
            } else if (firstRest.symbol == TerminalSymbol.SQUARE_BRACKET_LEFT) {

                Expr index = buildExpr(cmdIdRest.children.get(1));
                Expr value = buildExpr(cmdIdRest.children.get(4));
                return new Statements.ArrayAssign(id, index, value);
            }
        }
        return null;
    }

    private static Expr buildExpr(ParseTree expNode) {
        return buildAndExp(expNode.children.get(0));
    }

    // Level 1: &&
    private static Expr buildAndExp(ParseTree node) {
        Expr left = buildRelExp(node.children.get(0));
        return chainAndExp(left, node.children.get(1));
    }
    private static Expr chainAndExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty()) return left;
        Expr right = buildRelExp(restNode.children.get(1));
        Expr newLeft = new Expressions.Logical("&&", left, right);
        return chainAndExp(newLeft, restNode.children.get(2));
    }

    // Level 2: <
    private static Expr buildRelExp(ParseTree node) {
        Expr left = buildAddExp(node.children.get(0));
        return chainRelExp(left, node.children.get(1));
    }
    private static Expr chainRelExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty()) return left;
        Expr right = buildAddExp(restNode.children.get(1));
        Expr newLeft = new Expressions.Rel("<", left, right);
        return chainRelExp(newLeft, restNode.children.get(2));
    }

    // Level 3: + / -
    private static Expr buildAddExp(ParseTree node) {
        Expr left = buildMulExp(node.children.get(0));
        ParseTree restNode = node.children.get(1);

        while (!restNode.children.isEmpty()) {
            String op = restNode.children.get(0).token.lexeme();
            Expr right = buildMulExp(restNode.children.get(1));
            left = new Expressions.Ari(op, left, right);
            restNode = restNode.children.get(2);
        }
        return left;
    }

    // Level 4: *
    private static Expr buildMulExp(ParseTree node) {
        Expr left = buildUnExp(node.children.get(0));
        return chainMulExp(left, node.children.get(1));
    }
    private static Expr chainMulExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty()) return left;
        Expr right = buildUnExp(restNode.children.get(1));
        Expr newLeft = new Expressions.Ari("*", left, right);
        return chainMulExp(newLeft, restNode.children.get(2));
    }

    // Level 5: !
    private static Expr buildUnExp(ParseTree node) {
        if (node.children.get(0).symbol == TerminalSymbol.NOT) {
            return new Expressions.Not(buildUnExp(node.children.get(1)));
        }
        return buildPsfExp(node.children.get(0));
    }

    // Level 6: [ ] / .length / .Id()
    private static Expr buildPsfExp(ParseTree node) {
        Expr left = buildPriExp(node.children.get(0));
        return chainPsfExp(left, node.children.get(1));
    }
    private static Expr chainPsfExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty()) return left;

        ParseTree firstToken = restNode.children.get(0);
        Expr newLeft;

        if (firstToken.symbol == TerminalSymbol.SQUARE_BRACKET_LEFT) {
            // '[' EXP ']' PSF_EXP_REST
            Expr index = buildExpr(restNode.children.get(1));
            newLeft = new Expressions.ArrayAccess(left, index);
            return chainPsfExp(newLeft, restNode.children.get(3));

        } else if (firstToken.symbol == TerminalSymbol.DOT) {
            // We are looking at: '.' DOT_REST
            ParseTree dotRest = restNode.children.get(1);
            ParseTree firstDotChild = dotRest.children.get(0);

            if (firstDotChild.symbol == TerminalSymbol.LENGTH) {
                // 'length' PSF_EXP_REST
                newLeft = new Expressions.ArrayLength(left);
                return chainPsfExp(newLeft, dotRest.children.get(1));
            } else {
                // Id '(' L_EXP ')' PSF_EXP_REST
                String methodName = firstDotChild.token.lexeme();
                List<Expr> args = buildLExp(dotRest.children.get(2));
                newLeft = new Expressions.MethodCall(left, methodName, args);
                return chainPsfExp(newLeft, dotRest.children.get(4));
            }
        }
        return left;
    }

    // Level 7: Base values
    private static Expr buildPriExp(ParseTree node) {
        ParseTree firstChild = node.children.get(0);

        if (firstChild.symbol == TerminalSymbol.PAREN_LEFT) return buildExpr(node.children.get(1));
        if (firstChild.symbol == TerminalSymbol.NUMBER) return new Expressions.Num(Integer.parseInt(firstChild.token.lexeme()));
        if (firstChild.symbol == TerminalSymbol.ID) return new Expressions.IdNode(firstChild.token.lexeme());
        if (firstChild.symbol == TerminalSymbol.TRUE) return new Expressions.BoolLit(true);
        if (firstChild.symbol == TerminalSymbol.FALSE) return new Expressions.BoolLit(false);
        if (firstChild.symbol == TerminalSymbol.THIS) return new Expressions.This();

        if (firstChild.symbol == TerminalSymbol.NEW) {
            ParseTree secondChild = node.children.get(1);
            if (secondChild.symbol == TerminalSymbol.ID) {
                return new Expressions.NewObject(secondChild.token.lexeme());
            } else if (secondChild.symbol == TerminalSymbol.INT_TYPE) {
                return new Expressions.NewArray(buildExpr(node.children.get(3)));
            }
        }
        return null;
    }


    private static List<Expr> buildLExp(ParseTree lexpNode) {
        List<Expr> args = new ArrayList<>();
        if (lexpNode.children.isEmpty()) return args;

        args.add(buildExpr(lexpNode.children.get(0)));
        ParseTree restListExp = lexpNode.children.get(1);

        while (!restListExp.children.isEmpty()) {
            args.add(buildExpr(restListExp.children.get(1)));
            restListExp = restListExp.children.get(2);
        }
        return args;
    }

    private static List<Stmt> findStmts(ParseTree node) {
        List<Stmt> stmts = new ArrayList<>();
        if (node == null) return stmts;

        if (node.symbol == NonTerminalSymbol.CMD || node.symbol == NonTerminalSymbol.NON_ID_CMD) {
            Stmt s = buildStmt(node);
            if (s != null) stmts.add(s);
            return stmts;
        }

        if (node.symbol == NonTerminalSymbol.VARS_THEN_CMDS && !node.children.isEmpty()) {
            ParseTree firstChild = node.children.get(0);
            if (firstChild.symbol == TerminalSymbol.ID) {
                String id = firstChild.token.lexeme();
                ParseTree idStartRest = node.children.get(1);
                ParseTree firstRest = idStartRest.children.get(0);
                if (firstRest.symbol == NonTerminalSymbol.CMD_ID_REST) {
                    ParseTree cmdIdRestFirst = firstRest.children.get(0);
                    if (cmdIdRestFirst.symbol == TerminalSymbol.EQUALS) {
                        stmts.add(new Statements.Assign(id, buildExpr(firstRest.children.get(1))));
                    } else {
                        stmts.add(new Statements.ArrayAssign(id, buildExpr(firstRest.children.get(1)), buildExpr(firstRest.children.get(4))));
                    }
                }
            }
        }

        for (ParseTree child : node.children) {
            stmts.addAll(findStmts(child));
        }

        return stmts;
    }
}