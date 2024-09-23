package org.example.spring;

import pascal.taie.language.classes.JField;

import java.util.Objects;

/**
 * 实体类
 * 对于FieldInjection这种注入方式（即直接在field上面写依赖注入相关的注解）实例化一个FieldOfFieldInjection类
 * 用于提供相关操作接口
 */
public class FieldOfFieldInjection {

    private final JField field;

    private final String defaultName;

    public FieldOfFieldInjection(JField field) {
        this.field = field;
        this.defaultName = field.getName();
    }

    public String getSpecifyName() {
        // @Qualifier 和 @Resource 一般不同时出现，@Qualifier是辅助 @Autowired 和 @Inject 的
        // 获取 @Qualifier 指定的名称
        if (field.hasAnnotation(DependencyInjectionType.Qualifier.getType())) {
            return Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(DependencyInjectionType.Qualifier.getType()))
                    .getElement("value")).toString();
        }
        // 获取 @Resource 指定的名称
        if (field.hasAnnotation(DependencyInjectionType.Resource.getType())) {
            return Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(DependencyInjectionType.Qualifier.getType()))
                    .getElement("name")).toString();
        }

        return defaultName;
    }

}
