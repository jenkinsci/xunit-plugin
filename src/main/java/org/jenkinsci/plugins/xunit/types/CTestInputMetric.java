package org.jenkinsci.plugins.xunit.types;


import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

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
        return JUnitModel.LATEST;
    }
}
