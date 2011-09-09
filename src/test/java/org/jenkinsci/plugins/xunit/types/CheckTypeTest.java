package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class CheckTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(CheckInputMetric.class, "check/testcase1/input.xml", "check/testcase1/result.xml");
    }

    @Test
    public void testTestCase2() throws Exception {
        convertAndValidate(CheckInputMetric.class, "check/testcase2/input.xml", "check/testcase2/result.xml");
    }
}
