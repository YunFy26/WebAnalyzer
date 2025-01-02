package org.example.spring.di.injectpoints;

import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.JClass;

import java.util.Collection;

/**
 * 构造方法注入点
 */
public class ConstructorPoint extends InjectPoint {

    // 参数名
    private String paramName;

    // 注解上指定的要注入的Bean名称
    private String specifyName;

    // 参数类型
    private String paramType;

    // 方法上的注解
    private Collection<Annotation> methodAnnotations;

    // 参数上的注解
    private Collection<Annotation> paramAnnotations;


    public ConstructorPoint(JClass inClass, String fieldName, String runtimeType) {
        super(inClass, fieldName, runtimeType);
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Collection<Annotation> getParamAnnotations() {
        return paramAnnotations;
    }

    public void setParamAnnotations(Collection<Annotation> paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
    }

    public Collection<Annotation> getMethodAnnotations() {
        return methodAnnotations;
    }

    public void setMethodAnnotations(Collection<Annotation> methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getSpecifyName() {
        return specifyName;
    }

    public void setSpecifyName(String specifyName) {
        this.specifyName = specifyName;
    }
}
