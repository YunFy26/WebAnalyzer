package org.example.rag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.rag.es.ElasticsearchUtils;
import org.example.rag.es.MethodData;
import org.example.vulns.VulnerabilityLoader;
import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;

import java.util.ArrayList;
import java.util.List;

public class RepositoryBuilder {

    private static final Logger logger = LogManager.getLogger(RepositoryBuilder.class);

    private static final VulnerabilityLoader loader = new VulnerabilityLoader("configs/vulns.json");


//    private final CallGraph<Invoke, JMethod> callGraph;

//    private final List<MethodData> methodDataList = new ArrayList<>();

//    public RepositoryBuilder(CallGraph<Invoke, JMethod> callGraph) {
//        this.callGraph = callGraph;
//    }

//    private void setEntityList(){
//        callGraph.reachableMethods().forEach(jMethod -> {
//            String signature = jMethod.getSignature();
//            IR ir = jMethod.getIR();
//            List<Var> vars = ir.getVars();
//            List<Stmt> stmts = ir.getStmts();
//            methodDataList.add(new MethodData(signature, vars, stmts));
//        });
//    }

    public void build(){
//        setEntityList();
//        methodDataList.forEach(ElasticsearchUtils::storeMethodData);
        loader.getVulnerabilities().forEach(ElasticsearchUtils::storeVulnData);
    }
}
