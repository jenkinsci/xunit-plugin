package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class JUnitTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase1/input.xml", "junit/testcase1/input.xml");
    }

    @Test
    public void testTestCase2() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase2/input.xml", "junit/testcase2/input.xml");
    }

    @Test
    public void testTestCase3() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase3/input.xml", "junit/testcase3/input.xml");
    }

    @Test
    public void testTestCase4() throws Exception {
        convertAndValidate(JUnitInputMetric.class, "junit/testcase4/input.xml", "junit/testcase4/input.xml");
    }

    @Test
    public void testTestCase5() throws Exception {
    	convertAndValidate(JUnitInputMetric.class, "junit/testcase5/input.xml", "junit/testcase5/input.xml");
    }
}
