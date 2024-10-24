package org.example.utils;

import org.example.spring.BeanAnnotationRules;
import org.example.spring.BeanInfo;
import org.example.spring.InjectionAnnotationRules;
import org.example.spring.InjectionPoint;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SpringUtils {

    private final ClassHierarchy hierarchy;

    private final Set<BeanInfo> beanInfoSet;


    public SpringUtils(ClassHierarchy hierarchy, Set<BeanInfo> beanInfoSet) {
        this.hierarchy = hierarchy;
        this.beanInfoSet = beanInfoSet;
    }


    /**
     * 判断是否有依赖注入的注解
     */
    private boolean hasDIAnnotation(Collection<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            String annotationType = annotation.getType();
            if (annotationType.equals(InjectionAnnotationRules.Autowired.getType()) ||
                    annotationType.equals(InjectionAnnotationRules.Inject.getType()) ||
                    annotationType.equals(InjectionAnnotationRules.Resource.getType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个字段是否是依赖注入的字段。
     */
    public boolean isDependencyInjectionField(JField field) {
        return hasDIAnnotation(field.getAnnotations());
    }


    /**
     * 判断一个类是否是 Spring Bean。
     */
    public static boolean isSpringBean(JClass jClass) {
        return jClass.hasAnnotation(BeanAnnotationRules.Component.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Service.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Repository.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Controller.getType());
    }

}
