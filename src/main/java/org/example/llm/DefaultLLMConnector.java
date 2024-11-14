package org.example.llm;

import com.knuddels.jtokkit.api.EncodingType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.llm.model.GPTClient;
import org.example.llm.model.SparkClient;
import org.example.printer.VulnsPrinter;
import org.example.rag.es.ElasticsearchUtils;
import org.example.utils.DotProcessor;
import org.example.utils.DotReader;
import org.example.utils.MethodInvocationAnalyzer;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


// TODO: 1. 知识库信息构建
public class DefaultLLMConnector {

    // yuntsy-apiKey
//    private final String gptApiKey = "sk-...";

    // d3do-apiKey
    private final String gptApiKey = "sk-...";

    private final String gptApiUrl = "...";

    // gpt model
    private final GPTClient gptModel = new GPTClient(gptApiKey, gptApiUrl);

    private final String sparkApiKey = "...";

    private final String sparkApiUrl = "...";

    // spark model
    private final SparkClient sparkModel = new SparkClient(sparkApiKey, sparkApiUrl);

    private final CallGraph<Invoke, JMethod> callGraph;

    private final DotReader dotReader;

    private final Logger logger = LogManager.getLogger(DefaultLLMConnector.class);

    // determine the max tokens of input
    private final int MAX_SEND_TOKENS = 8000;

    // gpt3.5 / gpt-4 encodingType
    private static final EncodingType ENCODING_TYPE = EncodingType.CL100K_BASE;

    // tokens of BACKGROUND_SYSTEM_PROMPT
    private final int BACKGROUND_SYSTEM_PROMPT_TOKENS = Tokenizer.countTokens(Prompts.BACKGROUND_SYSTEM_PROMPT.getContent(), ENCODING_TYPE);

    // tokens of USER_PROMPT
    private final int USER_PROMPT_TOKENS = Tokenizer.countTokens(Prompts.USER_PROMPT.getContent(), ENCODING_TYPE);

    // max tokens of DYNAMIC_PROMPT (DYNAMIC_PROMPT within USER_PROMPT)
    private final int MAX_DYNAMIC_TOKENS = MAX_SEND_TOKENS - BACKGROUND_SYSTEM_PROMPT_TOKENS - USER_PROMPT_TOKENS;

    // Map<entryMethod, List<direct and indirect calls>>
    private final HashMap<String, List<String>> invocationMap = new HashMap<>();


    public DefaultLLMConnector(CallGraph<Invoke, JMethod> callGraph) {
        this.callGraph = callGraph;
        this.dotReader = new DotReader("output/callFlows");
    }

    /**
     * For each call flow, get the direct and indirect calls of the entry method.
     */
    private void setInvocationMap(){
        callGraph.entryMethods().forEach(jMethod -> {
            String key = String.valueOf(jMethod.getDeclaringClass()) + '.' +
                    jMethod.getName() + '(' +
                    jMethod.getParamTypes()
                            .stream()
                            .map(Type::toString)
                            .collect(Collectors.joining(",")) +
                    ')';
            Set<JMethod> allCallees = MethodInvocationAnalyzer.getAllCallees(callGraph, jMethod);
            List<String> calleesList = new ArrayList<>();
            allCallees.forEach(callee -> {
                calleesList.add(callee.getSignature());
            });
            invocationMap.put(key, calleesList);
        });
    }

    /**
     * For each call flow, get the description of each method in the call flow.
     * Then send the request to llm, the response contains whether the call flow include vulnerability.
     */
    public void analyze() throws Exception {
        setInvocationMap();
        List<File> dotFiles = dotReader.getDotFiles();
        StringBuilder methodDescription = new StringBuilder();
        String result = null;
//        int count = 0;
        for (File dotFile : dotFiles) {
            String callFlow = DotProcessor.processCallGraph(dotReader.readDotFile(dotFile));
            int callFlowTokens = Tokenizer.countTokens(callFlow, ENCODING_TYPE);
            int maxMethodDescriptionTokens = MAX_DYNAMIC_TOKENS - callFlowTokens;
//            logger.info(dotFile.getName());
            List<String> callesList = invocationMap.get(dotFile.getName().replace(".dot", ""));
            int size = callesList.size();
            // get the description of every method in the call flow
            for (int i = 0; i < size; i++) {
                String methodSignature = callesList.get(i);
                String methodBody = ElasticsearchUtils.fetchMethodData(methodSignature);
//                logger.info(count++);
//                String currentDescription = gptModel.sendRequest(Prompts.METHOD_DESCRIPTION_SYSTEM_PROMPT.getContent(), String.format(Prompts.METHOD_DESCRIPTION_USER_PROMPT.getContent(), methodSignature, methodBody));
//                methodDescription.append(currentDescription);
            }

            String systemPrompt = Prompts.BACKGROUND_SYSTEM_PROMPT.getContent();
            String userPrompt = String.format(Prompts.USER_PROMPT.getContent(), callFlow, methodDescription);
            int inputTokens = Tokenizer.countTokens(systemPrompt + userPrompt, ENCODING_TYPE);
//            slidingWindow(systemPrompt, methodDescription);
//            if (inputTokens > MAX_SEND_TOKENS){
//
//            }
            logger.info("-------------------------------------");
            logger.info(systemPrompt + userPrompt);
//            result = gptModel.sendRequest(Prompts.BACKGROUND_SYSTEM_PROMPT.getContent(), String.format(Prompts.USER_PROMPT.getContent(), callFlow, methodDescription));
//            logger.info(result);
            methodDescription.setLength(0);
        }
    }

    public void analyzeVuln() throws Exception {
        List<File> dotFiles = dotReader.getDotFiles();
        int count = 0;
        for (File dotFile : dotFiles) {
            String callFlow = dotReader.readDotFile(dotFile);
//            String callFlow = DotProcessor.processCallGraph(dotReader.readDotFile(dotFile));
            int callFlowTokens = Tokenizer.countTokens(callFlow, ENCODING_TYPE);
            String systemPrompt = Prompts.BACKGROUND_SYSTEM_PROMPT.getContent();
            String entryMethodName = dotFile.getName().replace(".dot", "");
            if (callFlowTokens <= MAX_DYNAMIC_TOKENS){
                String userPrompt = String.format(Prompts.USER_PROMPT.getContent(), callFlow);
                logger.info("Start analyzing the {} call flow, the entry point is: {}" , ++count, entryMethodName);
                String response = sparkModel.sendRequest(systemPrompt, userPrompt);
                response = response.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "");
                logger.info("Write the analysis results of the call flow into: output/vulns.xlsx");
                try{
                    VulnsPrinter.generateExcel(response);
                }catch (Exception e){
                    logger.warn("Failed to process response for entry point {}: {}", entryMethodName, e.getMessage());
                }
//                logger.info(systemPrompt + userPrompt);
            }else {

            }
        }
    }

}
