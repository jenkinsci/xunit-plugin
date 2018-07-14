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

package org.jenkinsci.plugins.xunit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;

import hudson.model.Descriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.TransformerException;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Saveable;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.DescribableList;
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
    /**
     * for compatibility reasons, can be null.
     */
    @SuppressFBWarnings(value="SE_BAD_FIELD", justification="Known non-serializable field")
    private DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers;

    @DataBoundConstructor
    public XUnitPublisher(@CheckForNull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds, int thresholdMode, @CheckForNull String testTimeMargin) {
        this.tools = (tools != null ? Arrays.copyOf(tools, tools.length) : new TestType[0]);
        this.thresholds = (thresholds != null ? Arrays.copyOf(thresholds, thresholds.length) : new XUnitThreshold[0]);
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitUtil.parsePositiveLong(testTimeMargin, XUnitDefaultValues.TEST_REPORT_TIME_MARGING);
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin);
    }

    /*
     * Needed to support Snippet Generator and Workflow properly.
     */
    public TestType[] getTools() {
        return tools;
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public String getTestTimeMargin() {
        return String.valueOf(getExtraConfiguration().getTestTimeMargin());
    }

    public XUnitThreshold[] getThresholds() {
        return thresholds;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    public ExtraConfiguration getExtraConfiguration() {
        if (extraConfiguration == null) {
            extraConfiguration = new ExtraConfiguration(XUnitDefaultValues.TEST_REPORT_TIME_MARGING);
        }
        return extraConfiguration;
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
            xUnitProcessor.process(build, workspace, listener, launcher, testDataPublishers);
        } catch(TransformerException e) {
            // also if we throws AbortException the all published steps are always performed. I prefer hide stacktrace.
            listener.error("The plugin hasn't been performed correctly: " + e.getMessage());
            build.setResult(Result.FAILURE);
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public @Nonnull List<TestDataPublisher> getTestDataPublishers() {
        return testDataPublishers == null ? Collections.<TestDataPublisher>emptyList() : testDataPublishers;
    }

    /**
     * Configures the {@link TestDataPublisher}s for this custom reports publisher, to process the recorded data.
     * @param testDataPublishers the test data publishers to set for this custom reports publisher
     */
    @DataBoundSetter
    public final void setTestDataPublishers(@Nonnull List<TestDataPublisher> testDataPublishers) {
        this.testDataPublishers = new DescribableList<>(Saveable.NOOP);
        this.testDataPublishers.addAll(testDataPublishers);
    }

    @Symbol("xunit")
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

        public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
            return XUnitThresholdDescriptor.all();
        }

        public XUnitThreshold[] getListXUnitThresholdInstance() {
            return new XUnitThreshold[]{
                    new FailedThreshold(),
                    new SkippedThreshold()/*,
                    new PassedThreshold()*/
            };
        }

    }

}
