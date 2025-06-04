/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot, Falco Nikolas
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

class PHPUnitTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("testcase1", PHPUnit.class, "phpunit", 1),
                Arguments.of("testcase2", PHPUnit.class, "phpunit", 2),
                Arguments.of("testcase3", PHPUnit.class, "phpunit", 3),
                Arguments.of("testcase4", PHPUnit.class, "phpunit", 4),
                Arguments.of("testcase5", PHPUnit.class, "phpunit", 5),
                Arguments.of("testcase6", PHPUnit.class, "phpunit", 6),
                Arguments.of("testcase7", PHPUnit.class, "phpunit", 7),
                Arguments.of("JENKINS-42715 skipped test using PHPUnit 5.4", PHPUnit.class, "phpunit", 8),
                Arguments.of("JENKINS-42715 skipped test using PHPUnit 6+", PHPUnit.class, "phpunit", 9),
                Arguments.of("JENKINS-27494 feature attribute", PHPUnit.class, "phpunit", 10),
                Arguments.of("PHPUnit 4.8.2 warnings", PHPUnit.class, "phpunit", 11)
        );
    }

}