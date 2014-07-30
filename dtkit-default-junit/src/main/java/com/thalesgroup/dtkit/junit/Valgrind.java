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
@XmlType(name = "valgrind", namespace = "junit")
@InputMetric
public class Valgrind extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolName() {
        return "Valgrind";
    }

    @Override
    public String getToolVersion() {
        return "N/A";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "valgrind-1.0-to-junit-2.0.xsl";
    }

    @Override
    public String[] getInputXsdNameList() {
        return null;
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return JUnitModel.OUTPUT_JUNIT_2;
    }
}
