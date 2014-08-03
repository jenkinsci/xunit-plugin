package org.jenkinsci.plugins.xunit.types;


import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

/**
 * @author Gregory Boissinot
 */
public class MbUnitInputMetric extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolVersion() {
        return "2.4";
    }

    @Override
    public String getToolName() {
        return "MbUnit";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "mbunit-to-junit-4.xsl";
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
