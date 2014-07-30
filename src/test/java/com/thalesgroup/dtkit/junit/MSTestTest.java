/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.dtkit.junit;

import org.junit.Test;


public class MSTestTest extends AbstractTest {
    @Test
    public void testTransformationCase1() throws Exception {

        convertAndValidate(MSTest.class, "mstest/mstest_2_tests_1_class.trx", "mstest/junit_mstest_2_tests_1_class.xml");
    }

    @Test
    public void testTransformationCase2() throws Exception {
        convertAndValidate(MSTest.class, "mstest/mstest_2_tests_from_different_assemblies.trx", "mstest/junit_mstest_2_tests_from_different_assemblies.xml");
    }

    @Test
    public void testTransformationCase3() throws Exception {
        convertAndValidate(MSTest.class, "mstest/mstest_4_tests_2_classes.trx", "mstest/junit_mstest_4_tests_2_classes.xml");
    }

    @Test
    public void testTransformationCase4() throws Exception {
        convertAndValidate(MSTest.class, "mstest/mstest_vs_2010.trx", "mstest/junit_mstest_vs_2010.xml");
    }

    @Test
    public void testTransformationCase5() throws Exception {
        convertAndValidate(MSTest.class, "mstest/mstest_more_than_one_minute_test.trx", "mstest/junit_mstest_more_than-one_minute_test.xml");
    }
}

