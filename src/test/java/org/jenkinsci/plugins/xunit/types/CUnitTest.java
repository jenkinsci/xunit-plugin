/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016, Schneider Electric
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

import org.junit.Test;

public class CUnitTest extends AbstractTest{

    // Empty CUNIT_RESULT_LISTING
    @Test
    public void testcase1() throws Exception {
        convertAndValidate(CUnit.class, "cunit/testcase1/testresult.xml", "cunit/testcase1/junit-result.xml");
    }

    // 1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / 1 CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS
    @Test
    public void testcase2() throws Exception {
        convertAndValidate(CUnit.class, "cunit/testcase2/testresult.xml", "cunit/testcase2/junit-result.xml");
    }

    // 1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / 1 CUNIT_TEST_RECORD / CUNIT_RUN_TEST_FAILURE
    @Test
    public void testcase3() throws Exception {
        convertAndValidate(CUnit.class, "cunit/testcase3/testresult.xml", "cunit/testcase3/junit-result.xml");
    }

    // 1 CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / * CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS | CUNIT_RUN_TEST_FAILURE
    @Test
    public void testcase4() throws Exception {
        convertAndValidate(CUnit.class, "cunit/testcase4/testresult.xml", "cunit/testcase4/junit-result.xml");
    }

    // * CUNIT_RUN_SUITE / CUNIT_RUN_SUITE_SUCCESS / * CUNIT_TEST_RECORD / CUNIT_RUN_TEST_SUCCESS | CUNIT_RUN_TEST_FAILURE
    @Test
    public void testcase5() throws Exception {
        convertAndValidate(CUnit.class, "cunit/testcase5/testresult.xml", "cunit/testcase5/junit-result.xml");
    }
}
