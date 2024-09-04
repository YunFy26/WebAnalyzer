package org.example.spring;

import pascal.taie.World;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SpringUtils {

    private final ClassHierarchy hierarchy;

    public SpringUtils(ClassHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     * 判断一个字段是否是依赖注入的字段。
     */
    public boolean isDependencyInjectionField(Collection<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            String annotationType = annotation.getType();
            if (annotationType.equals(DependencyInjectionType.Autowired.getType()) ||
                    annotationType.equals(DependencyInjectionType.Inject.getType()) ||
                    annotationType.equals(DependencyInjectionType.Resource.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理依赖注入字段，返回被注入对象的具体实现类
     */
    public JClass processInjectionField(LoadField loadField) {
        JField field = loadField.getFieldRef().resolve();
        JClass fieldClass = hierarchy.getClass(field.getType().getName());
        JClass implClass = getImplClass(fieldClass, field);

        if (implClass != null) {
            return implClass;
        } else {
            System.out.println("No implementation found for injected field: " + field.getDeclaringClass() + "." + field.getName());
            return null;
        }
    }

    /**
     * 获取依赖注入字段的具体实现类
     */
    public JClass getImplClass(JClass fieldClass, JField field) {
        // 先查找所有标记了 @Configuration 的类
        Collection<JClass> configClasses = hierarchy.applicationClasses().filter(jClass ->
                jClass.hasAnnotation(ComponentType.Configuration.getType())
        ).toList();

        // 在这些类中查找带有@Bean的方法
        for (JClass configClass : configClasses) {
            Collection<JMethod> methods = configClass.getDeclaredMethods();
            for (JMethod method : methods) {
                if (method.hasAnnotation(ComponentType.Bean.getType())) {
                    // 如果 @Bean 方法的返回类型匹配注入字段的类型，返回该类型
                    if (method.getReturnType().equals(fieldClass.getType())) {
                        return hierarchy.getClass(method.getReturnType().getName());
                    }
                }
            }
        }

        // 处理 @Qualifier 注解
        if (field.hasAnnotation(DependencyInjectionType.Qualifier.getType())) {
            String qualifierValue = Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(DependencyInjectionType.Qualifier.getType()))
                    .getElement("value")).toString();
            Collection<JClass> subClasses = hierarchy.getAllSubclassesOf(fieldClass);
            for (JClass subClass : subClasses) {
                if (subClass.hasAnnotation(DependencyInjectionType.Qualifier.getType())) {
                    String subClassQualifierValue = Objects.requireNonNull(Objects.requireNonNull(subClass.getAnnotation(DependencyInjectionType.Qualifier.getType()))
                            .getElement("value")).toString();
                    if (qualifierValue.equals(subClassQualifierValue)) {
                        return subClass;
                    }
                }
            }
        }

        // 如果没有 @Qualifier 注解，则按照类型匹配
        Collection<JClass> subClasses = hierarchy.getAllSubclassesOf(fieldClass);
        List<JClass> concreteClasses = subClasses.stream()
                .filter(jClass -> !jClass.isAbstract() && !jClass.isInterface() &&
                        (jClass.hasAnnotation(ComponentType.Service.getType()) || jClass.hasAnnotation(ComponentType.Component.getType())))
                .toList();

        if (concreteClasses.size() == 1) {
            return concreteClasses.get(0);
        }

        // 处理 @Resource 注解，优先通过名称匹配
        if (field.hasAnnotation(DependencyInjectionType.Resource.getType())) {
            String fieldName = Objects.requireNonNull(Objects.requireNonNull(field.getAnnotation(DependencyInjectionType.Resource.getType()))
                    .getElement("name")).toString();
            for (JClass concreteClass : concreteClasses) {
                String componentName = getComponentName(concreteClass);
                if (componentName.equals(fieldName)) {
                    return concreteClass;
                }
            }
        }

        // 默认使用类名匹配
        String fieldName = field.getName();
        for (JClass concreteClass : concreteClasses) {
            String componentName = getComponentName(concreteClass);
            if (componentName.equals(fieldName)) {
                return concreteClass;
            }
        }

        return null;
    }

    /**
     * 判断一个类是否是 Spring Bean。
     */
    public boolean isSpringBean(JClass jClass) {
        return jClass.hasAnnotation(ComponentType.Component.getType()) ||
                jClass.hasAnnotation(ComponentType.Service.getType()) ||
                jClass.hasAnnotation(ComponentType.Repository.getType()) ||
                jClass.hasAnnotation(ComponentType.Controller.getType());
    }

    /**
     * 获取注解中的bean名称
     */
    private String getComponentName(JClass jClass) {
        if (jClass.hasAnnotation(ComponentType.Service.getType()) &&
                Objects.requireNonNull(jClass.getAnnotation(ComponentType.Service.getType())).hasElement("value")) {
            return Objects.requireNonNull(Objects.requireNonNull(jClass.getAnnotation(ComponentType.Service.getType())).getElement("value")).toString();
        } else if (jClass.hasAnnotation(ComponentType.Component.getType()) &&
                Objects.requireNonNull(jClass.getAnnotation(ComponentType.Component.getType())).hasElement("value")) {
            return Objects.requireNonNull(Objects.requireNonNull(jClass.getAnnotation(ComponentType.Component.getType())).getElement("value")).toString();
        }
        // 默认使用类名首字母小写
        String className = jClass.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

}