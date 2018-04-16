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

import java.io.Serializable;

import org.jenkinsci.plugins.xunit.service.XUnitLog;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
public abstract class XUnitThreshold implements ExtensionPoint, Serializable, Describable<XUnitThreshold> {

    private String unstableThreshold;

    private String unstableNewThreshold;

    private String failureThreshold;

    private String failureNewThreshold;

    protected XUnitThreshold() {
    }

    public XUnitThreshold(String unstableThreshold, String unstableNewThreshold, String failureThreshold, String failureNewThreshold) {
        this.unstableThreshold = unstableThreshold;
        this.unstableNewThreshold = unstableNewThreshold;
        this.failureThreshold = failureThreshold;
        this.failureNewThreshold = failureNewThreshold;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<XUnitThreshold> getDescriptor() {
        return Jenkins.getActiveInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> all() {
        return Jenkins.getActiveInstance().<XUnitThreshold, XUnitThresholdDescriptor<?>>getDescriptorList(XUnitThreshold.class);
    }

    public String getUnstableThreshold() {
        return unstableThreshold;
    }

    public String getUnstableNewThreshold() {
        return unstableNewThreshold;
    }

    public String getFailureThreshold() {
        return failureThreshold;
    }

    public String getFailureNewThreshold() {
        return failureNewThreshold;
    }

    public abstract Result getResultThresholdNumber(XUnitLog log,
                                                    Run<?, ?> build,
                                                    TestResultAction testResultAction,
                                                    TestResultAction previousTestResultAction);

    public abstract Result getResultThresholdPercent(XUnitLog log,
                                                     Run<?, ?> build,
                                                     TestResultAction testResultAction,
                                                     TestResultAction previousTestResultAction);

    public Result getResultThresholdNumber(XUnitLog log,
                                           int testCount,
                                           int newTestCount) {

        String thresholdErrorMessage = "The %s number of tests for this category exceeds the specified '%s' threshold value.";
        if (isValid(getFailureThreshold())
                && (convertToInteger(getFailureThreshold()) < testCount)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "total", "failure"));
            return Result.FAILURE;
        }

        if (isValid(getFailureNewThreshold())
                && (convertToInteger(getFailureNewThreshold()) < newTestCount)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "new", "new failure"));
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold())
                && (convertToInteger(getUnstableThreshold()) < testCount)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "total", "unstable"));
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold())
                && (convertToInteger(getUnstableNewThreshold()) < newTestCount)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "new", "new unstable"));
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;

    }

    public Result getResultThresholdPercent(XUnitLog log,
                                            double testPercent,
                                            double newTestPercent) {

        String thresholdErrorMessage = "The percent %s tests for this category exceeds the specified '%s' threshold percent value.";
        if (isValid(getFailureThreshold())
                && (convertToIntegerPercent(getFailureThreshold()) < testPercent)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "of the total number of", "failure"));
            return Result.FAILURE;
        }

        if (isValid(getFailureNewThreshold())
                && (convertToIntegerPercent(getFailureNewThreshold()) < newTestPercent)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "of the new number of", "new failure"));
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold())
                && (convertToIntegerPercent(getUnstableThreshold()) < testPercent)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "of", "unstable"));
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold())
                && (convertToIntegerPercent(getUnstableNewThreshold()) < newTestPercent)) {
            log.infoConsoleLogger(String.format(thresholdErrorMessage, "of the new number of", "new unstable"));
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
    }

    private int convertToInteger(String threshold) {
        return Integer.parseInt(threshold);
    }

    private int convertToIntegerPercent(String threshold) {
        String thresholdRemoved = threshold.replace("%", "");
        return Integer.parseInt(thresholdRemoved);
    }

    private boolean isValid(String threshold) {
        if (threshold == null) {
            return false;
        }

        if (threshold.trim().length() == 0) {
            return false;
        }

        try {
            Integer.parseInt(threshold);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
}
