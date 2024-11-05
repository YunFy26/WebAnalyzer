package org.example.rag.es;

import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;

import java.util.List;
import java.util.stream.Collectors;

public class MethodData {

    private final String methodSignature;
    private final List<Var> vars;
    private final List<Stmt> stmts;

    public MethodData(String methodSignature, List<Var> vars, List<Stmt> stmts) {
        this.methodSignature = methodSignature;
        this.vars = vars;
        this.stmts = stmts;
    }

    public String getMethodSignature() {
        return methodSignature;
    }


    public String getVars() {
        return vars.stream()
                .map(var -> var.getType() + " " + var.getName())
                .collect(Collectors.joining(", "));
    }

    public String getStmts() {
        return stmts.stream()
                .map(Stmt::toString)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return "MethodData{" +
                "methodSignature='" + methodSignature + '\'' +
                ", vars: [" + getVars() +
                "], stmts: [" + getStmts() +
                "]}";
    }
}