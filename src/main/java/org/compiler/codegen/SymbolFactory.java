package org.compiler.codegen;

import org.compiler.semantic.SymbolTable;
import org.compiler.semantic.SymbolTable.Entry;

import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolFactory {

    private final SymbolTable table;
    private final Map<String, Entry> known = new LinkedHashMap<>();
    private int tempCounter = 0;
    private int labelCounter = 0;

    public SymbolFactory(SymbolTable table) {
        this.table = table;
    }

    public Entry newTemp() {
        return newTemp("int");
    }

    public Entry newTemp(String type) {
        return register("t" + (tempCounter++), type, "temp");
    }

    public Entry newLabel() {
        return register("L" + (labelCounter++), "label", "label");
    }

    public Entry symbol(String name, String type, String kind) {
        Entry existing = known.get(name);
        if (existing != null) {
            return existing;
        }
        return register(name, type, kind);
    }

    public Entry variable(String name) {
        return symbol(name, "unknown", "var");
    }

    public Entry constant(String literal) {
        return symbol(literal, "int", "const");
    }

    private Entry register(String name, String type, String kind) {
        table.put(name, type, kind);
        Entry entry = new Entry(name, type, kind);
        known.put(name, entry);
        return entry;
    }
}
