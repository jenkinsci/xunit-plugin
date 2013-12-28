package org.jenkinsci.plugins.xunit.types.model;

import com.thalesgroup.dtkit.metrics.model.AbstractOutputMetric;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class JUnit8 extends AbstractOutputMetric implements Serializable {

    @Override
    public String getKey() {
        return "junit";
    }

    @Override
    public String getDescription() {
        return "JUNIT OUTPUT FORMAT 8.0";
    }

    @Override
    public String getVersion() {
        return "8.0";
    }

    @Override
    public String[] getXsdNameList() {
        return new String[]{"xsd/junit-8.xsd"};
    }
}


