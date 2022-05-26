/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022, JoC0de
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
package org.jenkinsci.plugins.xunit;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultSummary;

public class XUnitProcessorResult {
    private final TestResultSummary testResultSummary;
    private final TestResult testResult;

    public XUnitProcessorResult(@NonNull final TestResultSummary testResultSummary, @NonNull final TestResult testResult) {
        this.testResultSummary = testResultSummary;
        this.testResult = testResult;
    }

    @NonNull
    public TestResultSummary getTestResultSummary() {
        return testResultSummary;
    }

    @NonNull
    public TestResult getTestResult() {
        return testResult;
    }
}
