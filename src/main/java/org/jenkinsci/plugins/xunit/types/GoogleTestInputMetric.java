package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;

/**
 * @author David Hallas
 */
public class GoogleTestInputMetric extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolVersion() {
        return "1.6";
    }

    @Override
    public String getToolName() {
        return "GoogleTest";
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public String getXslName() {
        return "googletest-to-junit-4.xsl";
    }

    @Override
    public String[] getInputXsdNameList() {
        return null;
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return JUnitModel.OUTPUT_JUNIT_4;
    }
}
