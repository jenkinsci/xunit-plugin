/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Falco Nikolas
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
 * Set the build status based on the count of passed test.
 *
 * @author Falco Nikolas
 */
@SuppressWarnings("serial")
public class PassedThreshold extends XUnitThreshold {

    @DataBoundConstructor
    public PassedThreshold() {
    }

    @Override
    public Result getResultThresholdNumber(XUnitLog log, Run<?, ?> build, TestResultSummary testResult, TestResultSummary previousTestResultAction) {

        int passedCount = testResult.getPassCount();

        int previousPassedCount = 0;
        if (previousTestResultAction != null) {
            previousPassedCount = previousTestResultAction.getPassCount();
        }
        int newPassedCount = passedCount - previousPassedCount;


        return getResultThresholdNumber(log, passedCount, newPassedCount);
    }

    @Override
    public Result getResultThresholdPercent(XUnitLog log, Run<?, ?> build, TestResultSummary testResult, TestResultSummary previousTestResultAction) {

        double count = testResult.getTotalCount();

        double passedCount = testResult.getPassCount();
        double percentPassed = (passedCount / count) * 100;

        double previousPassedCount = 0;
        if (previousTestResultAction != null) {
            previousPassedCount = previousTestResultAction.getPassCount();
        }
        double newPassedCount = passedCount - previousPassedCount;
        double percentNewPassed = (newPassedCount / count) * 100;

        return getResultThresholdPercent(log, percentPassed, percentNewPassed);
    }

    @Override
    public boolean isValidThreshold(double threshold, double value) {
        return value >= threshold;
    }

}
