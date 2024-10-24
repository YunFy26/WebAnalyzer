package org.example.llm;

import com.knuddels.jtokkit.api.EncodingType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.DotProcessor;
import org.example.utils.DotReader;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.graph.callgraph.CallGraphBuilder;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultLLMConnector {

    private final CallGraph<Invoke, JMethod> callGraph;

    private final DotReader dotReader;

    private final Logger logger = LogManager.getLogger(DefaultLLMConnector.class);

    private final int TOKEN_LIMIT = 7000;

    // gpt-3 encodingType
    private final EncodingType ENCODING_TYPE = EncodingType.P50K_BASE;
    // gpt-4 encodingType
//    private static final EncodingType ENCODING_TYPE = EncodingType.CL100K_BASE;

    private final StringBuilder callFlowAndIR;

    private final StringBuilder callFlowBuilder;


    public DefaultLLMConnector(CallGraph<Invoke, JMethod> callGraph) {
        this.callGraph = callGraph;
        this.dotReader = new DotReader("output/callFlows");
        this.callFlowAndIR = new StringBuilder();
        this.callFlowBuilder = new StringBuilder();
    }

    public Object analyze() {

        return null;
    }

    private void sendToLLM(){

    }

    public String collectCallFlow() throws IOException {

        List<File> dotFiles = dotReader.getDotFiles();
        for (File dotFile : dotFiles){
            String s = dotReader.readDotFile(dotFile);
            String s1 = DotProcessor.processDot(s);
            System.out.println(s1);
        }

        return callFlowBuilder.toString();
    }


}
