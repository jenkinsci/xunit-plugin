/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Thales Corporate Services SAS Gregory Boissinot
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

/**
 * @author Gregory Boissinot
 */
class QTestLibTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("testcase1", QTestLib.class, "qtestlib", 1),
                Arguments.of("testcase2", QTestLib.class, "qtestlib", 2),
                Arguments.of("JENKINS-35660 verify_that_duration_converted_correctly", QTestLib.class, "qtestlib", 3),
                Arguments.of("boh", QTestLib.class, "qtestlib", 4),
                Arguments.of("JENKINS-51604 support for multiple Incident", QTestLib.class, "qtestlib", 5));
    }

}