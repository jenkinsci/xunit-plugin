package org.jenkinsci.plugins.xunit.types.model;


import org.jenkinsci.lib.dtkit.model.AbstractOutputMetric;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class JUnit10 extends AbstractOutputMetric implements Serializable {

    @Override
    public String getKey() {
        return "junit";
    }

    @Override
    public String getDescription() {
        return "JUNIT OUTPUT FORMAT 10.0";
    }

    @Override
    public String getVersion() {
        return "10.0";
    }

    @Override
    public String[] getXsdNameList() {
        return new String[]{"xsd/junit-10.xsd"};
    }
}



