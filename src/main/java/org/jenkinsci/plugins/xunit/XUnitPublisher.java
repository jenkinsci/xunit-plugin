/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Gregory Boissinot, Nikolas Falco, Arnaud, Andrew Bayer
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

import static org.jenkinsci.plugins.xunit.XUnitDefaultValues.FOLLOW_SYMLINK;
import static org.jenkinsci.plugins.xunit.XUnitDefaultValues.JUNIT_FILE_REDUCE_LOG;
import static org.jenkinsci.plugins.xunit.XUnitDefaultValues.PROCESSING_SLEEP_TIME;
import static org.jenkinsci.plugins.xunit.XUnitDefaultValues.TEST_REPORT_TIME_MARGING;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.TransformerException;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import hudson.AbortException;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResultSummary;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

/**
 * Class that converting custom reports to Junit reports and records them.
 *
 * @author Gregory Boissinot
 */
public class XUnitPublisher extends Recorder implements SimpleBuildStep {

    @XStreamAlias("types")
    private TestType[] tools;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;
    private Collection<TestDataPublisher> testDataPublishers;

    @DataBoundConstructor
    public XUnitPublisher(@CheckForNull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds, int thresholdMode, @CheckForNull String testTimeMargin) {
        this.tools = (tools != null ? Arrays.copyOf(tools, tools.length) : new TestType[0]);
        this.thresholds = (thresholds != null ? Arrays.copyOf(thresholds, thresholds.length) : new XUnitThreshold[0]);
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitUtil.parsePositiveLong(testTimeMargin, TEST_REPORT_TIME_MARGING);
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin, JUNIT_FILE_REDUCE_LOG, PROCESSING_SLEEP_TIME, FOLLOW_SYMLINK);
        this.testDataPublishers = Collections.<TestDataPublisher> emptySet();
    }

    @DataBoundSetter
    public void setReduceLog(boolean reduceLog) {
        extraConfiguration = ExtraConfiguration.withConfiguration(extraConfiguration).reduceLog(reduceLog).build();
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public boolean getReduceLog() {
        return extraConfiguration.isReduceLog();
    }

    @DataBoundSetter
    public void setSleepTime(long sleepTime) {
        extraConfiguration = ExtraConfiguration.withConfiguration(extraConfiguration).sleepTime(sleepTime > 0 ? sleepTime : 0).build();
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public boolean getSleepTime() {
        return extraConfiguration.isReduceLog();
    }

    @DataBoundSetter
    public void setFollowSymlink(boolean followSymlink) {
        extraConfiguration = ExtraConfiguration.withConfiguration(extraConfiguration).followSymlink(followSymlink).build();
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public boolean getFollowSymlink() {
        return extraConfiguration.isFollowSymlink();
    }

    /*
     * Needed to support Snippet Generator and Workflow properly.
     */
    @Nonnull
    public TestType[] getTools() {
        return tools;
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    @Nonnull
    public String getTestTimeMargin() {
        return String.valueOf(getExtraConfiguration().getTestTimeMargin());
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    @Nonnull
    public XUnitThreshold[] getThresholds() {
        return thresholds;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    @Nonnull
    public ExtraConfiguration getExtraConfiguration() {
        if (extraConfiguration == null) {
            extraConfiguration = new ExtraConfiguration(TEST_REPORT_TIME_MARGING, JUNIT_FILE_REDUCE_LOG, PROCESSING_SLEEP_TIME, FOLLOW_SYMLINK);
        }
        return extraConfiguration;
    }

    @Nonnull
    public Collection<TestDataPublisher> getTestDataPublishers() {
        return testDataPublishers != null ? testDataPublishers : Collections.<TestDataPublisher> emptyList();
    }

    /**
     * Configures the {@link TestDataPublisher}s for this custom reports
     * publisher, to process the recorded data.
     *
     * @param testDataPublishers
     *            the test data publishers to set for this custom reports
     *            publisher
     */
    @DataBoundSetter
    public void setTestDataPublishers(@CheckForNull Collection<TestDataPublisher> testDataPublishers) {
        this.testDataPublishers = testDataPublishers != null
                ? new LinkedList<>(testDataPublishers)
                : Collections.<TestDataPublisher> emptyList();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        JUnitResultArchiver jUnitResultArchiver = project.getPublishersList().get(JUnitResultArchiver.class);
        if (jUnitResultArchiver == null) {
            return new TestResultProjectAction(project);
        }
        return null;
    }

    @Override
    public void perform(final Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {
        try {
            XUnitProcessor xUnitProcessor = new XUnitProcessor(getTools(), getThresholds(), getThresholdMode(), getExtraConfiguration());
            TestResultSummary testResult = xUnitProcessor.process(build, workspace, listener, launcher, getTestDataPublishers(), null);

            XUnitLog logger = new XUnitLog(listener);
            if (testResult.getPassCount() == 0 && testResult.getFailCount() == 0) {
                logger.warn(Messages.xUnitProcessor_emptyReport());
            }

            Result result = xUnitProcessor.getBuildStatus(testResult, build);
            logger.info("Setting the build status to " + result);
            build.setResult(result);

        } catch(AbortException e) {
            build.setResult(Result.FAILURE);
            throw e;
        } catch(TransformerException e) {
            build.setResult(Result.FAILURE);
            // extract original message
            throw new AbortException(e.getMessage());
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class XUnitDescriptorPublisher extends BuildStepDescriptor<Publisher> {

        public XUnitDescriptorPublisher() {
            super(XUnitPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.xUnit_PublisherName();
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/xunit/help.html";
        }

        public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListXUnitTypeDescriptors() {
            return TestTypeDescriptor.all();
        }

        public DescriptorExtensionList<TestDataPublisher, Descriptor<TestDataPublisher>> getListTestDataPublisherDescriptors() {
            return hudson.tasks.junit.TestDataPublisher.all();
        }

        public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
            return XUnitThresholdDescriptor.all();
        }

        /**
         * Verify that the given sleepTime is a positive number value.
         *
         * @param sleepTime value
         * @return an validation form for the given sleep time value.
         */
        public FormValidation doCheckSleepTime(@QueryParameter final long sleepTime) {
            if (sleepTime < 0) {
                return FormValidation.error(Messages.xUnitProcessor_checkSleepTime());
            }
            return FormValidation.ok();
        }

    }

}
