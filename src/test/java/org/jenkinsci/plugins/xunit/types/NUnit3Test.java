/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017, Alex Schwantes, Falco Nikolas
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
public class NUnit3Test extends AbstractTest {

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "simple transformation", 1 }, //
                                              { "test transformed of ignored", 2 }, //
                                              { "categories", 3 }, //
                                              { "support SetupFixture", 4 }, //
                                              { "not handled ParameterizedMethod tesuite produce text outside a testsuite element", 5 } //
        });
    }

    public NUnit3Test(String testName, int testNumber) {
        super(NUnit3.class, resolveInput("nunit3", testNumber), resolveOutput("nunit3", testNumber));
    }

}