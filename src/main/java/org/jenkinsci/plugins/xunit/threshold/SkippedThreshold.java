/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
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

package org.jenkinsci.plugins.xunit.threshold;

import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultSummary;

/**
 * @author Gregory Boissinot
 */
@SuppressWarnings("serial")
public class SkippedThreshold extends XUnitThreshold {

    @DataBoundConstructor
    public SkippedThreshold() {
    }

    @Override
    public Result getResultThresholdNumber(XUnitLog log, Run<?, ?> build, TestResultSummary testResult, TestResultSummary previousTestResultAction) {

        int skipCount = testResult.getSkipCount();

        int previousSkipCount = 0;
        if (previousTestResultAction != null) {
            previousSkipCount = previousTestResultAction.getSkipCount();
        }
        int newSkipCount = skipCount - previousSkipCount;

        return getResultThresholdNumber(log, skipCount, newSkipCount);
    }

    @Override
    public Result getResultThresholdPercent(XUnitLog log, Run<?, ?> build, TestResultSummary testResult, TestResultSummary previousTestResultAction) {

        int count = testResult.getTotalCount();
        int skippedCount = testResult.getSkipCount();
        int percentSkipped = (count == 0) ? 0 : (skippedCount * 100 / count);

        int previousSkippedCount = 0;
        if (previousTestResultAction != null) {
            previousSkippedCount = previousTestResultAction.getSkipCount();
        }
        int newSkippedCount = skippedCount - previousSkippedCount;
        int percentNewSkipped = (count == 0) ? 0 : (newSkippedCount  * 100 / count);

        return getResultThresholdPercent(log, percentSkipped, percentNewSkipped);
    }

    @Override
    public boolean isValidThreshold(double threshold, double value) {
        return value <= threshold;
    }

}
