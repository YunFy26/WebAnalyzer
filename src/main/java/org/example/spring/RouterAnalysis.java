package org.example.spring;

import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class RouterAnalysis extends ProgramAnalysis {

    public static final String ID = "routerAnalysis";

    public List<ControllerClass> controllerClasses = new ArrayList<>();

    public RouterAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Object analyze() {
        extractUrls();
//        for (ControllerClass controllerClass: controllerClasses){
//            controllerClass.printUrls();
//        }
        return controllerClasses;
    }

    private void extractUrls() {
        World world = World.get();
        Stream<JClass> jClassStream = world.getClassHierarchy().applicationClasses();
        for (JClass jClass: jClassStream.toList()){
            boolean isController = jClass.hasAnnotation("org.springframework.web.bind.annotation.RestController") ||
                    jClass.hasAnnotation("org.springframework.stereotype.Controller");

            if (isController) {
                ControllerClass controllerClass = new ControllerClass(jClass);
                controllerClass.setBaseUrls();

                Collection<JMethod> declaredMethods = jClass.getDeclaredMethods();
                for (JMethod jMethod: declaredMethods){
                    if (jMethod.hasAnnotation("org.springframework.web.bind.annotation.RequestMapping") ||
                            jMethod.hasAnnotation("org.springframework.web.bind.annotation.PostMapping") ||
                            jMethod.hasAnnotation("org.springframework.web.bind.annotation.GetMapping") ||
                            jMethod.hasAnnotation("org.springframework.web.bind.annotation.PutMapping") ||
                            jMethod.hasAnnotation("org.springframework.web.bind.annotation.DeleteMapping") ||
                            jMethod.hasAnnotation("org.springframework.web.bind.annotation.PatchMapping")){

                        RouterMethod routerMethod = new RouterMethod(jMethod);
                        routerMethod.addUrl();
                        controllerClass.addRouterMethod(routerMethod);
                    }
                }
                controllerClasses.add(controllerClass);
            }
        }

    }

}
