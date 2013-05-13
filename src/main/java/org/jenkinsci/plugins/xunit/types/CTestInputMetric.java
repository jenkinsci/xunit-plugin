package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;

/**
 * @author Gregory Boissinot
 */
public class CTestInputMetric extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolVersion() {
        return "Version N/A";
    }

    @Override
    public String getToolName() {
        return "CTest";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "ctest-to-junit.xsl";
    }

    @Override
    public String[] getInputXsdNameList() {
        return null;
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return JUnitModel.OUTPUT_JUNIT_5;
    }
}
