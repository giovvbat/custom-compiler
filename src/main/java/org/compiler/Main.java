package org.compiler;

import org.compiler.analyzer.Lexer;
import org.compiler.analyzer.Parser;
import org.compiler.domain.ParseTree;
import org.compiler.domain.Token;
import org.compiler.semantic.ASTBuilder;
import org.compiler.semantic.ASTPrinter;
import org.compiler.ast.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static List<String> errors = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        List<String> argsList = Arrays.asList(args);

        boolean printTokens = argsList.contains("-tokens");
        boolean stopFirstError = argsList.contains("-stop-first-error");
        boolean showSuggestions = argsList.contains("-suggestions");
        boolean printAST = argsList.contains("-ast");

        try {
            String input = Files.readString(Path.of(System.getProperty("user.dir"), "assets", "asset-1.ling"));

            // 1. Lexical Analysis
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

            // 2. Syntax Analysis -> Builds CST
            ParseTree cstRoot = Parser.parse(tokens, showSuggestions);

            // 3. Build AST from CST
            Node astRoot = ASTBuilder.build(cstRoot);

            if (printAST) {
                System.out.println("\n=== ABSTRACT SYNTAX TREE ===");
                ASTPrinter.print(astRoot);
            }

        } catch (RuntimeException exception) {
            System.err.println(exception.getMessage());
        }
    }
}