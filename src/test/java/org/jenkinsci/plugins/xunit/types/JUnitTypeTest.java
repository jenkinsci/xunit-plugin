/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.xunit.types;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Gregory Boissinot
 */
@RunWith(Parameterized.class)
public class JUnitTypeTest extends AbstractTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "test case 1", 1 }, //
                                              { "test case 2", 2 }, //
                                              { "test case 3", 3 }, //
                                              { "test case 4", 4 }, //
                                              { "support rerun of maven-surefire-plugin version", 5 }, //
                                              { "support maven surefure report schema version 3", 6 }
        });
    }

    public JUnitTypeTest(String testName, int testNumber) {
        super(JUnitInputMetric.class, resolveInput("junit", testNumber), resolveInput("junit", testNumber));
    }

}