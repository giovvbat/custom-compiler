package org.compiler;

import org.compiler.analyzer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(Lexer.analyze(Files.readString(Path.of(System.getProperty("user.dir"), "assets", "asset-1.txt"))));
    }
}