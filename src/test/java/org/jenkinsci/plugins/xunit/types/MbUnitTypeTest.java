package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class MbUnitTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(MbUnitInputMetric.class, "mbunit/testcase1/input.xml", "mbunit/testcase1/result.xml");
    }
}