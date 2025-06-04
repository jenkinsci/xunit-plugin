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

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

class CppTestTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("testcase1", CppTest.class, "cpptest", 1),
                Arguments.of("mix execution of pass and fail tests", CppTest.class, "cpptest", 2),
                Arguments.of("testcase3", CppTest.class, "cpptest", 3),
                Arguments.of("7.x all test succeed", CppTest.class, "cpptest", 4),
                Arguments.of("7.x all test succeed, no CLI options", CppTest.class, "cpptest", 5),
                Arguments.of("7.x one test fails with 2 assertions", CppTest.class, "cpptest", 6),
                Arguments.of("7.x one test fails with 1 assertion", CppTest.class, "cpptest", 7)
        );
    }

}