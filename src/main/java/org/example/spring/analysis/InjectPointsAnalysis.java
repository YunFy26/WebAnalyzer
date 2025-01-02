package org.example.spring.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.spring.di.bean.BeanAnnotationRules;
import org.example.spring.di.bean.BeanInfo;
import org.example.spring.di.injectpoints.*;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.stmt.StoreField;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;

import java.util.*;
import java.util.stream.Stream;

public class InjectPointsAnalysis extends ProgramAnalysis {

    public static final String ID = "injectPointsAnalysis";

    private final Set<BeanInfo> beanInfoSet = World.get().getResult(BeanAnalysis.ID);

    private final Set<InjectPoint> injectPoints = new HashSet<>();

    private static final Logger logger = LogManager.getLogger(InjectPointsAnalysis.class);

    public InjectPointsAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Object analyze() {
        fieldInjectionAnalyze();
        constructorInjectionAnalyze();
        setterInjectionAnalyze();
        return injectPoints;
    }

    private void fieldInjectionAnalyze() {
        logger.info("\n==================================================");
        logger.info("【字段注入分析：开始】");
        logger.info("==================================================\n");

        World world = World.get();
        Stream<JClass> jClassStream = world.getClassHierarchy().applicationClasses();
        jClassStream.forEach(jClass -> {
            Collection<JField> declaredFields = jClass.getDeclaredFields();
            declaredFields.forEach(jField -> processField(jClass, jField));
        });

        logger.info("\n==================================================");
        logger.info("【字段注入分析：结束】");
        logger.info("==================================================\n");
    }

    private void processField(JClass inClass, JField jField) {
        String fieldName = jField.getName();
        Collection<Annotation> fieldAnnotations = jField.getAnnotations();
        if (hasDIAnnotation(fieldAnnotations)) {
            logger.info("--------------------------------------------------------------------------------");
            logger.info(" [字段注入] 类：'{}' 中的字段：'{}' 检测到依赖注入注解", inClass.getName(), fieldName);
            logger.info(" --> 开始分析可能被注入的 Bean...");

            String specifyName = getSpecifyName(fieldAnnotations);
            if (!specifyName.isEmpty()) {
                logger.info(" --> 注解上明确指定了 Bean 名称：'{}'", specifyName);
            } else {
                logger.info(" --> 注解上没有明确指定 Bean 名称");
            }

            logger.info(" --> 根据字段名 / 注解指定的名称尝试查找匹配的 Bean...");
            String findClass = findByName(jField.getName(), getSpecifyName(jField.getAnnotations()));
            if (findClass != null) {
                logger.info(" >>> 找到匹配的 Bean：'{}'，将被注入字段：'{}'", findClass, fieldName);
                FieldPoint fieldPoint = new FieldPoint(inClass, fieldName, findClass);
                fieldPoint.setSpecifyName(specifyName);
                fieldPoint.setFieldType(jField.getType().getName());
                fieldPoint.setAnnotations(fieldAnnotations);
                injectPoints.add(fieldPoint);
            } else {
                logger.info(" --> 无法根据字段名找到匹配的 Bean，尝试通过字段类型进行查找...");
                HashSet<String> findClasses = findByType(jField.getType().getName());
                if (findClasses.isEmpty()) {
                    logger.warn(" [警告] 在类：'{}' 中，字段：'{}' 无法找到具体的 Bean", inClass.getName(), fieldName);
                } else {
                    for (String findClassType : findClasses) {
                        logger.info(" >>> 根据字段类型找到匹配的 Bean：'{}'，将被注入字段：'{}'", findClassType, fieldName);
                        FieldPoint fieldPoint = new FieldPoint(inClass, fieldName, findClassType);
                        fieldPoint.setSpecifyName(specifyName);
                        fieldPoint.setFieldType(jField.getType().getName());
                        fieldPoint.setAnnotations(fieldAnnotations);
                        injectPoints.add(fieldPoint);
                    }
                }
            }
            logger.info("--------------------------------------------------------------------------------\n");
        }
    }




    private void constructorInjectionAnalyze() {
        logger.info("\n==================================================");
        logger.info("【构造方法注入分析：开始】");
        logger.info("==================================================\n");

        World world = World.get();
        Stream<JClass> jClassStream = world.getClassHierarchy().applicationClasses();
        jClassStream.forEach(jClass -> {
            List<JMethod> constructors = getConstructors(jClass);
            if (constructors.size() == 1 && isSpringBean(jClass) && constructors.get(0).getParamCount() > 0) {
                logger.info("--------------------------------------------------------------------------------");
                logger.info(" [构造方法注入] 类：'{}' 是一个 Spring Bean，并且只有一个构造方法", jClass.getName());
                logger.info(" --> 该构造方法将被视为默认的依赖注入入口...");
                processConstructor(jClass, constructors.get(0));
                logger.info("--------------------------------------------------------------------------------\n");
            }
            // TODO: 如果类是Bean且存在多个构造方法，这里暂时不做处理
        });

        logger.info("\n==================================================");
        logger.info("【构造方法注入分析：结束】");
        logger.info("==================================================\n");
    }

    private List<JMethod> getConstructors(JClass jClass) {
        Collection<JMethod> declaredMethods = jClass.getDeclaredMethods();
        List<JMethod> constructors = new ArrayList<>();
        declaredMethods.forEach(jMethod -> {
            if (jMethod.isConstructor()) {
                constructors.add(jMethod);
            }
        });
        return constructors;
    }

    private void processConstructor(JClass jClass, JMethod constructor) {
        logger.info(" --> 扫描构造方法体，查看参数与字段的关联关系...");
        Map<String, String> fieldMapParam = getFieldMapParam(constructor);
        fieldMapParam.forEach((key, value) ->
                logger.info("     构造方法参数：'{}' 被赋值给字段：'{}'", value, key)
        );

        Collection<Annotation> methodAnnotations = constructor.getAnnotations();
        String specifyName = getSpecifyName(methodAnnotations);
        int paramCount = constructor.getParamCount();
        for (int i = 0; i < paramCount; i++) {
            processConstructorParam(jClass, constructor, i, fieldMapParam, methodAnnotations, specifyName);
        }
    }

    private Map<String, String> getFieldMapParam(JMethod constructor) {
        Map<String, String> fieldMapParam = new HashMap<>();
        constructor.getIR().getStmts().forEach(stmt -> {
            if (stmt instanceof StoreField storeField) {
                fieldMapParam.put(storeField.getLValue().getFieldRef().getName(), storeField.getRValue().getName());
            }
        });
        return fieldMapParam;
    }

    private void processConstructorParam(JClass inClass,
                                         JMethod constructor,
                                         int paramIndex,
                                         Map<String, String> fieldMapParam,
                                         Collection<Annotation> methodAnnotations,
                                         String specifyName) {
        String paramName = constructor.getParamName(paramIndex);
        String fieldName = fieldMapParam.getOrDefault(paramName, "");
        Type paramType = constructor.getParamType(paramIndex);

        Collection<Annotation> paramAnnotations = constructor.getParamAnnotations(paramIndex);
        if (hasDIAnnotation(paramAnnotations)) {
            specifyName = getSpecifyName(paramAnnotations);
        }

        logger.info(" -------------------------------------------------");
        logger.info("  > 构造方法第 {} 个参数：'{}'", paramIndex, paramName);
        logger.info("    - 对应的字段：'{}'", fieldName.isEmpty() ? "[没有匹配的字段]" : fieldName);
        logger.info("    - 参数类型：'{}'", paramType.getName());
        logger.info("    - 注解指定的 Bean 名称：'{}'", specifyName.isEmpty() ? "[未指定]" : specifyName);

        logger.info("    --> 尝试通过参数名 / 注解指定名称查找 Bean...");
        String findClass = findByName(paramName, specifyName);
        if (findClass != null) {
            logger.info("     >>> 找到 Bean：'{}'（通过名称匹配）为参数：'{}' 注入", findClass, paramName);
            ConstructorPoint constructorPoint = new ConstructorPoint(inClass, fieldName, findClass);
            constructorPoint.setParamName(paramName);
            constructorPoint.setSpecifyName(specifyName);
            constructorPoint.setParamType(paramType.getName());
            constructorPoint.setMethodAnnotations(methodAnnotations);
            constructorPoint.setParamAnnotations(paramAnnotations);
            injectPoints.add(constructorPoint);
        } else {
            logger.info("    --> 无法通过名称匹配 Bean，开始通过参数类型进行查找...");
            HashSet<String> findClasses = findByType(paramType.getName());
            if (findClasses.isEmpty()) {
                logger.warn(" [警告] 在类：'{}' 中，构造方法参数：'{}' 无法找到具体的 Bean", inClass.getName(), paramName);
            } else {
                for (String findClassType : findClasses) {
                    logger.info("     >>> 找到 Bean：'{}'（通过类型匹配）为参数：'{}' 注入", findClassType, paramName);
                    ConstructorPoint constructorPoint = new ConstructorPoint(inClass, fieldName, findClassType);
                    constructorPoint.setParamName(paramName);
                    constructorPoint.setSpecifyName(specifyName);
                    constructorPoint.setParamType(paramType.getName());
                    constructorPoint.setMethodAnnotations(methodAnnotations);
                    constructorPoint.setParamAnnotations(paramAnnotations);
                    injectPoints.add(constructorPoint);
                }
            }
        }
        logger.info(" -------------------------------------------------\n");
    }

    private void setterInjectionAnalyze() {
        logger.info("\n==================================================");
        logger.info("【Setter 方法注入分析：开始】");
        logger.info("==================================================\n");

        World world = World.get();
        Stream<JClass> jClassStream = world.getClassHierarchy().applicationClasses();
        jClassStream.forEach(jClass -> {
            if (isSpringBean(jClass)) {
                Collection<JMethod> declaredMethods = jClass.getDeclaredMethods();
                declaredMethods.forEach(jMethod -> {
                    if (isSetterMethod(jMethod)) {
                        processSetterMethod(jClass, jMethod);
                    }
                });
            }
        });

        logger.info("\n==================================================");
        logger.info("【Setter 方法注入分析：结束】");
        logger.info("==================================================\n");
    }

    private boolean isSetterMethod(JMethod jMethod) {
        return jMethod.getName().startsWith("set") && jMethod.getParamCount() == 1;
    }

    private void processSetterMethod(JClass inClass, JMethod jMethod) {
        logger.info("--------------------------------------------------------------------------------");
        logger.info(" [Setter 方法注入] 检测到类：'{}' 中的 setter 方法：'{}'", inClass.getName(), jMethod.getName());

        String methodName = jMethod.getName();
        String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        logger.info(" --> 推断该 setter 方法对应的字段名称：'{}'", fieldName);

        String paramName = jMethod.getParamName(0);
        Type paramType = jMethod.getParamType(0);
        Collection<Annotation> methodAnnotations = jMethod.getAnnotations();
        Collection<Annotation> paramAnnotations = jMethod.getParamAnnotations(0);

        String specifyName = getSpecifyName(methodAnnotations, paramAnnotations);
        logger.info(" --> 方法参数名：'{}'，参数类型：'{}'", paramName, paramType.getName());
        logger.info(" --> 注解指定的 Bean 名称：'{}'", specifyName.isEmpty() ? "[未指定]" : specifyName);

        logger.info(" --> 尝试通过参数名 / 注解指定名称查找 Bean...");
        String findClass = findByName(paramName, specifyName);
        if (findClass != null) {
            logger.info("  >>> 找到 Bean：'{}'（通过名称匹配）为参数：'{}' 注入", findClass, paramName);
            SetterPoint setterPoint = new SetterPoint(inClass, fieldName, findClass);
            setterPoint.setSetterName(jMethod.getName());
            setterPoint.setParamName(paramName);
            setterPoint.setSpecifyName(specifyName);
            setterPoint.setParamType(paramType.getName());
            setterPoint.setMethodAnnotations(methodAnnotations);
            setterPoint.setParaAnnotations(paramAnnotations);
            injectPoints.add(setterPoint);
        } else {
            logger.info(" --> 无法通过名称找到匹配的 Bean，尝试通过参数类型进行查找...");
            HashSet<String> findClasses = findByType(paramType.getName());
            if (findClasses.isEmpty()) {
                logger.warn(" [警告] 在类：'{}' 中，setter 方法参数：'{}' 无法找到具体的 Bean", inClass.getName(), paramName);
            } else {
                for (String findClassType : findClasses) {
                    logger.info("  >>> 找到 Bean：'{}'（通过类型匹配）为参数：'{}' 注入", findClassType, paramName);
                    SetterPoint setterPoint = new SetterPoint(inClass, fieldName, findClassType);
                    setterPoint.setSetterName(jMethod.getName());
                    setterPoint.setParamName(paramName);
                    setterPoint.setSpecifyName(specifyName);
                    setterPoint.setParamType(paramType.getName());
                    setterPoint.setMethodAnnotations(methodAnnotations);
                    setterPoint.setParaAnnotations(paramAnnotations);
                    injectPoints.add(setterPoint);
                }
            }
        }
        logger.info("--------------------------------------------------------------------------------\n");
    }

    private String getSpecifyName(Collection<Annotation> methodAnnotations, Collection<Annotation> paramAnnotations) {
        String specifyName = getSpecifyName(methodAnnotations);
        if (specifyName.isEmpty()) {
            specifyName = getSpecifyName(paramAnnotations);
        }
        return specifyName;
    }

    private boolean hasDIAnnotation(Collection<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            String annotationType = annotation.getType();
            if (annotationType.equals(InjectionAnnotationRules.Autowired.getType()) ||
                    annotationType.equals(InjectionAnnotationRules.Inject.getType()) ||
                    annotationType.equals(InjectionAnnotationRules.Resource.getType()) ||
                    annotationType.equals(InjectionAnnotationRules.Qualifier.getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpringBean(JClass jClass) {
        return jClass.hasAnnotation(BeanAnnotationRules.Component.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Service.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Repository.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.Controller.getType()) ||
                jClass.hasAnnotation(BeanAnnotationRules.RestController.getType());
    }

    public String getSpecifyName(Collection<Annotation> annotations) {
        if (hasDIAnnotation(annotations)) {
            for (Annotation annotation : annotations) {
                String annotationType = annotation.getType();
                // @Qualifier 和 @Resource 一般不同时出现
                if (annotationType.equals(InjectionAnnotationRules.Qualifier.getType())) {
                    return Objects.requireNonNull(annotation.getElement("value")).toString();
                }
                if (annotationType.equals(InjectionAnnotationRules.Resource.getType())) {
                    return Objects.requireNonNull(annotation.getElement("name")).toString();
                }
            }
        }
        return "";
    }

    private String findByName(String paramName, String specifyName) {
        for (BeanInfo beanInfo : beanInfoSet) {
            if (beanInfo.getDefaultName().equals(paramName) || specifyName.equals(beanInfo.getFromAnnotationName())) {
                return beanInfo.getBeanClass().getType().getName();
            }
        }
        return null;
    }


    private HashSet<String> findByType(String typeName) {
        JClass aClass = World.get().getClassHierarchy().getClass(typeName);
        HashSet<String> result = new HashSet<>();
        Map<String, BeanInfo> beanInfoMap = new HashMap<>();
        for (BeanInfo beanInfo : beanInfoSet) {
            beanInfoMap.put(beanInfo.getBeanClass().getName(), beanInfo);
        }
        if (aClass != null) {
            if (aClass.isInterface()) {
                Collection<JClass> implementors = World.get().getClassHierarchy().getDirectImplementorsOf(aClass);
                for (JClass implementor : implementors) {
                    BeanInfo matchingBeanInfo = beanInfoMap.get(implementor.getName());
                    if (matchingBeanInfo != null) {
                        result.add(matchingBeanInfo.getBeanClass().getType().getName());
                    }
                }
            } else {
                BeanInfo matchingBeanInfo = beanInfoMap.get(aClass.getName());
                if (matchingBeanInfo != null) {
                    result.add(matchingBeanInfo.getBeanClass().getType().getName());
                }
            }
        }
        return result;
    }
}