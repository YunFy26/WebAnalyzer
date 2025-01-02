package org.example.spring.bean;

public enum BeanAnnotationRules {

    // 通用注解，表示是Bean
    Component("org.springframework.stereotype.Component"),

    // 服务层注解
    Service("org.springframework.stereotype.Service"),

    // DAO层注解
    Repository("org.springframework.stereotype.Repository"),

    // 控制层注解
    Controller("org.springframework.stereotype.Controller"),
    RestController("org.springframework.web.bind.annotation.RestController"),

    Configuration("org.springframework.context.annotation.Configuration"),
    Bean("org.springframework.context.annotation.Bean"),

    // TODO:Mybatis中的注解
    Mapper("org.apache.ibatis.annotations.Mapper");

    private final String annotationType;

    BeanAnnotationRules(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getType() {
        return annotationType;
    }

}
