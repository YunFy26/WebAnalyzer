package org.example;


import org.apache.commons.cli.*;
import org.example.llm.DefaultLLMConnector;
import org.example.printer.OutputPrinter;
import org.example.rag.RepositoryBuilder;
import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.graph.callgraph.CallGraphBuilder;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;

public class Main {

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        Option optionFile = new Option("o", "options-file", true, "配置文件路径");
        optionFile.setRequired(true);
        options.addOption(optionFile);

        Option enableLLMOption = new Option("e", "enable-llm", false, "启用 LLM 进行漏洞检测");
        enableLLMOption.setRequired(false);
        options.addOption(enableLLMOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String optionsFilePath;
        boolean enableLLM = false;

        try {
            cmd = parser.parse(options, args);
            optionsFilePath = cmd.getOptionValue("options-file");
            if (cmd.hasOption("enable-llm") || cmd.hasOption("e")) {
                String enableLLMValue = cmd.getOptionValue("enable-llm");
                enableLLM = Boolean.parseBoolean(enableLLMValue);
            }

        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println("Error: " + e.getMessage());
            formatter.printHelp("--options-file=<path>", options);
            System.exit(1);
            return;
        }

        pascal.taie.Main.main(
                "--options-file",
                optionsFilePath
        );
        // Output
        OutputPrinter.outputUrls();
        OutputPrinter.outputCallFlows();
        OutputPrinter.outputIcfg();
//        if (enableLLM){
//            CallGraph<Invoke, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
//            DefaultLLMConnector llmConnector = new DefaultLLMConnector(callGraph);
//        }
        CallGraph<Invoke, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
//        RepositoryBuilder repositoryBuilder = new RepositoryBuilder(callGraph);
//        repositoryBuilder.build();
        DefaultLLMConnector llmConnector = new DefaultLLMConnector(callGraph);
        llmConnector.analyze();





    }


}
