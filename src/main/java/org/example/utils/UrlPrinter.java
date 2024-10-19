package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.spring.ControllerClass;
import org.example.spring.RouterMethod;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class UrlPrinter {

    private final String urlFilePath = "output/urls.txt";

    private final List<ControllerClass> routerAnalysis;

    private final Logger logger = LogManager.getLogger(UrlPrinter.class);


    public UrlPrinter(List<ControllerClass> routerAnalysis) {
        this.routerAnalysis = routerAnalysis;
    }

    public void printUrls() {
        StringBuilder sb = new StringBuilder();

        for (ControllerClass controllerClass : routerAnalysis) {
            StringBuilder tempSb = new StringBuilder();
            if (!controllerClass.getBaseUrls().isEmpty()) {
                for (String baseUrl : controllerClass.getBaseUrls()) {
                    baseUrl = baseUrl.substring(2, baseUrl.length() - 2);
                    for (RouterMethod routerMethod : controllerClass.getRouterMethods()) {
                        for (String url : routerMethod.getUrls()) {
                            url = url.substring(2, url.length() - 2);
                            tempSb.append(baseUrl).append(url)
                                    .append("  <--->  ")
                                    .append(routerMethod.getJMethod().getDeclaringClass().getName())
                                    .append(".")
                                    .append(routerMethod.getJMethod().getName())
                                    .append("()\n");
                        }
                    }
                }
            } else {
                for (RouterMethod routerMethod : controllerClass.getRouterMethods()) {
                    for (String url : routerMethod.getUrls()) {
                        url = url.substring(2, url.length() - 2);
                        tempSb.append(url)
                                .append("  <--->  ")
                                .append(routerMethod.getJMethod().getDeclaringClass().getName())
                                .append(".")
                                .append(routerMethod.getJMethod().getName())
                                .append("()\n");
                    }
                }
            }
            sb.append(tempSb);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(urlFilePath, false))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
