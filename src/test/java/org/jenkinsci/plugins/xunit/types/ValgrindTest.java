package org.jenkinsci.plugins.xunit.types;

import org.junit.Ignore;
import org.junit.Test;

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
