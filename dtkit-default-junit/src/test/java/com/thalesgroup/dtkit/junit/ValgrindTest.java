package com.thalesgroup.dtkit.junit;

import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Gregory Boissinot
 */
public class ValgrindTest extends AbstractTest {

    @Test
    @Ignore	
    public void testcase1() throws Exception {
        convertAndValidate(Valgrind.class, "valgrind/testcase1/valgrind-out.xml", "valgrind/testcase1/junit-result.xml");
    }
}
