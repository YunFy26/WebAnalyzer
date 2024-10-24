package org.example.spring;

import org.example.utils.SpringUtils;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;

import java.util.*;
import java.util.stream.Stream;

public class BeanAnalysis extends ProgramAnalysis {

    public static final String ID = "beanAnalysis";

    private final Set<BeanInfo> beanInfoSet = new HashSet<>();


    public BeanAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Object analyze() {
        extractBeans();
//        for(BeanInfo beanInfo : beanInfoSet){
//            System.out.println(beanInfo.toString());
//        }
        return beanInfoSet;
    }

    private void extractBeans() {
        World world = World.get();
        Stream<JClass> jClassStream = world.getClassHierarchy().applicationClasses();

        for (JClass jClass : jClassStream.toList()) {
            // 查找带有 @Component、@Service、@Controller、@Repository 注解的类
            if (SpringUtils.isSpringBean(jClass)) {
                String fromAnnotationName = getBeanNameFromAnnotations(jClass);
                BeanInfo beanInfo = new BeanInfo(jClass, fromAnnotationName);
                beanInfoSet.add(beanInfo);
            }

            // 查找带有 @Configuration 注解的类并处理 @Bean 方法
            if (jClass.hasAnnotation(BeanAnnotationRules.Configuration.getType())) {
                Collection<JMethod> methods = jClass.getDeclaredMethods();
                for (JMethod method : methods) {
                    if (method.hasAnnotation(BeanAnnotationRules.Bean.getType())) {
                        Annotation annotation = method.getAnnotation(BeanAnnotationRules.Bean.getType());
                        if (annotation != null) {
                            String fromAnnotationName = null;
                            if (annotation.hasElement("value")) {
                                fromAnnotationName = Objects.requireNonNull(annotation.getElement("value")).toString();
                            }
                            JClass beanClass = world.getClassHierarchy().getClass(method.getReturnType().getName());
//                            System.out.println(beanClass);
                            if (beanClass != null) {
                                if (beanClass.isInterface()) {
                                    Collection<JClass> directImplementorsOf = world.getClassHierarchy().getDirectImplementorsOf(beanClass);
                                    for (JClass implementor : directImplementorsOf) {
                                        if (!implementor.isAbstract()) {
                                            BeanInfo beanInfo = new BeanInfo(implementor, fromAnnotationName);
                                            beanInfoSet.add(beanInfo);
                                        }
                                    }
                                } else {
                                    Collection<JClass> subclasses = world.getClassHierarchy().getAllSubclassesOf(beanClass);
                                    for (JClass subclass : subclasses) {
                                        if (!subclass.isAbstract()) {
                                            BeanInfo beanInfo = new BeanInfo(subclass, fromAnnotationName);
                                            beanInfoSet.add(beanInfo);
                                        }
                                    }
                                }
//                                BeanInfo beanInfo = new BeanInfo(beanClass, fromAnnotationName);
//                                beanInfoSet.add(beanInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取类注解上指定的名称，如果没有指定名称，则返回 null
     */
    private String getBeanNameFromAnnotations(JClass jClass) {
        Annotation componentAnnotation = jClass.getAnnotation(BeanAnnotationRules.Component.getType());
        Annotation serviceAnnotation = jClass.getAnnotation(BeanAnnotationRules.Service.getType());
        Annotation controllerAnnotation = jClass.getAnnotation(BeanAnnotationRules.Controller.getType());
        Annotation repositoryAnnotation = jClass.getAnnotation(BeanAnnotationRules.Repository.getType());

        // 检查所有相关注解的 "value" 元素
        if (componentAnnotation != null && componentAnnotation.hasElement("value")) {
            return Objects.requireNonNull(componentAnnotation.getElement("value")).toString();
        }
        if (serviceAnnotation != null && serviceAnnotation.hasElement("value")) {
            return Objects.requireNonNull(serviceAnnotation.getElement("value")).toString();
        }
        if (controllerAnnotation != null && controllerAnnotation.hasElement("value")) {
            return Objects.requireNonNull(controllerAnnotation.getElement("value")).toString();
        }
        if (repositoryAnnotation != null && repositoryAnnotation.hasElement("value")) {
            return Objects.requireNonNull(repositoryAnnotation.getElement("value")).toString();
        }

        return null;
    }

}