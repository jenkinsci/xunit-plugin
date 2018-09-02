/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Schneider Electric, Nikolas Falco
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
public class CUnitTest extends AbstractTest{

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "Empty CUNIT_RESULT_LISTING", 1 }, //
                                              { "1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / 1 CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS", 2 }, //
                                              { "1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / 1 CUNIT_TEST_RECORD / CUNIT_RUN_TEST_FAILURE", 3 }, //
                                              { "1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / * CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS | CUNIT_RUN_TEST_FAILURE", 4 }, //
                                              { "* CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / * CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS | CUNIT_RUN_TEST_FAILURE", 5 } //
        });
    }

    public CUnitTest(String testName, int testNumber) {
        super(CUnit.class, resolveInput("cunit", testNumber), resolveOutput("cunit", testNumber));
    }
}