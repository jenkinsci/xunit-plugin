package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class JUnitTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase1/input.xml", "junit/testcase1/result.xml");
    }

    @Test
    public void testTestCase2() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase2/input.xml", "junit/testcase2/result.xml");
    }
}
