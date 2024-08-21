package org.example.spring;

import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.annotation.Element;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entity class including the JMethod and urls
 */
public class RouterMethod {

    private JMethod jMethod;

    private final List<String> urls = new ArrayList<>();


    public RouterMethod(JMethod jMethod) {
        this.jMethod = jMethod;
    }

    public void addUrl() {

        Collection<Annotation> annotations = jMethod.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.getType().equals("org.springframework.web.bind.annotation.RequestMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            } else if (annotation.getType().equals("org.springframework.web.bind.annotation.PostMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            } else if (annotation.getType().equals("org.springframework.web.bind.annotation.GetMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            } else if (annotation.getType().equals("org.springframework.web.bind.annotation.PutMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            } else if (annotation.getType().equals("org.springframework.web.bind.annotation.DeleteMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            } else if (annotation.getType().equals("org.springframework.web.bind.annotation.PatchMapping")) {
                Element value = annotation.getElement("value");
                Element path = annotation.getElement("path");
                if (value != null) {
                    urls.add(value.toString());
                }
                if (path != null) {
                    urls.add(path.toString());
                }
            }
        }

    }

    public JMethod getJMethod() {
        return jMethod;
    }


    public List<String> getUrls() {
        return urls;
    }

}
