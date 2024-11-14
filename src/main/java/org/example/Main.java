package org.example;


import org.apache.commons.cli.*;
import org.example.llm.DefaultLLMConnector;
import org.example.printer.OutputPrinter;
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

        Option enableVulnAnalyze = new Option("v", "vulns-analyze", false, "启用 LLM 进行漏洞检测");
        enableVulnAnalyze.setRequired(false);
        options.addOption(enableVulnAnalyze);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String optionsFilePath;
        boolean enableVulnAnalysis = false;

        try {
            cmd = parser.parse(options, args);
            optionsFilePath = cmd.getOptionValue("options-file");

            // 如果存在 "vulns-analyze" 选项，则启用漏洞分析
            enableVulnAnalysis = cmd.hasOption("vulns-analyze");

        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println("Error: " + e.getMessage());
            formatter.printHelp("java -jar <YourProgram>.jar --options-file=<path> [-v]", options);
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
//        OutputPrinter.outputIcfg();
        if (enableVulnAnalysis){
            CallGraph<Invoke, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
            DefaultLLMConnector llmConnector = new DefaultLLMConnector(callGraph);
            llmConnector.analyzeVuln();
        }
    }


}
