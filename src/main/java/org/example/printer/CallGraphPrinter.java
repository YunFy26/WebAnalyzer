package org.example.printer;

import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;
import pascal.taie.util.Indexer;
import pascal.taie.util.SimpleIndexer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CallGraphPrinter {

    private final CallGraph<Invoke, JMethod> callGraph;

    private final Indexer<JMethod> methodIndexer;


    public CallGraphPrinter(CallGraph<Invoke, JMethod> callGraph) {
        this.callGraph = callGraph;
        this.methodIndexer = new SimpleIndexer<>();
    }

    public String dotContent(JMethod entryMethod){
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
        return dotContent.toString();
    }

    public void generateDotFile(JMethod entryMethod) throws IOException {
        String dotContent = dotContent(entryMethod);

        String directoryPath = "output/callFlows";
        Files.createDirectories(Paths.get(directoryPath));
        String fileName = String.valueOf(entryMethod.getDeclaringClass()) + '.' +
                entryMethod.getName() + '(' +
                entryMethod.getParamTypes()
                        .stream()
                        .map(Type::toString)
                        .collect(Collectors.joining(",")) +
                ')'+ ".dot";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(directoryPath + '/' + fileName))) {
            writer.write(dotContent);
        }
    }

    private void explore(JMethod currentMethod, Set<String> visited, Set<String> addedEdges, StringBuilder nodesContent, StringBuilder edgesContent) {
        String currentMethodSignature = currentMethod.getSignature();
        if (visited.contains(currentMethodSignature)) {
            return;
        }
        visited.add(currentMethodSignature);
        Set<JMethod> callees = callGraph.getCalleesOfM(currentMethod);

        int methodIndex = methodIndexer.getIndex(currentMethod);

        String currentMethodLabel = "\"" + methodIndex + "\"";
        nodesContent.append(currentMethodLabel)
                .append(" [label=\"")
                .append(currentMethod.getSignature())
                .append("\"];\n");

        for (JMethod callee : callees) {
            int calleeIndex = methodIndexer.getIndex(callee);
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


    private String getCallSiteDetails(JMethod caller, JMethod callee) {
        StringBuilder details = new StringBuilder();
        for (Invoke callSite : callGraph.getCallSitesIn(caller)) {
            if (callGraph.getCalleesOf(callSite).contains(callee)) {
                String callSiteStr = callSite.toString();
                int index = callSiteStr.indexOf('>');
                if (index != -1 && index + 1 < callSiteStr.length()) {
                    details.append(callSiteStr.substring(index + 1).trim()).append("; ");
                }
            }
        }
        return !details.isEmpty() ? details.toString() : "invoke " + callee.getName() + "();";
    }
}