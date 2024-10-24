package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotProcessor {

    private static final Logger logger = LogManager.getLogger(DotProcessor.class);


    public static String processDot(String dotContent) {
        StringBuilder result = new StringBuilder();
        String[] lines = dotContent.split("\n");

        // 处理节点
        for (String line : lines) {
            line = line.trim();
            if (line.matches("\"\\d+\" \\[label=\"<.*>\"];")) {
                String node = processNode(line);
                result.append(node).append("\n");
            } else if (line.matches("\"\\d+\" -> \"\\d+\" \\[label=.*];")) {
                String edge = processEdge(line);
                result.append(edge).append("\n");
            }
        }

        return result.toString().trim();
    }

    private static String processNode(String nodeLine) {
        // 提取节点编号和标签信息
        String nodeId = nodeLine.split("\"")[1];  // 提取节点 ID
        String label = nodeLine.split("label=\"<")[1].split(">\"]")[0]; // 提取标签内容
        label = label.replaceAll("\\s+", "");  // 去除空格
        return nodeId + label;
    }

    private static String processEdge(String edgeLine) {
        // 提取源节点、目标节点信息
        String[] parts = edgeLine.split(" ");
        String fromNode = parts[0].replace("\"", "");  // 去除引号
        String toNode = parts[2].replace("\"", "");
        return fromNode + "->" + toNode;
    }

}