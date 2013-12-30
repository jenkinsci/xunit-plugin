package com.thalesgroup.dtkit.junit;

import org.junit.Test;

public class BoostTestTest extends AbstractTest {

    @Test
    public void testAutoTest() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/autotest/testlog.xml", "boosttest/autotest/junit-result.xml");
    }

    @Test
    public void testAutoTestMultiple() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/autotest-multiple/testlog.xml", "boosttest/autotest-multiple/junit-result.xml");
    }

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase1/testlog.xml", "boosttest/testcase1/junit-result.xml");
    }

    @Test
    public void testTestCase2() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase2/testlog.xml", "boosttest/testcase2/junit-result.xml");
    }

    @Test
    public void testTestCase3() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase3/testlog.xml", "boosttest/testcase3/junit-result.xml");
    }

    @Test
    public void testTestCase4() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase4/testlog.xml", "boosttest/testcase4/junit-result.xml");
    }

    @Test
    public void testTestCase5() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase5/testlog.xml", "boosttest/testcase5/junit-result.xml");
    }

    @Test
    public void testTestCase6() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase6/testlog.xml", "boosttest/testcase6/junit-result.xml");
    }

    @Test
    public void testTestCase7() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase7/testlog.xml", "boosttest/testcase7/junit-result.xml");
    }

    @Test
    public void testTestCase8() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase8/testlog.xml", "boosttest/testcase8/junit-result.xml");
    }

    @Test
    public void testTestCase9() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase9/testlog.xml", "boosttest/testcase9/junit-result.xml");
    }

    @Test
    public void testTestCase10() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase10/testlog.xml", "boosttest/testcase10/junit-result.xml");
    }

    @Test
    public void testTestCase11() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase11/testlog.xml", "boosttest/testcase11/junit-result.xml");
    }

    @Test
    public void testTestCase12() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase12/testlog.xml", "boosttest/testcase12/junit-result.xml");
    }

    @Test
    public void testTestCase13() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase13/testlog.xml", "boosttest/testcase13/junit-result.xml");
    }

    @Test
    public void testTestCase14() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase14/testlog.xml", "boosttest/testcase14/junit-result.xml");
    }

    @Test
    public void testTestCase15() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase15/testlog.xml", "boosttest/testcase15/junit-result.xml");
    }

    @Test
    public void testTestCase16() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase16/testlog.xml", "boosttest/testcase16/junit-result.xml");
    }

    @Test
    public void testTestCase17() throws Exception {
        convertAndValidate(BoostTest.class, "boosttest/testcase17/testlog.xml", "boosttest/testcase17/junit-result.xml");
    }
}
