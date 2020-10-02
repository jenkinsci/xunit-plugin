/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, CloudBees, inc.
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
package org.jenkinsci.plugins.xunit.pipeline;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.xunit.Messages;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;
import org.jenkinsci.plugins.xunit.XUnitUtil;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.collect.ImmutableSet;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestDataPublisher;

public class XUnitResultsStep extends Step {
    private List<TestType> tools;
    private List<XUnitThreshold> thresholds;
    private int thresholdMode = 1;
    private long testTimeMargin = XUnitDefaultValues.TEST_REPORT_TIME_MARGING;
    private long sleepTime = XUnitDefaultValues.PROCESSING_SLEEP_TIME;
    private boolean reduceLog = XUnitDefaultValues.JUNIT_FILE_REDUCE_LOG;
    private boolean followSymlink = XUnitDefaultValues.FOLLOW_SYMLINK;
    private Collection<TestDataPublisher> testDataPublishers;

    @DataBoundConstructor
    public XUnitResultsStep(@CheckForNull List<TestType> tools) {
        this.tools = (tools != null) ? tools : Collections.emptyList();
    }

    @Nonnull
    public List<TestType> getTools() {
        return tools;
    }

    @Nonnull
    public List<XUnitThreshold> getThresholds() {
        return thresholds != null ? thresholds : Collections.emptyList();
    }

    @DataBoundSetter
    public void setThresholds(@Nonnull List<XUnitThreshold> thresholds) {
        this.thresholds = thresholds;
    }

    @DataBoundSetter
    public void setThresholdMode(int thresholdMode) {
        this.thresholdMode = thresholdMode;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    @DataBoundSetter
    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    @DataBoundSetter
    public void setTestTimeMargin(String testTimeMargin) {
        this.testTimeMargin = XUnitUtil.parsePositiveLong(testTimeMargin, XUnitDefaultValues.TEST_REPORT_TIME_MARGING);
    }

    public String getTestTimeMargin() {
        return String.valueOf(testTimeMargin);
    }

    public long getTestTimeMarginAsLong() {
        return testTimeMargin;
    }

    @Nonnull
    public Collection<TestDataPublisher> getTestDataPublishers() {
        return testDataPublishers != null ? testDataPublishers : Collections.emptyList();
    }

    @DataBoundSetter
    public void setTestDataPublishers(@Nonnull Collection<TestDataPublisher> testDataPublishers) {
        this.testDataPublishers = testDataPublishers;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new XUnitResultsStepExecution(this, context);
    }

    public boolean isReduceLog() {
        return reduceLog;
    }

    @DataBoundSetter
    public void setReduceLog(boolean reduceLog) {
        this.reduceLog = reduceLog;
    }

    public boolean isFollowSymlink() {
        return followSymlink;
    }

    @DataBoundSetter
    public void setFollowSymlink(boolean followSymlink) {
        this.followSymlink = followSymlink;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "xunit";
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.xUnit_PublisherName();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, FlowNode.class, TaskListener.class);
        }

        public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListXUnitTypeDescriptors() {
            return TestTypeDescriptor.all();
        }

        public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
            return XUnitThresholdDescriptor.all();
        }

        public DescriptorExtensionList<TestDataPublisher, Descriptor<TestDataPublisher>> getListTestDataPublisherDescriptors() {
            return hudson.tasks.junit.TestDataPublisher.all();
        }
    }
}
