package org.example.spring;

import pascal.taie.World;
import pascal.taie.analysis.pta.core.solver.Solver;
import pascal.taie.analysis.pta.core.heap.HeapModel;
import pascal.taie.analysis.pta.plugin.Plugin;
import pascal.taie.language.classes.ClassHierarchy;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.type.TypeSystem;

import java.util.List;

public class AddRouterEntryPlugin implements Plugin {

    private Solver solver;

    private ClassHierarchy hierarchy;

    private TypeSystem typeSystem;

    private HeapModel heapModel;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
        this.hierarchy = solver.getHierarchy();
        this.typeSystem = solver.getTypeSystem();
        this.heapModel = solver.getHeapModel();
    }

    @Override
    public void onStart() {
        World world = World.get();
        List<ControllerClass> routerAnalysis = world.getResult("routerAnalysis");
        for (ControllerClass controllerClass: routerAnalysis){
            JClass jClass = controllerClass.getJClass();
            System.out.println(jClass);
        }

    }
}
