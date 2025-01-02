package org.example.spring.di.injectpoints;

import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.JClass;

import java.util.Collection;

/**
 * Field注入点
 */
public class FieldPoint extends InjectPoint {

    // 注解上指定的要注入的Bean名称
    private String specifyName;

    // Field类型
    private String fieldType;

    // Field上的注解
    private Collection<Annotation> annotations;


    public FieldPoint(JClass inClass, String runtimeType, String fieldName) {
        super(inClass, runtimeType, fieldName);
    }


    public void setSpecifyName(String specifyName) {
        this.specifyName = specifyName;
    }

    public void setAnnotations(Collection<Annotation> annotations) {
        this.annotations = annotations;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
