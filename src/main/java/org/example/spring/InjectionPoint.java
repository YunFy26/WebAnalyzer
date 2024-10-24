package org.example.spring;

import pascal.taie.language.classes.JField;

import java.util.Objects;

/**
 * entity class
 * for FieldInjection   make a InjectionPoint instance, represent an injection point
 * provide some api
 * TODO: for setter injection and constructor injection, provide api
 */
public class InjectionPoint {

    private final JField field;

    private final String defaultName;

    public InjectionPoint(JField field) {
        this.field = field;
        this.defaultName = field.getName();
    }

    public String getSpecifyName() {
        // @Qualifier 和 @Resource 一般不同时出现，@Qualifier是辅助 @Autowired 和 @Inject 的
        // 获取 @Qualifier 指定的名称
        if (field.hasAnnotation(InjectionAnnotationRules.Qualifier.getType())) {
            return Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(InjectionAnnotationRules.Qualifier.getType()))
                    .getElement("value")).toString();
        }
        // 获取 @Resource 指定的名称
        if (field.hasAnnotation(InjectionAnnotationRules.Resource.getType())) {
            return Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(InjectionAnnotationRules.Qualifier.getType()))
                    .getElement("name")).toString();
        }

        return defaultName;
    }

}
