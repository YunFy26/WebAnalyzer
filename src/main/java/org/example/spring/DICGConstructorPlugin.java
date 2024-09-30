package org.example.spring;

import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.pta.PointerAnalysisResult;
import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.*;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.core.solver.EmptyParamProvider;
import pascal.taie.analysis.pta.core.solver.EntryPoint;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.plugin.Plugin;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.stmt.*;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.TypeSystem;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Add routerMethods in ControllerClass as new entry points
 */
public class DICGConstructorPlugin implements Plugin {

    private Solver solver;

    private ClassHierarchy hierarchy;

    private TypeSystem typeSystem;

    private HeapModel heapModel;

    private CSManager csManager;

    private ContextSelector contextSelector;

    private SpringUtils springUtils;

    private Set<BeanInfo> beanInfoSet;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.hierarchy = solver.getHierarchy();
        this.typeSystem = solver.getTypeSystem();
        this.heapModel = solver.getHeapModel();
        this.csManager = solver.getCSManager();
        this.contextSelector = solver.getContextSelector();
        this.beanInfoSet = World.get().getResult("beanAnalysis");
        this.springUtils = new SpringUtils(hierarchy, beanInfoSet);
    }


    @Override
    public void onStart() {
        World world = World.get();
        List<ControllerClass> routerAnalysis = world.getResult("routerAnalysis");


        // 增加入口点
        for (ControllerClass controllerClass: routerAnalysis){
            List<RouterMethod> routerMethods = controllerClass.getRouterMethods();
            for (RouterMethod routerMethod: routerMethods) {
                solver.addEntryPoint(new EntryPoint(routerMethod.getJMethod(), EmptyParamProvider.get()));
            }
        }

//        for (BeanInfo beanInfo: beanInfoSet) {
//            System.out.println(beanInfo.toString());
//        }

    }


    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        JMethod jMethod = csMethod.getMethod();
        Context context = csMethod.getContext();
        if (isJdkCalls(jMethod)) {
            return;
        }
        List<Stmt> stmts = jMethod.getIR().getStmts();
        HashMap<Var, JField> varField = new HashMap<>();
        List<Invoke> invokeInstanceExps = new ArrayList<>();
        for (Stmt stmt: stmts) {
            // 遍历方法中所有的LoadField语句，将变量跟被加载的field做一个映射
            if(stmt instanceof LoadField loadField){
                Var lValue = loadField.getLValue();
                JField field = loadField.getFieldRef().resolve();
                varField.put(lValue, field);
            }
            // 获取方法中所有调用语句 --> 实例调用
            if (stmt instanceof Invoke invoke){
                if (invoke.isInterface() || invoke.isVirtual()){
                    InvokeExp invokeExp = invoke.getRValue();
                    if (invokeExp instanceof InvokeInstanceExp invokeInstanceExp){
                        invokeInstanceExps.add(invoke);
                    }
                }
            }
        }

        for (Invoke invoke : invokeInstanceExps){
            InvokeInstanceExp invokeInstanceExp = (InvokeInstanceExp) invoke.getRValue();
            Var base = invokeInstanceExp.getBase();
            PointsToSet pointsToSet = solver.getCSManager().getCSVar(context, base).getPointsToSet();
            if (pointsToSet == null || pointsToSet.isEmpty()){
                if (!"%this".equals(base.getName())){
                    JField field = varField.get(base);
                    boolean processed = false;
                    if(field != null){
                        // process Field Injection
                        if (springUtils.isDependencyInjectionField(field)){
                            processed = processFieldInjection(csMethod, field, base, invoke);
                        }
                        // process Setter and Constructor Injection
                        else {
                            processed = processOtherInjection(csMethod, field, base, invoke);
                        }
                        if (!processed){
                            processByType(csMethod, field, base, invoke);
                        }
                    }
                }else {
                    processThis(csMethod, base, invoke);
                }

            }
        }


    }


    @Override
    public void onFinish() {
        PointerAnalysisResult result = solver.getResult();
        //
        JClass aClass = hierarchy.getClass("org.springframework.util.MultiValueMap");

        // 输出url路径及对应的入口方法
        List<ControllerClass> routerAnalysis = World.get().getResult("routerAnalysis");
        for (ControllerClass controllerClass: routerAnalysis){
            controllerClass.printUrls();
        }
        // 输出Beans

        // 输出调用流
        CallGraph<CSCallSite, CSMethod> callGraph = solver.getCallGraph();
        Stream<CSMethod> csMethodStream = callGraph.entryMethods();
        CallGraphExplorer callGraphExplorer = new CallGraphExplorer(callGraph);
        csMethodStream.forEach(csMethod -> {
            try {
                callGraphExplorer.generateDotFile(csMethod);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private boolean isJdkCalls(JMethod jMethod) {
        String packageName = jMethod.getDeclaringClass().getName();
        return packageName.startsWith("java.") ||
                packageName.startsWith("javax.") ||
                packageName.startsWith("sun.") ||
                packageName.startsWith("com.sun.") ||
                packageName.startsWith("jdk.") ||
                packageName.startsWith("org.w3c.dom");
//                packageName.startsWith("com.apple") ||
//                packageName.startsWith("apple");
    }

    /**
     * processFieldInjection 处理Field Injection类型
     *
     * @param csMethod  注入点所在的方法
     * @param field  注入点
     * @param var    跟注入点相关的变量
     * @param invoke 跟变量相关的调用语句
     * @return 是否成功处理
     */
    private boolean processFieldInjection(CSMethod csMethod, JField field, Var var, Invoke invoke){
        boolean processed = false;
        Context context = csMethod.getContext();
        InvokeExp invokeExp = invoke.getRValue();
        InjectionPoint injectionPoint = new InjectionPoint(field);
        String specifyName = injectionPoint.getSpecifyName();
        for (BeanInfo beanInfo : beanInfoSet){
            if (specifyName.equals(beanInfo.getFromAnnotationName()) || specifyName.equals(beanInfo.getDefaultName())){
                Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", invoke.getContainer() + ":" + invokeExp, beanInfo.getBeanClass().getType());
                Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                solver.addVarPointsTo(context, var, heapContext, obj);
                processed = true;
            }
        }
        return processed;
    }

    /**
     * processOntherInjection 处理 Setter, Constructor Injection
     *
     * @param csMethod  注入点所在的方法
     * @param field  注入点
     * @param var    跟注入点相关的变量
     * @param invoke 跟变量相关的调用语句
     * @return 是否成功处理
     */
    private boolean processOtherInjection(CSMethod csMethod, JField field, Var var, Invoke invoke){
        boolean processed = false;
        Context context = csMethod.getContext();
        InvokeExp invokeExp = invoke.getRValue();
        String fieldName = field.getName();
        for (BeanInfo beanInfo : beanInfoSet){
            if (fieldName.equals(beanInfo.getDefaultName()) || fieldName.equals(beanInfo.getFromAnnotationName())){
                Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", invoke.getContainer() + ":" + invokeExp, beanInfo.getBeanClass().getType());
                Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                solver.addVarPointsTo(context, var, heapContext, obj);
                processed = true;
            }
        }
        return processed;
    }


    /**
     * processByType 当按照名称查找对应的bean没有找到时，按照类型去查找bean，然后mock
     * @param csMethod
     * @param field
     * @param var
     * @param invoke
     */
    private void processByType(CSMethod csMethod, JField field, Var var, Invoke invoke){
        Context context = csMethod.getContext();
        InvokeExp invokeExp = invoke.getRValue();
        String type = field.getType().getName();
        JClass jClass = hierarchy.getClass(type);
        Collection<JClass> implementors = new HashSet<>();
        if (jClass != null){
            if (jClass.isInterface()){
                implementors.addAll(hierarchy.getDirectImplementorsOf(jClass));
            }else {
                implementors.addAll(hierarchy.getAllSubclassesOf(jClass));
            }
        }
        Collection<JClass> intersection = new HashSet<>();
        for (JClass aClass : implementors){
            for(BeanInfo beanInfo : beanInfoSet){
                if (beanInfo.getBeanClass().getName().equals(aClass.getName())){
                    intersection.add(aClass);
                }
                break;
            }
        }

        for (JClass jClazz : intersection){
            Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", invoke.getContainer() + ":" + invokeExp, jClass.getType());
            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
            solver.addVarPointsTo(context, var, heapContext, obj);
        }
    }

    /**
     * 处理this变量
     */
    private void processThis(CSMethod csMethod, Var var, Invoke invoke){
        Context context = csMethod.getContext();
        InvokeExp invokeExp = invoke.getRValue();
        String type = var.getType().getName();
        JClass jClass = hierarchy.getClass(type);
        if (jClass != null){
            Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", invoke.getContainer() + ":" + invokeExp, jClass.getType());
            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
            solver.addVarPointsTo(context, var, heapContext, obj);
        }
    }

}


