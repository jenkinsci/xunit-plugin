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
public class CppUnitTest extends AbstractTest {

    @Parameters(name = "testcase{1}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "success and failure", 1 }, //
                                              { "zero failure", 2 }, //
                                              { "zero failure and success", 3 }, //
                                              { "zero success", 4 }, //
                                              { "testcase5", 5 }, //
                                              { "testcase6", 6 }, //
                                              { "allow custom project properties", 7 } //
        });
    }

    public CppUnitTest(String testName, int testNumber) {
        super(CppUnit.class, resolveInput("cppunit", testNumber), resolveOutput("cppunit", testNumber));
    }
}