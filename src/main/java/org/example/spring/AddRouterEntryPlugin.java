package org.example.spring;

import pascal.taie.World;
import pascal.taie.analysis.graph.callgraph.Edge;
import pascal.taie.analysis.pta.core.cs.context.Context;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSManager;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.analysis.pta.core.cs.element.CSVar;
import pascal.taie.analysis.pta.core.cs.selector.ContextSelector;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.core.solver.EmptyParamProvider;
import pascal.taie.analysis.pta.core.solver.EntryPoint;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.plugin.Plugin;
import pascal.taie.analysis.pta.pts.PointsToSet;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.proginfo.FieldRef;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.LoadField;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.language.type.TypeSystem;

import java.util.Collection;
import java.util.List;
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

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.hierarchy = solver.getHierarchy();
        this.typeSystem = solver.getTypeSystem();
        this.heapModel = solver.getHeapModel();
        this.csManager = solver.getCSManager();
        this.contextSelector = solver.getContextSelector();
        this.springUtils = new SpringUtils(hierarchy);
    }

    @Override
    public void onStart() {
        World world = World.get();
        List<ControllerClass> routerAnalysis = world.getResult("routerAnalysis");

        for (ControllerClass controllerClass: routerAnalysis){
            List<RouterMethod> routerMethods = controllerClass.getRouterMethods();
            for (RouterMethod routerMethod: routerMethods) {
                solver.addEntryPoint(new EntryPoint(routerMethod.getJMethod(), EmptyParamProvider.get()));
            }
        }
//        solver.getCallGraph().entryMethods().forEach(entryMethod -> {
//            System.out.println("Entry Method: " + entryMethod.getMethod().getDeclaringClass().getName() + "." + entryMethod.getMethod().getName());
//        });

    }


    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        System.out.println(csMethod.getMethod().getDeclaringClass() + " " + csMethod.getMethod().getName());
        JMethod jMethod = csMethod.getMethod();
        Context context = csMethod.getContext();

        if (isJdkCalls(jMethod)) {
            solver.addIgnoredMethod(jMethod);
        }
        List<Stmt> stmts = jMethod.getIR().getStmts();
        for (Stmt stmt: stmts) {
            if(stmt instanceof LoadField loadField){
                JField field = loadField.getFieldRef().resolve();
                Collection<Annotation> annotations = field.getAnnotations();
                if (springUtils.isDependencyInjectionField(annotations)) {
                    JClass jClass = springUtils.processInjectionField(loadField);
                    if (jClass != null) {
                        Obj obj = heapModel.getMockObj(() -> "DependencyInjectionObj", field.getRef(), jClass.getType());
                        Var var = loadField.getLValue();
                        Context heapContext = contextSelector.selectHeapContext(csMethod, obj);
                        solver.addVarPointsTo(context, var, heapContext, obj);
                    }
                }
            }
        }


    }

    @Override
    public void onNewPointsToSet(CSVar csVar, PointsToSet pts) {

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
