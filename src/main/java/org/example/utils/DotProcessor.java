package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotProcessor {

    private static final Logger logger = LogManager.getLogger(DotProcessor.class);

    private static final Pattern NODE_PATTERN = Pattern.compile("\"(\\d+)\" \\[label=\"(?:\\d+: )?(.*?)(?:\\[\\d+@L\\d+\\])?\"\\];?");
    private static final Pattern EDGE_PATTERN = Pattern.compile("\"(\\d+)\" -> \"(\\d+)\"(?: \\[.*?\\])?;");
    private static final String HEADER_PATTERN = "(node \\[color=.*?];|edge \\[.*?];)";

    private static final HashSet<String> encounteredClassNames = new HashSet<>();

    /**
     * process icfg from dot
     * @param content dot content
     * @return content processed
     */
    public static String processIcfg(String content){
        StringBuilder processedContent = new StringBuilder();
        processedContent.append("digraph G {\n");

        String[] lines = content.split("\\r?\\n");  // 按行拆分字符串
        for (String line : lines) {
            // 去除 node 和 edge 属性定义行
            if (line.matches(HEADER_PATTERN)) {
                continue;
            }

            // 处理节点信息
            Matcher nodeMatcher = NODE_PATTERN.matcher(line);
            if (nodeMatcher.find()) {
                String nodeIndex = nodeMatcher.group(1);
                String nodeContent = processNodeContent(nodeMatcher.group(2));
                processedContent.append(nodeIndex).append("[").append(nodeContent).append("];\n");
                continue;
            }

            // 处理边信息
            Matcher edgeMatcher = EDGE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                String fromNode = edgeMatcher.group(1);
                String toNode = edgeMatcher.group(2);
                processedContent.append(fromNode).append("->").append(toNode).append(";\n");
            }
        }

        processedContent.append("}");
        return processedContent.toString();
    }

    private static String processNodeContent(String content) {

        content = content.replaceAll("\\[\\d+@L\\d+\\]", "");

        Matcher matcher = Pattern.compile("([a-zA-Z_0-9.]+\\.[A-Z][a-zA-Z_0-9]*)").matcher(content);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String fullClassName = matcher.group(1);
            String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

            if (encounteredClassNames.contains(fullClassName)) {
                matcher.appendReplacement(result, simpleClassName);
            } else {
                encounteredClassNames.add(fullClassName);
                matcher.appendReplacement(result, fullClassName);
            }
        }
        matcher.appendTail(result);

        return result.toString()
                .replaceAll("\\b(invokevirtual|invokespecial|invokedynamic|invokeinterface)\\b", "")
                .trim();
    }


    public static String processCallGraph(String dotContent) {
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