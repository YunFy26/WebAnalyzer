package org.example.spring;

import pascal.taie.language.annotation.Element;
import pascal.taie.language.classes.JClass;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class including the JClass, routerMethods and baseUrls
 */
public class ControllerClass {

    private final String urlFilePath = "output/urls.txt";

    private final JClass jClass;

    private final List<String> baseUrls = new ArrayList<>();

    private final List<RouterMethod> routerMethods = new ArrayList<>();


    public ControllerClass(JClass jClass) {
        this.jClass = jClass;
    }

    public void addBaseUrl(String baseUrl) {
        this.baseUrls.add(baseUrl);
    }

    public void addRouterMethod(RouterMethod routerMethod) {
        this.routerMethods.add(routerMethod);
    }

    public List<RouterMethod> getRouterMethods() {
        return routerMethods;
    }

    public JClass getJClass() {
        return jClass;
    }


    public List<String> getBaseUrls() {
        return baseUrls;
    }

    public void setBaseUrls() {
        // TODO: other Mapping ?
        if (jClass.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping")) {
            // TODO：if value is a List, such as @RequestMapping({"/params", "/"}), how to process ?
            Element value = Objects.requireNonNull(jClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping")).getElement("value");
            Element path = Objects.requireNonNull(jClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping")).getElement("path");
            if (value != null) {
                baseUrls.add(value.toString());
            }
            if (path != null) {
                baseUrls.add(path.toString());
            }
        }
    }

    public void printUrls() {
        // 写入文件
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(urlFilePath, true))) {
//            if (!baseUrls.isEmpty()) {
//                for (String baseUrl : baseUrls) {
//                    baseUrl = baseUrl.substring(2, baseUrl.length() - 2);
//
//                    for (RouterMethod routerMethod : routerMethods) {
//                        for (String url : routerMethod.getUrls()) {
//                            url = url.substring(2, url.length() - 2);
//                            writer.write(baseUrl + url);
//                            writer.newLine();
//                        }
//                    }
//                }
//            } else {
//                for (RouterMethod routerMethod : routerMethods) {
//                    for (String url : routerMethod.getUrls()) {
//                        url = url.substring(2, url.length() - 2);
//                        writer.write(url);
//                        writer.newLine();
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!baseUrls.isEmpty()){
            for (String baseUrl : baseUrls) {
                baseUrl = baseUrl.substring(2, baseUrl.length() - 2);
                for (RouterMethod routerMethod : routerMethods) {
                    for (String url : routerMethod.getUrls()) {
                        url = url.substring(2, url.length() - 2);
                        System.out.println(baseUrl+ url);
                    }
                }
            }
        }else {
            for (RouterMethod routerMethod : routerMethods) {
                for (String url : routerMethod.getUrls()) {
                    url = url.substring(2, url.length() - 2);
                    System.out.println(url);
                }
            }
        }
    }

}
