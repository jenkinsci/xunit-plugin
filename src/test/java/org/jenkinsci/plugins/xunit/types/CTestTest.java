package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class CTestTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(CTestInputMetric.class, "ctest/testcase1/input.xml", "ctest/testcase1/result.xml");
        convertAndValidate(CTestInputMetric.class, "ctest/testcase2/input.xml", "ctest/testcase2/result.xml");
    }

}
