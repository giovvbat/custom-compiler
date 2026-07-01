package org.compiler;

import org.compiler.analyzer.Lexer;
import org.compiler.analyzer.Parser;
import org.compiler.domain.ParseTree;
import org.compiler.domain.Token;
import org.compiler.semantic.ASTBuilder;
import org.compiler.semantic.ASTPrinter;
import org.compiler.semantic.SemanticAnalyzer;
import org.compiler.ast.Node;
import org.compiler.ast.Structure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> argsList = Arrays.asList(args);

        boolean printTokens = argsList.contains("-tokens");
        boolean stopFirstError = argsList.contains("-stop-first-error");
        boolean showSuggestions = argsList.contains("-suggestions");
        boolean printAST = argsList.contains("-ast");
        boolean printSymTable = argsList.contains("-symtable");

        try {
            String fileName = "test.ling";
            String input = Files.readString(Path.of(System.getProperty("user.dir"), "assets", fileName));

            List<Token> tokens = Lexer.tokenize(input, stopFirstError, showSuggestions);

            if (!stopFirstError && !Lexer.errors.isEmpty()) {
                Lexer.errors.forEach(System.err::println);
                System.err.println("Compilation halted due to lexical errors.");
                return;
            }

            if (printTokens) {
                System.out.println("=== TOKENS ===");
                tokens.forEach(System.out::println);
            }

            ParseTree cstRoot = Parser.parse(tokens, showSuggestions);

            if (printSymTable) {
                Parser.symbolTable.printTable();
            }
            Node astRoot = ASTBuilder.build(cstRoot);

            if (printAST) {
                System.out.println("\n=== ABSTRACT SYNTAX TREE ===");
                ASTPrinter.print(astRoot);
            }

            // 4. Semantic Analysis & Symbol Table Output
            SemanticAnalyzer.analyze((Structure.Program) astRoot, fileName, showSuggestions);
        } catch (RuntimeException exception) {
            System.err.println(exception.getMessage());
        }
    }
}