package org.jenkinsci.plugins.xunit.types;


import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

/**
 * @author Gregory Boissinot
 */
public class CheckInputMetric extends InputMetricXSL {

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
        return "Check";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "check-to-junit-4.xsl";
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
