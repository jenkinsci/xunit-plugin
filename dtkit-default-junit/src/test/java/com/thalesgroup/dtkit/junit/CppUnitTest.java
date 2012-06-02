package com.thalesgroup.dtkit.junit;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class CppUnitTest extends AbstractTest {


    @Test
    public void cpptestTestcase1() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase1/cppunit-successAndFailure.xml", "cppunit/testcase1/junit-result.xml");
    }

    @Test
    public void cpptestTestcase2() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase2/cppunit-zeroFailure.xml", "cppunit/testcase2/junit-result.xml");
    }

    @Test
    public void cpptestTestcase3() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase3/cppunit-zeroFailureAndSuccess.xml", "cppunit/testcase3/junit-result.xml");
    }

    @Test
    public void cpptestTestcase4() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase4/cppunit-zeroSuccess.xml", "cppunit/testcase4/junit-result.xml");
    }

    @Test
    public void cpptestTestcase5() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase5/cppunit.xml", "cppunit/testcase5/junit-result.xml");
    }

    @Test
    public void cpptestTestcase6() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase6/cppunit.xml", "cppunit/testcase6/junit-result.xml");
    }
}
