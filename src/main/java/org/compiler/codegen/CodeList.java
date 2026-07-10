package org.compiler.codegen;

public class CodeList {
    public Instruction head;
    public Instruction tail;

    public static CodeList create() {
        return new CodeList();
    }

    public static CodeList of(Instruction instruction) {
        CodeList list = new CodeList();
        list.append(instruction);
        return list;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public CodeList append(Instruction instruction) {
        if (instruction == null) {
            return this;
        }
        if (head == null) {
            head = instruction;
            tail = instruction;
        } else {
            tail.next = instruction;
            tail = instruction;
        }
        while (tail.next != null) {
            tail = tail.next;
        }
        return this;
    }

    public CodeList emit(Operation op, org.compiler.semantic.SymbolTable.Entry arg1,
                         org.compiler.semantic.SymbolTable.Entry arg2,
                         org.compiler.semantic.SymbolTable.Entry result) {
        return append(new Instruction(op, arg1, arg2, result));
    }

    public static CodeList concat(CodeList first, CodeList second) {
        if (first == null || first.isEmpty()) {
            return second == null ? create() : second;
        }
        if (second == null || second.isEmpty()) {
            return first;
        }
        first.tail.next = second.head;
        first.tail = second.tail;
        return first;
    }

    public CodeList concat(CodeList other) {
        return concat(this, other);
    }

    public void print() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Instruction current = head; current != null; current = current.next) {
            if (current.op == Operation.LABEL) {
                builder.append(current).append('\n');
            } else {
                builder.append("    ").append(current).append('\n');
            }
        }
        return builder.toString();
    }
}
