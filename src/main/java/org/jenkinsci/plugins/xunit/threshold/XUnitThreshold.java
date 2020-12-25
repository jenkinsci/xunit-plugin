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
import org.kohsuke.stapler.DataBoundSetter;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultSummary;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
@SuppressWarnings("serial")
public abstract class XUnitThreshold implements ExtensionPoint, Serializable, Describable<XUnitThreshold> {

    private String unstableThreshold;
    private String unstableNewThreshold;
    private String failureThreshold;
    private String failureNewThreshold;

    protected XUnitThreshold() {
    }

    public XUnitThreshold(String unstableThreshold, String unstableNewThreshold, String failureThreshold, String failureNewThreshold) {
        this.setUnstableThreshold(unstableThreshold);
        this.setUnstableNewThreshold(unstableNewThreshold);
        this.setFailureThreshold(failureThreshold);
        this.setFailureNewThreshold(failureNewThreshold);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<XUnitThreshold> getDescriptor() {
        return Jenkins.get().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> all() {
        return Jenkins.get().<XUnitThreshold, XUnitThresholdDescriptor<?>> getDescriptorList(XUnitThreshold.class);
    }

    public String getUnstableThreshold() {
        return unstableThreshold;
    }

    @DataBoundSetter
    public void setUnstableThreshold(String unstableThreshold) {
        this.unstableThreshold = Util.fixEmptyAndTrim(unstableThreshold);
    }

    public String getUnstableNewThreshold() {
        return unstableNewThreshold;
    }

    @DataBoundSetter
    public void setUnstableNewThreshold(String unstableNewThreshold) {
        this.unstableNewThreshold = Util.fixEmptyAndTrim(unstableNewThreshold);
    }

    public String getFailureThreshold() {
        return failureThreshold;
    }

    @DataBoundSetter
    public void setFailureThreshold(String failureThreshold) {
        this.failureThreshold = Util.fixEmptyAndTrim(failureThreshold);
    }

    public String getFailureNewThreshold() {
        return failureNewThreshold;
    }

    @DataBoundSetter
    public void setFailureNewThreshold(String failureNewThreshold) {
        this.failureNewThreshold = Util.fixEmptyAndTrim(failureNewThreshold);
    }

    public abstract Result getResultThresholdNumber(XUnitLog log,
                                                    Run<?, ?> build,
                                                    TestResultSummary testResult,
                                                    TestResultSummary previousResult);

    public abstract Result getResultThresholdPercent(XUnitLog log,
                                                     Run<?, ?> build,
                                                     TestResultSummary testResult,
                                                     TestResultSummary previousTestResultAction);

    public abstract boolean isValidThreshold(double threshold, double value);

    public Result getResultThresholdNumber(XUnitLog log, int testCount, int newTestCount) {

        String thresholdName = this.getDescriptor().getDisplayName();

        if (isValid(getFailureThreshold()) && !isValidThreshold(asInteger(getFailureThreshold()), testCount)) {
            log.info(Messages.XUnitThreshold_threshold_message(thresholdName, Messages.XUnitThreshold_failureThreshold_name()));
            return Result.FAILURE;
        }

        if (isValid(getFailureNewThreshold()) && !isValidThreshold(asInteger(getFailureNewThreshold()), newTestCount)) {
            log.info(Messages.XUnitThreshold_newThreshold_message(thresholdName, Messages.XUnitThreshold_failureNewThreshold_name()));
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold()) && !isValidThreshold(asInteger(getUnstableThreshold()), testCount)) {
            log.info(Messages.XUnitThreshold_threshold_message(thresholdName, Messages.XUnitThreshold_unstableThreshold_name()));
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold()) && !isValidThreshold(asInteger(getUnstableNewThreshold()), newTestCount)) {
            log.info(Messages.XUnitThreshold_newThreshold_message(thresholdName, Messages.XUnitThreshold_unstableNewThreshold_name()));
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
    }

    public Result getResultThresholdPercent(XUnitLog log, double testPercent, double newTestPercent) {

        String thresholdName = this.getDescriptor().getDisplayName();

        if (isValid(getFailureThreshold()) && !isValidThreshold(asInteger(getFailureThreshold()), testPercent)) {
            log.info(Messages.XUnitThreshold_threshold_message(thresholdName, Messages.XUnitThreshold_failureThreshold_name()));
            return Result.FAILURE;
        }

        if (isValid(getFailureNewThreshold()) && !isValidThreshold(asInteger(getFailureNewThreshold()), newTestPercent)) {
            log.info(Messages.XUnitThreshold_newThreshold_message(thresholdName, Messages.XUnitThreshold_failureNewThreshold_name()));
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold()) && !isValidThreshold(asInteger(getUnstableThreshold()), testPercent)) {
            log.info(Messages.XUnitThreshold_threshold_message(thresholdName, Messages.XUnitThreshold_unstableThreshold_name()));
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold()) && !isValidThreshold(asInteger(getUnstableNewThreshold()), newTestPercent)) {
            log.info(Messages.XUnitThreshold_newThreshold_message(thresholdName, Messages.XUnitThreshold_unstableNewThreshold_name()));
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
    }

    private int asInteger(String threshold) {
        return Integer.parseInt(threshold);
    }

    private boolean isValid(String threshold) {
        if (threshold == null) {
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