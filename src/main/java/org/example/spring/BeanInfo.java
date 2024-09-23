package org.example.spring;

import pascal.taie.language.classes.JClass;

/**
 * Custom structure for storing bean information.
 */
public class BeanInfo {

    private final JClass beanClass;
    private final String defaultName;
    private final String fromAnnotationName;


    public BeanInfo(JClass beanClass, String fromAnnotationName) {
        this.beanClass = beanClass;
        this.fromAnnotationName = fromAnnotationName;
        this.defaultName = deriveBeanName(beanClass);
    }

    public JClass getBeanClass() {
        return beanClass;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getFromAnnotationName() {
        return fromAnnotationName;
    }

    private String deriveBeanName(JClass jClass) {
        String className = jClass.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    // Override toString to display bean information
    @Override
    public String toString() {
        return "BeanInfo{" +
                "beanClass=" + beanClass +
                ", defaultName='" + defaultName + '\'' +
                ", fromAnnotationName='" + fromAnnotationName + '\'' +
                '}';
    }
}