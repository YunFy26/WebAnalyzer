package org.example.spring;

import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class CallGraphExplorer {
    private final CallGraph<CSCallSite, CSMethod> callGraph;

    public CallGraphExplorer(CallGraph<CSCallSite, CSMethod> callGraph) {
        this.callGraph = callGraph;
    }

    public void generateDotFile(CSMethod entryMethod) throws IOException {
        Set<CSMethod> visited = new HashSet<>();
        Set<String> addedEdges = new HashSet<>(); // 用来存储已添加的边
        StringBuilder dotContent = new StringBuilder("digraph G {\n");
        explore(entryMethod, visited, addedEdges, dotContent);
        dotContent.append("}\n");

        String directoryPath = "output/callFlows";
        Files.createDirectories(Paths.get(directoryPath));
        String filename = directoryPath + "/" + entryMethod.getMethod().getDeclaringClass() + "." + entryMethod.getMethod().getName() + ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(dotContent.toString());
        }
    }

    private void explore(CSMethod currentMethod, Set<CSMethod> visited, Set<String> addedEdges, StringBuilder dotContent) {
        if (visited.contains(currentMethod)) {
            return;
        }
        visited.add(currentMethod);
        Set<CSMethod> callees = callGraph.getCalleesOfM(currentMethod);
        for (CSMethod callee : callees) {
            String edge = "\"" + currentMethod.getMethod().getSignature() + "\" -> \"" + callee.getMethod().getSignature() + "\"";
            if (!addedEdges.contains(edge)) { // 检查是否已添加过此边
                dotContent.append(edge).append(";\n");
                addedEdges.add(edge); // 添加到已添加的边集合中
            }
            explore(callee, visited, addedEdges, dotContent);
        }
    }
}