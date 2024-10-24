package org.example;


import org.apache.commons.cli.*;
import org.example.llm.DefaultLLMConnector;
import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.graph.callgraph.CallGraphBuilder;
import pascal.taie.analysis.graph.callgraph.DefaultCallGraph;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JMethod;

import java.text.ParseException;

public class MyMain {

    public static void main(String[] args) {

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
            System.out.println("错误: " + e.getMessage());
            formatter.printHelp("--options-file=<path>", options);  // 打印帮助信息
            System.exit(1);  // 退出程序
            return;
        }

        pascal.taie.Main.main(
                "--options-file",
                optionsFilePath
        );
//        if (enableLLM){
//            CallGraph<Invoke, JMethod> callGraph = World.get().getResult(CallGraphBuilder.ID);
//            DefaultLLMConnector llmConnector = new DefaultLLMConnector(callGraph);
//        }


    }


}
