package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class QTestlibTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate(QTestLibInputMetric.class, "qtestlib/testcase1/input.xml", "qtestlib/testcase1/result.xml");
    }
}
