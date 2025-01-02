package org.example.spring.router;

import pascal.taie.language.annotation.Element;
import pascal.taie.language.classes.JClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class including the JClass, routerMethods and baseUrls
 */
public class ControllerClass {


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
}
