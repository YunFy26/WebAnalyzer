package org.example.spring.di.injectpoints;


import pascal.taie.language.classes.JClass;

public class InjectPoint{

    // 注入点所在的类
    protected JClass inClass;

    // 被注入的field名称
    protected String fieldName;

    // 运行时类型 （也就是实际注入的Bean的类型）
    protected String runtimeType;

    public InjectPoint(JClass inClass, String fieldName, String runtimeType) {
        this.inClass = inClass;
        this.fieldName = fieldName;
        this.runtimeType = runtimeType;
    }

    public String getInClassName(){
        return inClass.getName();
    }

    public String getRuntimeType(){
        return runtimeType;
    }

    public String getFieldName(){
        return fieldName;
    }

    public void setFieldName(String name){
        fieldName = name;
    }

    public void setInClass(JClass inClass) {
        this.inClass = inClass;
    }

    public void setRuntimeType(String runtimeType) {
        this.runtimeType = runtimeType;
    }
}
