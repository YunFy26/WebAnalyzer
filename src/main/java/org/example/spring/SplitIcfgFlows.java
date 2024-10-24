package org.example.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.ICFGPrinter;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.graph.icfg.*;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;

/**
 * output the icfg for each entry point
 */
public class SplitIcfgFlows extends ProgramAnalysis {

    private final Logger logger = LogManager.getLogger(SplitIcfgFlows.class);

    public static final String ID = "splitIcfgFlows";


    public SplitIcfgFlows(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Object analyze() {
        ICFG<JMethod, Stmt> icfg = World.get().getResult(ICFGBuilder.ID);
        ICFGPrinter icfgPrinter = new ICFGPrinter(icfg);
        icfgPrinter.dumpICFG();
        return null;
    }

}
