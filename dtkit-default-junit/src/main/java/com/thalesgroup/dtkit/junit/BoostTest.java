package com.thalesgroup.dtkit.junit;

import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;
import com.thalesgroup.dtkit.processor.InputMetric;

import javax.xml.bind.annotation.XmlType;


@XmlType(name = "boosttest", namespace = "junit")
@InputMetric
public class BoostTest extends InputMetricXSL {

    @Override
    public InputType getToolType() {
        return InputType.TEST;
    }

    @Override
    public String getToolName() {
        return "BoostTest";
    }

    @Override
    public String getToolVersion() {
        return "1.x";
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String getXslName() {
        return "boosttest-1.2-to-junit-4.xsl";
    }

    @Override
    public String[] getInputXsdNameList() {
        return new String[]{"boosttest-1.3.0.xsd"};
    }

    @Override
    public OutputMetric getOutputFormatType() {
        return JUnitModel.OUTPUT_JUNIT_4;
    }
}
