package org.example.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.World;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.analysis.graph.cfg.CFGDumper;
import pascal.taie.analysis.graph.icfg.*;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;
import pascal.taie.util.Indexer;
import pascal.taie.util.SimpleIndexer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class ICFGPrinter {

    private static final Logger logger = LogManager.getLogger(ICFGPrinter.class);

    private final ICFG<JMethod, Stmt> icfg;

    private final Indexer<Stmt> stmtIndexer;

    public ICFGPrinter(ICFG<JMethod, Stmt> icfg) {
        this.icfg = icfg;
        this.stmtIndexer = new SimpleIndexer<>();
    }

    public void dumpICFG() {
        icfg.entryMethods().forEach(entryMethod -> {
//            String fileName = entryMethod.getDeclaringClass().getName() + "." + entryMethod.getName() + ".dot";
            String fileName = String.valueOf(entryMethod.getDeclaringClass()) + '.' +
                    entryMethod.getName() + '(' +
                    entryMethod.getParamTypes()
                            .stream()
                            .map(Type::toString)
                            .collect(Collectors.joining(",")) +
                    ')'+ ".dot";
            File outputDir = new File(World.get().getOptions().getOutputDir(), "callFlowsWithCFG");
            if (!outputDir.exists()) {
                boolean created = outputDir.mkdirs();
                if (!created) {
                    logger.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
                    throw new RuntimeException("Failed to create output directory!");
                }
            }
            File dotFile = new File(outputDir, fileName);

//            logger.info("Dumping ICFG for method {} to {}", entryMethod.getName(), dotFile.getAbsolutePath());

            Stmt entryNode = icfg.getEntryOf(entryMethod);

            try (PrintWriter writer = new PrintWriter(dotFile)) {
                writer.println("digraph G {");
                writer.println("  node [color=\".3 .2 1.0\",shape=box,style=filled];");
                writer.println("  edge [];");

                Set<Stmt> visitedNodes = new HashSet<>();
                List<ICFGEdge<Stmt>> edges = new ArrayList<>();
                collectICFGForMethod(icfg, entryNode, visitedNodes, edges);

                for (Stmt node : visitedNodes) {
                    int nodeId = stmtIndexer.getIndex(node);
                    String label = toLabel(node, icfg);
                    writer.printf("  \"%d\" [label=\"%s\"];\n", nodeId, label);
                }

                for (ICFGEdge<Stmt> edge : edges) {
                    int sourceId = stmtIndexer.getIndex(edge.source());
                    int targetId = stmtIndexer.getIndex(edge.target());
                    String edgeAttributes = getEdgeAttributes(edge);
                    writer.printf("  \"%d\" -> \"%d\" %s;\n", sourceId, targetId, edgeAttributes);
                }
                writer.println("}");
            } catch (FileNotFoundException e) {
                logger.error("Failed to write DOT file: {}", dotFile.getAbsolutePath(), e);
            }
        });
    }

    private void collectICFGForMethod(ICFG<JMethod, Stmt> icfg, Stmt currentNode, Set<Stmt> visitedNodes, List<ICFGEdge<Stmt>> edges) {
        if (!visitedNodes.add(currentNode)) {
            return;
        }

        Set<ICFGEdge<Stmt>> outEdges = icfg.getOutEdgesOf(currentNode);

        for (ICFGEdge<Stmt> edge : outEdges) {
            Stmt targetNode = edge.target();
            edges.add(edge);
            if (edge instanceof CallEdge) {
                JMethod targetMethod = icfg.getContainingMethodOf(targetNode);
                Stmt entryOfTargetMethod = icfg.getEntryOf(targetMethod);
                if (!visitedNodes.contains(entryOfTargetMethod)) {
                    collectICFGForMethod(icfg, entryOfTargetMethod, visitedNodes, edges);
                }
            }

            collectICFGForMethod(icfg, targetNode, visitedNodes, edges);
        }
    }


    private  String getEdgeAttributes(ICFGEdge<Stmt> edge) {
        if (edge instanceof CallEdge) {
            return "[color=blue,style=dashed]";
        } else if (edge instanceof ReturnEdge) {
            return "[color=red,style=dashed]";
        } else if (edge instanceof CallToReturnEdge) {
            return "[style=dashed]";
        } else {
            return "[]";
        }
    }

    private String toLabel(Stmt stmt, ICFG<JMethod, Stmt> icfg) {
        JMethod method = icfg.getContainingMethodOf(stmt);
        CFG<Stmt> cfg = getCFGOf(method);
        return CFGDumper.toLabel(stmt, cfg);
    }

    private CFG<Stmt> getCFGOf(JMethod method) {
        return method.getIR().getResult(CFGBuilder.ID);
    }



}