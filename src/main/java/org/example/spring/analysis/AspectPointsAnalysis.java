package org.example.spring.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.spring.di.bean.BeanInfo;
import org.example.spring.di.injectpoints.InjectPoint;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.config.AnalysisConfig;

import java.util.HashSet;
import java.util.Set;

public class AspectPointsAnalysis extends ProgramAnalysis {

    public static final String ID = "injectPointsAnalysis";

    private static final Logger logger = LogManager.getLogger(AspectPointsAnalysis.class);

    protected AspectPointsAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Object analyze() {
        return null;
    }
}
