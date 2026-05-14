package org.compiler;

import org.compiler.analyzer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        String input = Files.readString(Path.of(System.getProperty("user.dir"), "assets", "asset-1.ling"));

        System.out.println(Lexer.tokenize(Lexer.clean(input)));
    }
}
