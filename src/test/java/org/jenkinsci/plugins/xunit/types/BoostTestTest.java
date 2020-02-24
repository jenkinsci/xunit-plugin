/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Gregory Boissinot, Falco Nikolas
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
public class BoostTestTest extends AbstractTest {

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "testcase1", 1 }, //
                                              { "testcase2", 2 }, //
                                              { "testcase3", 3 }, //
                                              { "testcase4", 4 }, //
                                              { "testcase5", 5 }, //
                                              { "testcase6", 6 }, //
                                              { "testcase7", 7 }, //
                                              { "testcase8", 8 }, //
                                              { "testcase9", 9 }, //
                                              { "testcase10", 10 }, //
                                              { "testcase11", 11 }, //
                                              { "testcase12", 12 }, //
                                              { "testcase13", 13 }, //
                                              { "testcase14", 14 }, //
                                              { "testcase15", 15 }, //
                                              { "testcase16", 16 }, //
                                              { "testcase17", 17 }, //
                                              { "JENKINS-42031", 18 }, //
                                              { "testcase19", 19 }, //
                                              { "autotest", 20 }, //
                                              { "autotest-multiple", 21 }, //
                                              { "skipped", 22 } //
        });
    }

    public BoostTestTest(String testName, int testNumber) {
        super(BoostTest.class, resolveInput("boosttest", testNumber), resolveOutput("boosttest", testNumber));
    }

}