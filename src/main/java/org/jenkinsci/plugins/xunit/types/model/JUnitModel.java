package org.jenkinsci.plugins.xunit.types.model;

import org.jenkinsci.lib.dtkit.model.AbstractOutputMetric;

/**
 * @author Gregory Boissinot
 */
public class JUnitModel {

    public static AbstractOutputMetric LATEST = new JUnit10();
}
