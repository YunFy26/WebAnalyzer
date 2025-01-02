package org.example.spring.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.spring.analysis.InjectPointsAnalysis;
import org.example.spring.di.injectpoints.InjectPoint;
import org.example.spring.router.ControllerClass;
import org.example.spring.router.RouterMethod;
import pascal.taie.World;
import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.CSManager;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.analysis.pta.core.cs.element.CSObj;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.core.solver.EmptyParamProvider;
import pascal.taie.analysis.pta.core.solver.EntryPoint;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.plugin.Plugin;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.exp.InvokeExp;
import pascal.taie.ir.exp.InvokeInstanceExp;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.TypeSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ProcessDIPlugin implements Plugin {

    private Solver solver;

    private ClassHierarchy hierarchy;

    private TypeSystem typeSystem;

    private HeapModel heapModel;

    private CSManager csManager;

    private ContextSelector contextSelector;

    private final Set<InjectPoint> injectPoints = World.get().getResult(InjectPointsAnalysis.ID);


    private final Logger logger = LogManager.getLogger(ProcessDIPlugin.class);

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.hierarchy = solver.getHierarchy();
        this.typeSystem = solver.getTypeSystem();
        this.heapModel = solver.getHeapModel();
        this.csManager = solver.getCSManager();
        this.contextSelector = solver.getContextSelector();
    }

    @Override
    public void onStart() {
        World world = World.get();
        List<ControllerClass> routerAnalysis = world.getResult("routerAnalysis");
        // 增加入口点
        for (ControllerClass controllerClass: routerAnalysis){
            List<RouterMethod> routerMethods = controllerClass.getRouterMethods();
            for (RouterMethod routerMethod: routerMethods) {
                // TODO: Mock parameter for taint analysis
                solver.addEntryPoint(new EntryPoint(routerMethod.getJMethod(), EmptyParamProvider.get()));
            }
        }


    }

    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        String className = csMethod.getMethod().getDeclaringClass().getName();
//        logger.info(injectPoints);
        for (InjectPoint injectPoint : injectPoints){
            // 如果这个方法所属的类在包含注入点的类中
            if (injectPoint.getInClassName().equals(className)){
                String runtimeType = injectPoint.getRuntimeType();
                JMethod jMethod = csMethod.getMethod();
                Context context = csMethod.getContext();
                if (isJdkCalls(jMethod)) {
                    solver.addIgnoredMethod(csMethod.getMethod());
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
                            if (invokeExp instanceof InvokeInstanceExp){
                                invokeInstanceExps.add(invoke);
                            }
                        }
                    }
                }

                for (Invoke invoke : invokeInstanceExps){
                    InvokeInstanceExp invokeInstanceExp = (InvokeInstanceExp) invoke.getRValue();
                    Var base = invokeInstanceExp.getBase();
                    PointsToSet pointsToSet = solver.getCSManager().getCSVar(context, base).getPointsToSet();
                    varField.forEach((var, field) -> {
                        if (injectPoint.getFieldName().equals(field.getName())){
                            JClass aClass = hierarchy.getClass(runtimeType);
                            Obj obj = null;
                            if (aClass != null) {
                                obj = heapModel.getMockObj(() -> "DIObj", invoke.getContainer() + ":" + invokeInstanceExp, aClass.getType());
                            }
                            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                            solver.addVarPointsTo(context, var, heapContext, obj);
                        }
                    });
                    if ("%this".equals(base.getName())){
                        String type = base.getType().getName();
                        JClass jClass = hierarchy.getClass(type);
                        if (jClass != null){
                            Obj obj = heapModel.getMockObj(() -> "DIObj", invoke.getContainer() + ":" + invokeInstanceExp, jClass.getType());
                            Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                            solver.addVarPointsTo(context, base, heapContext, obj);
                        }
                    }

                }
            }

        }



    }

    @Override
    public void onUnresolvedCall(CSObj recv, Context context, Invoke invoke) {
//        logger.info("Recv is : {}, Unresolved call: {}", recv, invoke);

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

    @Override
    public void onPhaseFinish() {
//        logger.info("HeapModel: ");
//        heapModel.getObjects().forEach(System.out::println);
    }
}
