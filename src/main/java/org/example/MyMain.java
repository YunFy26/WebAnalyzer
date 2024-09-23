package org.example;


import org.apache.commons.cli.*;

import java.text.ParseException;

public class MyMain {

    public static void main(String[] args) {

        Options options = new Options();

        Option optionFile = new Option("o", "options-file", true, "配置文件路径");
        optionFile.setRequired(true);
        options.addOption(optionFile);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String optionsFilePath;

        try {
            // 解析命令行参数
            cmd = parser.parse(options, args);

            // 获取 --options-file 参数的值
            optionsFilePath = cmd.getOptionValue("options-file");

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
    }


}
