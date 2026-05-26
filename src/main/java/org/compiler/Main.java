package org.compiler;

import org.compiler.analyzer.Lexer;
import org.compiler.analyzer.Parser;
import org.compiler.domain.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            String input = Files.readString(Path.of(System.getProperty("user.dir"), "assets", "Program5.ling"));

            Parser.parse(Lexer.tokenize(input));
        } catch (RuntimeException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
