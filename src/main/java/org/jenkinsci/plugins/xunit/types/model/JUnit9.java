package org.jenkinsci.plugins.xunit.types.model;

import com.thalesgroup.dtkit.metrics.model.AbstractOutputMetric;
import com.thalesgroup.dtkit.util.validator.ValidationService;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class JUnit9 extends AbstractOutputMetric implements Serializable {

    public JUnit9() {
        set(new ValidationService());
    }

    @Override
    public String getKey() {
        return "junit";
    }

    @Override
    public String getDescription() {
        return "JUNIT OUTPUT FORMAT 9.0";
    }

    @Override
    public String getVersion() {
        return "9.0";
    }

    @Override
    public String[] getXsdNameList() {
        return new String[]{"xsd/junit-9.xsd"};
    }
}



