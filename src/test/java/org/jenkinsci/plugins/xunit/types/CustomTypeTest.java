package org.jenkinsci.plugins.xunit.types;

import org.junit.Test;

/**
 * @author Gregory Boissinot
 */
public class CustomTypeTest extends AbstractTest {

    @Test
    public void testTestCase1() throws Exception {
        convertAndValidate("customTool/testcase1/input.xml", "customTool/testcase1/input.xsl", "customTool/testcase1/result.xml");
    }

}
