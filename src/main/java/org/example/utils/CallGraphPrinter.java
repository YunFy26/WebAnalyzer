package org.example.utils;

import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.language.classes.JMethod;
import pascal.taie.util.Indexer;
import pascal.taie.util.SimpleIndexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class CallGraphPrinter {
    private final CallGraph<CSCallSite, CSMethod> callGraph;
    private final Indexer<JMethod> methodIndexer;

    public CallGraphPrinter(CallGraph<CSCallSite, CSMethod> callGraph) {
        this.callGraph = callGraph;
        this.methodIndexer = new SimpleIndexer<>();
    }

    public void generateDotFile(CSMethod entryMethod) throws IOException {
        Set<String> visited = new HashSet<>();
        Set<String> addedEdges = new HashSet<>();
        StringBuilder nodesContent = new StringBuilder();
        StringBuilder edgesContent = new StringBuilder();
        StringBuilder dotContent = new StringBuilder("digraph G {\n");
        dotContent.append("node [color=\".3 .2 1.0\",shape=box,style=filled];\n");
        dotContent.append("edge [];\n");
        explore(entryMethod, visited, addedEdges, nodesContent, edgesContent);

        dotContent.append(nodesContent);
        dotContent.append(edgesContent);
        dotContent.append("}\n");

        String directoryPath = "output/callFlows";
        Files.createDirectories(Paths.get(directoryPath));
        String filename = directoryPath + "/" + entryMethod.getMethod().getDeclaringClass() + "." + entryMethod.getMethod().getName() + ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(dotContent.toString());
        }
    }

    private void explore(CSMethod currentMethod, Set<String> visited, Set<String> addedEdges, StringBuilder nodesContent, StringBuilder edgesContent) {
        String currentMethodSignature = currentMethod.getMethod().getSignature();
        if (visited.contains(currentMethodSignature)) {
            return;
        }
        visited.add(currentMethodSignature);
        Set<CSMethod> callees = callGraph.getCalleesOfM(currentMethod);

        int methodIndex = methodIndexer.getIndex(currentMethod.getMethod());

        String currentMethodLabel = "\"" + methodIndex + "\"";
        nodesContent.append(currentMethodLabel)
                .append(" [label=\"")
                .append(currentMethod.getMethod().getSignature())
                .append("\"];\n");

        for (CSMethod callee : callees) {
            int calleeIndex = methodIndexer.getIndex(callee.getMethod());
            String calleeMethodLabel = "\"" + calleeIndex + "\"";

            String edge = currentMethodLabel + " -> " + calleeMethodLabel;
            if (!addedEdges.contains(edge)) {
                edgesContent.append(edge)
                        .append(" [label=\" ")
                        .append(getCallSiteDetails(currentMethod, callee))
                        .append("\"];\n");
                addedEdges.add(edge);
            }
            explore(callee, visited, addedEdges, nodesContent, edgesContent);
        }
    }


    private String getCallSiteDetails(CSMethod caller, CSMethod callee) {
        StringBuilder details = new StringBuilder();
        for (CSCallSite csCallSite : callGraph.getCallSitesIn(caller)) {
            if (callGraph.getCalleesOf(csCallSite).contains(callee)) {
                String callSiteStr = csCallSite.getCallSite().toString();
                int index = callSiteStr.indexOf('>');
                if (index != -1 && index + 1 < callSiteStr.length()) {
                    details.append(callSiteStr.substring(index + 1).trim()).append("; ");
                }
            }
        }
        return !details.isEmpty() ? details.toString() : "invoke " + callee.getMethod().getName() + "();";
    }
}