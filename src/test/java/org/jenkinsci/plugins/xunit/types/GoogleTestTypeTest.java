package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author David Hallas
 */
public class GoogleTestTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(GoogleTestInputMetric.class, "googletest/testcase1/input.xml", "googletest/testcase1/result.xml");
    }

    @Test
    public void testTestCase2() throws Exception {
        convertAndValidate(GoogleTestInputMetric.class, "googletest/testcase2/input.xml", "googletest/testcase2/result.xml");
    }
}
