/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot, Falco Nikolas
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
public class PHPUnitTest extends AbstractTest {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "testcase1", 1 }, //
                                              { "testcase2", 2 }, //
                                              { "testcase3", 3 }, //
                                              { "testcase4", 4 }, //
                                              { "testcase5", 5 }, //
                                              { "testcase6", 6 }, //
                                              { "testcase7", 7 }, //
                                              { "JENKINS-42715 skipped test using PHPUnit 5.4", 8 }, //
                                              { "JENKINS-42715 skipped test using PHPUnit 6+", 9 }, //
                                              { "JENKINS-27494 feature attribute", 10 }, //
                                              { "PHPUnit 4.8.2 warnings", 11 }, //
        });
    }

    public PHPUnitTest(String testName, int testNumber) {
        super(PHPUnit.class, resolveInput("phpunit", testNumber), resolveOutput("phpunit", testNumber));
    }

}