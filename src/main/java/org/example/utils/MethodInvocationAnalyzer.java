package org.example.utils;

import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class MethodInvocationAnalyzer {

    /**
     * Get direct and indirect calls of a method.
     */
    public static Set<JMethod> getAllCallees(CallGraph<Invoke, JMethod> callGraph, JMethod jMethod){
        Set<JMethod> jMethods = new HashSet<>();
        Queue<JMethod> queue = new LinkedList<>();
        queue.add(jMethod);
        while (!queue.isEmpty()){
            JMethod currMethod = queue.poll();
            Set<JMethod> directCallees = callGraph.getCalleesOfM(currMethod);
            directCallees.forEach(jMethod1 -> {
                if (jMethods.add(jMethod1)){
                    queue.add(jMethod1);
                }
            });
        }
        return jMethods;
    }

}
