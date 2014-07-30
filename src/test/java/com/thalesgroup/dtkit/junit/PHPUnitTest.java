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

public class PHPUnitTest extends AbstractTest {

    @Test
    public void testcase1() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase1/testresult.xml", "phpunit/testcase1/junit-result.xml");
    }

    @Test
    public void testcase2() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase2/testresult.xml", "phpunit/testcase2/junit-result.xml");
    }

    @Test
    public void testcase3() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase3/testresult.xml", "phpunit/testcase3/junit-result.xml");
    }

    @Test
    public void testcase4() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase4/testresult.xml", "phpunit/testcase4/junit-result.xml");
    }

    @Test
    public void testcase5() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase5/testresult.xml", "phpunit/testcase5/junit-result.xml");
    }

    @Test
    public void testcase6() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase6/testresult.xml", "phpunit/testcase6/junit-result.xml");
    }

    @Test
    public void testcase7() throws Exception {
        convertAndValidate(PHPUnit.class, "phpunit/testcase7/testresult.xml", "phpunit/testcase7/junit-result.xml");
    }
}
