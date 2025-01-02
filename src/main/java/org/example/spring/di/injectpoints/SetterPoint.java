package org.example.spring.di.injectpoints;

import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.JClass;

import java.util.Collection;

public class SetterPoint extends InjectPoint {

    // Setter方法名
    private String setterName;

    // Setter方法参数名
    private String paramName;

    // 方法或参数的注解上指定的要注入的Bean的名称
    private String specifyName;

    // Setter方法参数类型
    private String paramType;

    // Setter方法上的注解
    private Collection<Annotation> methodAnnotations;

    // 参数上的注解
    private Collection<Annotation> paraAnnotations;

    public SetterPoint(JClass inClass, String fieldName, String runtimeType) {
        super(inClass, fieldName, runtimeType);
    }

    public String getSetterName() {
        return setterName;
    }

    public void setSetterName(String setterName) {
        this.setterName = setterName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getSpecifyName() {
        return specifyName;
    }

    public void setSpecifyName(String specifyName) {
        this.specifyName = specifyName;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public Collection<Annotation> getMethodAnnotations() {
        return methodAnnotations;
    }

    public void setMethodAnnotations(Collection<Annotation> methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }

    public Collection<Annotation> getParaAnnotations() {
        return paraAnnotations;
    }

    public void setParaAnnotations(Collection<Annotation> paraAnnotations) {
        this.paraAnnotations = paraAnnotations;
    }
}
