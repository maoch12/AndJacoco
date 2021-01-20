package com.andjacoco.demo.report;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.instr.InstrSupport;
import org.objectweb.asm.ClassReader;

public class TtpAnalyzer extends Analyzer {
    /**
     * Creates a new analyzer reporting to the given output.
     *
     * @param executionData   execution data
     * @param coverageVisitor the output instance that will coverage data for every analyzed
     */
    public TtpAnalyzer(ExecutionDataStore executionData, ICoverageVisitor coverageVisitor) {
        super(executionData, coverageVisitor);
    }

    @Override
    public void analyzeClass(byte[] source) {
        ClassReader reader = InstrSupport.classReaderFor(source);
        if("com/example/jacoco_plugin/Hello".equals(reader.getClassName())){
            super.analyzeClass(source);
        }
    }
}
