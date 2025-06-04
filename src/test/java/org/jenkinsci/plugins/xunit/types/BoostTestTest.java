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

class BoostTestTest extends AbstractTest {

    protected Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("testcase1", BoostTest.class, "boosttest", 1),
                Arguments.of("testcase2", BoostTest.class, "boosttest", 2),
                Arguments.of("testcase3", BoostTest.class, "boosttest", 3),
                Arguments.of("testcase4", BoostTest.class, "boosttest", 4),
                Arguments.of("testcase5", BoostTest.class, "boosttest", 5),
                Arguments.of("testcase6", BoostTest.class, "boosttest", 6),
                Arguments.of("testcase7", BoostTest.class, "boosttest", 7),
                Arguments.of("testcase8", BoostTest.class, "boosttest", 8),
                Arguments.of("testcase9", BoostTest.class, "boosttest", 9),
                Arguments.of("testcase10", BoostTest.class, "boosttest", 10),
                Arguments.of("testcase11", BoostTest.class, "boosttest", 11),
                Arguments.of("testcase12", BoostTest.class, "boosttest", 12),
                Arguments.of("testcase13", BoostTest.class, "boosttest", 13),
                Arguments.of("testcase14", BoostTest.class, "boosttest", 14),
                Arguments.of("testcase15", BoostTest.class, "boosttest", 15),
                Arguments.of("testcase16", BoostTest.class, "boosttest", 16),
                Arguments.of("testcase17", BoostTest.class, "boosttest", 17),
                Arguments.of("JENKINS-42031", BoostTest.class, "boosttest", 18),
                Arguments.of("testcase19", BoostTest.class, "boosttest", 19),
                Arguments.of("autotest", BoostTest.class, "boosttest", 20),
                Arguments.of("autotest-multiple", BoostTest.class, "boosttest", 21),
                Arguments.of("skipped", BoostTest.class, "boosttest", 22),
                Arguments.of("exception-context", BoostTest.class, "boosttest", 23),
                Arguments.of("wrong_classname", BoostTest.class, "boosttest", 24),
                Arguments.of("parse_error", BoostTest.class, "boosttest", 25)
        );
    }

}
