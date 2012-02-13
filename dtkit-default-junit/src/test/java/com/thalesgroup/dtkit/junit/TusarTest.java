package com.thalesgroup.dtkit.junit;

import org.junit.Test;

public class TusarTest extends AbstractTest {


    @Test
    public void testTransformation() throws Exception {
        convertAndValidate(Tusar.class, "tusar/testcase1/input.xml", "tusar/testcase1/junit-result.xml");
    }

}
