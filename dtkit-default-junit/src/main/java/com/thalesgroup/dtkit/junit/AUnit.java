package com.thalesgroup.dtkit.junit;

import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;
import com.thalesgroup.dtkit.processor.InputMetric;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Gregory Boissinot
 */
@XmlType(name = "aunit", namespace = "junit")
@InputMetric
public class AUnit extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolVersion() {
        return "3.x";
    }

    @Override
    public String getToolName() {
        return "AUnit";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "aunit-2.0-to-junit-1.0.xsl";
    }

    @Override
    public String[] getInputXsdNameList() {
        return new String[]{"aunit-2.0.xsd"};
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return JUnitModel.OUTPUT_JUNIT_1_0;
    }
}
