/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Gregory Boissinot, Nikolas Falco
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

class NUnitTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("simple transformation", NUnit.class, "nunit", 1),
                Arguments.of("failures transformation", NUnit.class, "nunit", 2),
                Arguments.of("MultiNamespace transformation", NUnit.class, "nunit", 3),
                Arguments.of("test transformed of ignored", NUnit.class, "nunit", 4),
                Arguments.of("JENKINS-1077", NUnit.class, "nunit", 5),
                Arguments.of("JENKINS-8492", NUnit.class, "nunit", 6),
                Arguments.of("JENKINS-10911 skipped are ignored when failure is present before", NUnit.class, "nunit", 7),
                Arguments.of("Sample provided by http://nunit.org/files/testresult_25.txt", NUnit.class, "nunit", 8),
                Arguments.of("Sample provided by the 2.4.8 distribution", NUnit.class, "nunit", 9),
                Arguments.of("JENKINS-51481 report produced by DUnit to NUnit logger", NUnit.class, "nunit", 10),
                Arguments.of("JENKINS-51556 works", NUnit.class, "nunit", 11),
                Arguments.of("JENKINS-51556 cause JEP-200 issue", NUnit.class, "nunit", 12),
                Arguments.of("JENKINS-51561 NUnit 3.x produce report in NUnit 2.x format", NUnit.class, "nunit", 13),
                Arguments.of("JENKINS-51767", NUnit.class, "nunit", 14),
                Arguments.of("JENKINS-52107", NUnit.class, "nunit", 15),
                Arguments.of("JENKINS-53034", NUnit.class, "nunit", 16),
                Arguments.of("JENKINS-53186 result of kind Error is reported as Success", NUnit.class, "nunit", 17)
        );
    }

}