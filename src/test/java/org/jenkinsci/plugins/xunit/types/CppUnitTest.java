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

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class CppUnitTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("success and failure", CppUnit.class, "cppunit", 1),
                Arguments.of("zero failure", CppUnit.class, "cppunit", 2),
                Arguments.of("zero failure and success", CppUnit.class, "cppunit", 3),
                Arguments.of("zero success", CppUnit.class, "cppunit", 4),
                Arguments.of("testcase5", CppUnit.class, "cppunit", 5),
                Arguments.of("testcase6", CppUnit.class, "cppunit", 6),
                Arguments.of("allow custom project properties", CppUnit.class, "cppunit", 7)
        );
    }

}