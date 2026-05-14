package org.compiler.exception;

public class LexicalErrorException extends RuntimeException {
    public LexicalErrorException(String message) {
        super(message);
    }
}
