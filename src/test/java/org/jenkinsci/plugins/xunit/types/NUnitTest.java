/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Gregory Boissinot, Nikolas Falco
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

@RunWith(Parameterized.class)
public class NUnitTest extends AbstractTest {

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "simple transformation", 1 }, //
                                              { "failures transformation", 2 }, //
                                              { "MultiNamespace transformation", 3 }, //
                                              { "test transformed of ignored", 4 }, //
                                              { "JENKINS-1077", 5 }, //
                                              { "JENKINS-8492", 6 }, //
                                              { "JENKINS-10911 skipped are ignored when failure is present before", 7 }, //
                                              { "Sample provided by http://nunit.org/files/testresult_25.txt", 8 }, //
                                              { "Sample provided by the 2.4.8 distribution", 9 }, //
                                              { "JENKINS-51481 report produced by DUnit to NUnit logger", 10 }, //
                                              { "JENKINS-51556 works", 11 }, //
                                              { "JENKINS-51556 cause JEP-200 issue", 12 }, //
                                              { "JENKINS-51561 NUnit 3.x produce report in NUnit 2.x format", 13 }, //
                                              { "JENKINS-51767", 14 }, //
                                              { "JENKINS-52107", 15 }, //
                                              { "JENKINS-53034", 16 }, //
                                              { "JENKINS-53186 result of kind Error is reported as Success", 17 } //
        });
    }

    public NUnitTest(String testName, int testNumber) {
        super(NUnit.class, resolveInput("nunit", testNumber), resolveOutput("nunit", testNumber));
    }

}