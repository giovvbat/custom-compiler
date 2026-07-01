package org.compiler.semantic;

import org.compiler.domain.ParseTree;
import org.compiler.domain.Symbol;
import org.compiler.domain.Token;
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
            return withPos(new Structure.Program(classes), cstNode);
        }
        if (cstNode.symbol == NonTerminalSymbol.CMD) {
            return buildStmt(cstNode);
        } else if (cstNode.symbol == NonTerminalSymbol.EXP) {
            return buildExpr(cstNode);
        }
        return null;
    }

    private static <T extends Node> T withPos(T node, ParseTree pt) {
        if (node != null && pt != null) {
            Token t = findFirstToken(pt);
            if (t != null) {
                node.line = t.line();
                node.column = t.column();
            }
        }
        return node;
    }

    private static Token findFirstToken(ParseTree pt) {
        if (pt == null) return null;
        if (pt.token != null) return pt.token;
        for (ParseTree child : pt.children) {
            Token t = findFirstToken(child);
            if (t != null) return t;
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

    private static String getTypeString(ParseTree typeNode) {
        ParseTree first = typeNode.children.get(0);
        if (first.symbol == TerminalSymbol.INT_TYPE) {
            if (typeNode.children.get(1).children.isEmpty() || typeNode.children.get(1).children.get(0).symbol == NonTerminalSymbol.EMPTY) return "int";
            return "int[]";
        }
        if (first.symbol == TerminalSymbol.BOOLEAN_TYPE) return "boolean";
        return first.token.lexeme(); // Object ID
    }

    private static Structure.ClassNode buildMainClass(ParseTree mainNode) {
        String name = mainNode.children.get(1).token.lexeme();
        ParseTree cmdsNode = findChildBySymbolDeep(mainNode, NonTerminalSymbol.CMDS);
        List<Stmt> stmts = findStmts(cmdsNode);

        String argName = mainNode.children.get(9).token.lexeme();
        List<Structure.FieldNode> params = List.of(withPos(new Structure.FieldNode("String[]", argName), mainNode.children.get(9)));

        Structure.MethodNode mainMethod = withPos(new Structure.MethodNode("void", "main", params, new ArrayList<>(), withPos(new Statements.Seq(stmts), cmdsNode), null), mainNode);
        return withPos(new Structure.ClassNode(name, null, new ArrayList<>(), List.of(mainMethod)), mainNode);
    }

    private static List<Structure.ClassNode> buildClasses(ParseTree defClNode) {
        List<Structure.ClassNode> classes = new ArrayList<>();
        if (defClNode.children.isEmpty() || defClNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return classes;

        String name = defClNode.children.get(1).token.lexeme();
        ParseTree rest = defClNode.children.get(2);

        boolean hasExtends = rest.children.get(0).symbol == TerminalSymbol.EXTENDS;
        String parentName = hasExtends ? rest.children.get(1).token.lexeme() : null;

        ParseTree varsNode = hasExtends ? rest.children.get(3) : rest.children.get(1);
        ParseTree methodsNode = hasExtends ? rest.children.get(4) : rest.children.get(2);
        ParseTree nextClass = hasExtends ? rest.children.get(6) : rest.children.get(4);

        List<Structure.FieldNode> fields = buildFields(varsNode);
        List<Structure.MethodNode> methods = buildMethods(methodsNode);

        classes.add(withPos(new Structure.ClassNode(name, parentName, fields, methods), defClNode));
        classes.addAll(buildClasses(nextClass));
        return classes;
    }

    private static List<Structure.FieldNode> buildFields(ParseTree defVarNode) {
        List<Structure.FieldNode> fields = new ArrayList<>();
        while (defVarNode != null && !defVarNode.children.isEmpty() && defVarNode.children.get(0).symbol != NonTerminalSymbol.EMPTY) {
            fields.add(withPos(new Structure.FieldNode(getTypeString(defVarNode.children.get(0)), defVarNode.children.get(1).token.lexeme()), defVarNode));
            defVarNode = defVarNode.children.get(3);
        }
        return fields;
    }

    private static List<Structure.MethodNode> buildMethods(ParseTree defMetNode) {
        List<Structure.MethodNode> methods = new ArrayList<>();
        if (defMetNode.children.isEmpty() || defMetNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return methods;

        String returnType = getTypeString(defMetNode.children.get(1));
        String name = defMetNode.children.get(2).token.lexeme();

        List<Structure.FieldNode> params = buildArgs(defMetNode.children.get(4));
        ParseTree varsNode = findChildBySymbolDeep(defMetNode, NonTerminalSymbol.VARS_THEN_CMDS);
        List<Structure.FieldNode> locals = buildLocalVars(varsNode);

        ParseTree expNode = findReturnExp(defMetNode);
        List<Stmt> stmts = findStmts(varsNode);
        Expr returnExp = buildExpr(expNode);

        methods.add(withPos(new Structure.MethodNode(returnType, name, params, locals, withPos(new Statements.Seq(stmts), varsNode), returnExp), defMetNode));
        methods.addAll(buildMethods(defMetNode.children.get(12)));
        return methods;
    }

    private static List<Structure.FieldNode> buildArgs(ParseTree argsNode) {
        List<Structure.FieldNode> params = new ArrayList<>();
        if (argsNode.children.isEmpty() || argsNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return params;
        params.add(withPos(new Structure.FieldNode(getTypeString(argsNode.children.get(0)), argsNode.children.get(1).token.lexeme()), argsNode));
        ParseTree rest = argsNode.children.get(2);
        while (!rest.children.isEmpty() && rest.children.get(0).symbol != NonTerminalSymbol.EMPTY) {
            params.add(withPos(new Structure.FieldNode(getTypeString(rest.children.get(1)), rest.children.get(2).token.lexeme()), rest));
            rest = rest.children.get(3);
        }
        return params;
    }

    private static List<Structure.FieldNode> buildLocalVars(ParseTree varsNode) {
        List<Structure.FieldNode> locals = new ArrayList<>();
        ParseTree curr = varsNode;
        while (curr != null && !curr.children.isEmpty() && curr.children.get(0).symbol != NonTerminalSymbol.EMPTY) {
            Symbol first = curr.children.get(0).symbol;
            if (first == TerminalSymbol.INT_TYPE) {
                String type = curr.children.get(1).children.isEmpty() || curr.children.get(1).children.get(0).symbol == NonTerminalSymbol.EMPTY ? "int" : "int[]";
                locals.add(withPos(new Structure.FieldNode(type, curr.children.get(2).token.lexeme()), curr));
                curr = curr.children.get(4);
            } else if (first == TerminalSymbol.BOOLEAN_TYPE) {
                locals.add(withPos(new Structure.FieldNode("boolean", curr.children.get(1).token.lexeme()), curr));
                curr = curr.children.get(3);
            } else if (first == TerminalSymbol.ID) {
                ParseTree idStartRest = curr.children.get(1);
                if (idStartRest.children.get(0).symbol == TerminalSymbol.ID) {
                    String type = curr.children.get(0).token.lexeme();
                    String name = idStartRest.children.get(0).token.lexeme();
                    locals.add(withPos(new Structure.FieldNode(type, name), curr));
                    curr = idStartRest.children.get(2);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return locals;
    }

    private static List<Stmt> findStmts(ParseTree node) {
        List<Stmt> stmts = new ArrayList<>();
        if (node == null || node.children.isEmpty() || node.children.get(0).symbol == NonTerminalSymbol.EMPTY) return stmts;

        if (node.symbol == NonTerminalSymbol.CMD || node.symbol == NonTerminalSymbol.NON_ID_CMD) {
            Stmt s = buildStmt(node);
            if (s != null) stmts.add(s);
            if (node.children.size() > 1 && node.children.get(1).symbol == NonTerminalSymbol.CMDS) {
                stmts.addAll(findStmts(node.children.get(1)));
            }
            return stmts;
        }

        if (node.symbol == NonTerminalSymbol.VARS_THEN_CMDS) {
            ParseTree firstChild = node.children.get(0);
            if (firstChild.symbol == TerminalSymbol.INT_TYPE) {
                stmts.addAll(findStmts(node.children.get(4)));
            } else if (firstChild.symbol == TerminalSymbol.BOOLEAN_TYPE) {
                stmts.addAll(findStmts(node.children.get(3)));
            } else if (firstChild.symbol == TerminalSymbol.ID) {
                String id = firstChild.token.lexeme();
                ParseTree idStartRest = node.children.get(1);
                ParseTree firstRest = idStartRest.children.get(0);
                if (firstRest.symbol == NonTerminalSymbol.CMD_ID_REST) {
                    ParseTree cmdIdRestFirst = firstRest.children.get(0);
                    if (cmdIdRestFirst.symbol == TerminalSymbol.EQUALS) {
                        stmts.add(withPos(new Statements.Assign(id, buildExpr(firstRest.children.get(1))), firstChild));
                    } else {
                        stmts.add(withPos(new Statements.ArrayAssign(id, buildExpr(firstRest.children.get(1)), buildExpr(firstRest.children.get(4))), firstChild));
                    }
                    stmts.addAll(findStmts(idStartRest.children.get(1)));
                }
            } else if (firstChild.symbol == NonTerminalSymbol.NON_ID_CMD) {
                stmts.addAll(findStmts(firstChild));
                stmts.addAll(findStmts(node.children.get(1)));
            }
            return stmts;
        }

        for (ParseTree child : node.children) {
            stmts.addAll(findStmts(child));
        }

        return stmts;
    }

    private static Stmt buildStmt(ParseTree cmdNode) {
        ParseTree firstChild = cmdNode.children.get(0);

        if (firstChild.symbol == TerminalSymbol.CURLY_BRACKET_LEFT) {
            return withPos(new Statements.Seq(findStmts(cmdNode.children.get(1))), cmdNode);
        }
        if (firstChild.symbol == TerminalSymbol.SYSTEM_OUT_PRINTLN) {
            return withPos(new Statements.Print(buildExpr(cmdNode.children.get(2))), cmdNode);
        }
        if (firstChild.symbol == TerminalSymbol.WHILE) {
            Expr cond = buildExpr(cmdNode.children.get(2));
            Stmt body = buildStmt(cmdNode.children.get(4));
            return withPos(new Statements.While(cond, body), cmdNode);
        }
        if (firstChild.symbol == TerminalSymbol.IF) {
            Expr cond = buildExpr(cmdNode.children.get(2));
            Stmt thenStmt = buildStmt(cmdNode.children.get(4));
            ParseTree cmdIfRest = cmdNode.children.get(5);
            Stmt elseStmt = cmdIfRest.children.isEmpty() || cmdIfRest.children.get(0).symbol == NonTerminalSymbol.EMPTY ? null : buildStmt(cmdIfRest.children.get(1));
            return withPos(new Statements.If(cond, thenStmt, elseStmt), cmdNode);
        }
        if (firstChild.symbol == TerminalSymbol.ID) {
            String id = firstChild.token.lexeme();
            ParseTree cmdIdRest = cmdNode.children.get(1);
            ParseTree firstRest = cmdIdRest.children.get(0);

            if (firstRest.symbol == TerminalSymbol.EQUALS) {
                return withPos(new Statements.Assign(id, buildExpr(cmdIdRest.children.get(1))), cmdNode);
            } else if (firstRest.symbol == TerminalSymbol.SQUARE_BRACKET_LEFT) {
                Expr index = buildExpr(cmdIdRest.children.get(1));
                Expr value = buildExpr(cmdIdRest.children.get(4));
                return withPos(new Statements.ArrayAssign(id, index, value), cmdNode);
            }
        }
        return null;
    }

    private static Expr buildExpr(ParseTree expNode) {
        return buildAndExp(expNode.children.get(0));
    }

    private static Expr buildAndExp(ParseTree node) {
        Expr left = buildRelExp(node.children.get(0));
        return chainAndExp(left, node.children.get(1));
    }
    private static Expr chainAndExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty() || restNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return left;
        Expr right = buildRelExp(restNode.children.get(1));
        Expr newLeft = withPos(new Expressions.Logical("&&", left, right), restNode);
        return chainAndExp(newLeft, restNode.children.get(2));
    }

    private static Expr buildRelExp(ParseTree node) {
        Expr left = buildAddExp(node.children.get(0));
        return chainRelExp(left, node.children.get(1));
    }
    private static Expr chainRelExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty() || restNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return left;
        Expr right = buildAddExp(restNode.children.get(1));
        Expr newLeft = withPos(new Expressions.Rel("<", left, right), restNode);
        return chainRelExp(newLeft, restNode.children.get(2));
    }

    private static Expr buildAddExp(ParseTree node) {
        Expr left = buildMulExp(node.children.get(0));
        ParseTree restNode = node.children.get(1);

        while (!restNode.children.isEmpty() && restNode.children.get(0).symbol != NonTerminalSymbol.EMPTY) {
            String op = restNode.children.get(0).token.lexeme();
            Expr right = buildMulExp(restNode.children.get(1));
            left = withPos(new Expressions.Ari(op, left, right), restNode);
            restNode = restNode.children.get(2);
        }
        return left;
    }

    private static Expr buildMulExp(ParseTree node) {
        Expr left = buildUnExp(node.children.get(0));
        return chainMulExp(left, node.children.get(1));
    }
    private static Expr chainMulExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty() || restNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return left;
        Expr right = buildUnExp(restNode.children.get(1));
        Expr newLeft = withPos(new Expressions.Ari("*", left, right), restNode);
        return chainMulExp(newLeft, restNode.children.get(2));
    }

    private static Expr buildUnExp(ParseTree node) {
        if (node.children.get(0).symbol == TerminalSymbol.NOT) {
            return withPos(new Expressions.Not(buildUnExp(node.children.get(1))), node);
        }
        return buildPsfExp(node.children.get(0));
    }

    private static Expr buildPsfExp(ParseTree node) {
        Expr left = buildPriExp(node.children.get(0));
        return chainPsfExp(left, node.children.get(1));
    }
    private static Expr chainPsfExp(Expr left, ParseTree restNode) {
        if (restNode.children.isEmpty() || restNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return left;

        ParseTree firstToken = restNode.children.get(0);
        Expr newLeft;

        if (firstToken.symbol == TerminalSymbol.SQUARE_BRACKET_LEFT) {
            Expr index = buildExpr(restNode.children.get(1));
            newLeft = withPos(new Expressions.ArrayAccess(left, index), restNode);
            return chainPsfExp(newLeft, restNode.children.get(3));

        } else if (firstToken.symbol == TerminalSymbol.DOT) {
            ParseTree dotRest = restNode.children.get(1);
            ParseTree firstDotChild = dotRest.children.get(0);

            if (firstDotChild.symbol == TerminalSymbol.LENGTH) {
                newLeft = withPos(new Expressions.ArrayLength(left), dotRest);
                return chainPsfExp(newLeft, dotRest.children.get(1));
            } else {
                String methodName = firstDotChild.token.lexeme();
                List<Expr> args = buildLExp(dotRest.children.get(2));
                newLeft = withPos(new Expressions.MethodCall(left, methodName, args), dotRest);
                return chainPsfExp(newLeft, dotRest.children.get(4));
            }
        }
        return left;
    }

    private static Expr buildPriExp(ParseTree node) {
        ParseTree firstChild = node.children.get(0);

        if (firstChild.symbol == TerminalSymbol.PAREN_LEFT) return buildExpr(node.children.get(1));
        if (firstChild.symbol == TerminalSymbol.NUMBER) return withPos(new Expressions.Num(Integer.parseInt(firstChild.token.lexeme())), node);
        if (firstChild.symbol == TerminalSymbol.ID) return withPos(new Expressions.IdNode(firstChild.token.lexeme()), node);
        if (firstChild.symbol == TerminalSymbol.TRUE) return withPos(new Expressions.BoolLit(true), node);
        if (firstChild.symbol == TerminalSymbol.FALSE) return withPos(new Expressions.BoolLit(false), node);
        if (firstChild.symbol == TerminalSymbol.THIS) return withPos(new Expressions.This(), node);

        if (firstChild.symbol == TerminalSymbol.NEW) {
            ParseTree secondChild = node.children.get(1);
            if (secondChild.symbol == TerminalSymbol.ID) {
                return withPos(new Expressions.NewObject(secondChild.token.lexeme()), node);
            } else if (secondChild.symbol == TerminalSymbol.INT_TYPE) {
                return withPos(new Expressions.NewArray(buildExpr(node.children.get(3))), node);
            }
        }
        return null;
    }

    private static List<Expr> buildLExp(ParseTree lexpNode) {
        List<Expr> args = new ArrayList<>();
        if (lexpNode.children.isEmpty() || lexpNode.children.get(0).symbol == NonTerminalSymbol.EMPTY) return args;

        args.add(buildExpr(lexpNode.children.get(0)));
        ParseTree restListExp = lexpNode.children.get(1);

        while (!restListExp.children.isEmpty() && restListExp.children.get(0).symbol != NonTerminalSymbol.EMPTY) {
            args.add(buildExpr(restListExp.children.get(1)));
            restListExp = restListExp.children.get(2);
        }
        return args;
    }
}