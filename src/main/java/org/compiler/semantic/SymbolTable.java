package org.compiler.semantic;

import java.util.*;

public class SymbolTable {

    public record Entry(String name, String type, String kind){
        @Override
        public String toString(){
            return String.format("{kind='%s', type'%s'}", kind, type);

        }

    }
    private final Deque<Map<String, Entry>> scopes;
    private final List<Map<String, Entry>> allScopesHistory;

    public SymbolTable(){
        this.allScopesHistory = new ArrayList<>();
        this.scopes = new ArrayDeque<>();
        enterScope();
    }
    public void enterScope(){
        Map<String, Entry> newScope = new HashMap<>();
        scopes.push(newScope);
        allScopesHistory.add(newScope);
    }
    public void exitScope(){
        if(!scopes.isEmpty()){
            scopes.pop();
        }
    }

    public boolean put(String name, String type, String kind){
        Map<String, Entry> scope = scopes.peek();
        if(scope.containsKey(name)){
            return false;
        }
        scope.put(name, new Entry(name, type, kind));
        return true;
    }

    public Entry lookup(String name){
        for (Map<String, Entry> scope: scopes){
            return scope.get(name);
        }
        return null;
    }
    public void printTable(){
        System.out.println("\n ==== SYUMBOL TABLE ====");
        int level = scopes.size() - 1;
        for (Map<String, Entry> scope: allScopesHistory){
            System.out.println("Scope Level " + level + ": " + scope);
            level--;
        }
    }

}
