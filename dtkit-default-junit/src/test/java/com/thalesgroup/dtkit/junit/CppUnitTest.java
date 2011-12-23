package com.thalesgroup.dtkit.junit;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class CppUnitTest extends AbstractTest {

    @Test
    public void cpptestTestcase5() throws Exception {
        convertAndValidate(CppUnit.class, "cppunit/testcase5/cppunit.xml", "cppunit/testcase5/junit-result.xml");
    }
}
