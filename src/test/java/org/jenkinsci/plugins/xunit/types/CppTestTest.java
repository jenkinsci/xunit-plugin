/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017, Gregory Boissinot, Falco Nikolas
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
public class CppTestTest extends AbstractTest {

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "testcase1", 1 }, //
                                              { "mix execution of pass and fail tests", 2 }, //
                                              { "testcase3", 3 }, //
                                              { "7.x all test successed", 4 }, //
                                              { "7.x all test succeed, no CLI options", 5 }, //
                                              { "7.x one test fails with 2 assertions", 6 }, //
                                              { "7.x one test fails with 1 assertion", 7 }, //
        });
    }

    public CppTestTest(String testName, int testNumber) {
        super(CppTest.class, resolveInput("cpptest", testNumber), resolveOutput("cpptest", testNumber));
    }

}