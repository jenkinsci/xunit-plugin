package com.thalesgroup.dtkit.junit;

import org.junit.Test;


/**
 * @author Gregory Boissinot
 */
public class AUnitTest extends AbstractTest {

    @Test
    public void testcase1() throws Exception {
        convertAndValidate(AUnit.class, "aunit/testcase1/testresult.xml", "aunit/testcase1/junit-result.xml");
    }

    @Test
    public void testcase2() throws Exception {
        convertAndValidate(AUnit.class, "aunit/testcase2/testresult.xml", "aunit/testcase2/junit-result.xml");
    }
}
