package org.example.spring.injectpoints;

public enum InjectionAnnotationRules {
    // 根据类型注入
    Autowired("org.springframework.beans.factory.annotation.Autowired"),
    Inject("javax.inject.Inject"),

    // 先根据名称注入，再根据类型注入
    Resource("jakarta.annotation.Resource"),

    // 配合Autowired，Inject使用，一般不单独出现
    Qualifier("org.springframework.beans.factory.annotation.Qualifier"),

    // 一般用于注入属性值
    Value("org.springframework.beans.factory.annotation.Value");

    private final String annotationType;

    InjectionAnnotationRules(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getType() {
        return annotationType;
    }
}
