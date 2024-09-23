package org.example.spring;

import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.graph.callgraph.CallGraphs;
import pascal.taie.analysis.graph.callgraph.Edge;
import pascal.taie.analysis.graph.icfg.ICFG;
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
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.Type;
import pascal.taie.language.type.TypeSystem;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Add routerMethods in ControllerClass as new entry points
 */
public class AddRouterEntryPlugin implements Plugin {

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
//        System.out.println(jMethod.getDeclaringClass().getName());
        Context context = csMethod.getContext();
        if (isJdkCalls(jMethod)) {
            return;
        }
        List<Stmt> stmts = jMethod.getIR().getStmts();
        HashMap<Var, JField> varField = new HashMap<>();
        for (Stmt stmt: stmts) {
            if(stmt instanceof LoadField loadField){
                Var lValue = loadField.getLValue();
                JField field = loadField.getFieldRef().resolve();
                varField.put(lValue, field);
                Collection<Annotation> annotations = field.getAnnotations();
                // 首先处理Field Injection，为所有由Field Injection注入的Field更新指针集
                if (springUtils.isDependencyInjectionField(field)){
//                    System.out.println(field.getName());
                    String specifyName = springUtils.processFieldInjection(field);
//                    boolean processed = false;
                    for (BeanInfo beanInfo: beanInfoSet){
                        if (specifyName.equals(beanInfo.getFromAnnotationName()) || specifyName.equals(beanInfo.getDefaultName())){
                            Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", field.getRef(), beanInfo.getBeanClass().getType());
                            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                            solver.addVarPointsTo(context, lValue, heapContext, obj);
//                            processed = true;
                        }
                    }
//                    // TODO
                }
            }
            // 处理setter Injection 和 constructor Injection
            if(stmt instanceof Invoke invoke){
                if (invoke.isInterface() || invoke.isVirtual()){
                    InvokeExp invokeExp = invoke.getRValue();
                    if (invokeExp instanceof InvokeInstanceExp invokeInstanceExp){
                        Var base = invokeInstanceExp.getBase();
                        if (solver.getCSManager().getCSVar(context, base).getPointsToSet() == null || Objects.requireNonNull(solver.getCSManager().getCSVar(context, base).getPointsToSet()).isEmpty()){
                            if(!base.getName().equals("%this")){
                                // 变量对应的field
                                JField field = varField.get(base);
                                boolean processed = false;
                                if (field != null){
                                    String fieldName = field.getName();
                                    // 按照fieldName名称匹配注入的bean
                                    for (BeanInfo beanInfo : beanInfoSet){
                                        if (beanInfo.getDefaultName().equals(fieldName)){
                                            Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", field.getRef(), beanInfo.getBeanClass().getType());
                                            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                                            solver.addVarPointsTo(context, base, heapContext, obj);
                                            processed = true;
                                        }
                                    }
                                }
                                if (!processed){

                                }
                            }else {
                                Type type = base.getType();
                                JClass aClass = hierarchy.getClass(type.getName());
                                if (aClass != null){
                                    Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", base.getName(), aClass.getType());
                                    Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                                    solver.addVarPointsTo(context, base, heapContext, obj);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onFinish() {
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
        PointerAnalysisResult result = solver.getResult();
        System.out.println(result);


    }


    public boolean isJdkCalls(JMethod jMethod) {
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

}
