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

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.test.TestResultProjectAction;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.lib.dryrun.DryRun;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.threshold.*;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;

/**
 * Class that converting custom reports to Junit reports and records them
 *
 * @author Gregory Boissinot
 */
@SuppressWarnings({"unchecked", "unused"})
public class XUnitPublisher extends Recorder implements DryRun, Serializable, SimpleBuildStep {

    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private boolean shouldFailIfFrequentTest;
    private String ageFailedTest;
    private String unstableTests;
    private String historyBuilds;
    private ExtraConfiguration extraConfiguration;

    public XUnitPublisher(TestType[] types, XUnitThreshold[] thresholds) {
        this.types = types;
        this.thresholds = thresholds;
        this.thresholdMode = 1;
    }

    @DataBoundConstructor
    public XUnitPublisher(TestType[] tools, XUnitThreshold[] thresholds, int thresholdMode, boolean shouldFailIfFrequentTest, String ageFailedTest, String unstableTests, String historyBuilds, String testTimeMargin) {
        this.types = tools;
        this.thresholds = thresholds;
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitDefaultValues.TEST_REPORT_TIME_MARGING;
        if (testTimeMargin != null && testTimeMargin.trim().length() != 0) {
            longTestTimeMargin = Long.parseLong(testTimeMargin);
        }
        this.shouldFailIfFrequentTest = shouldFailIfFrequentTest;
        this.ageFailedTest = ageFailedTest;
        this.unstableTests = unstableTests;
        this.historyBuilds = historyBuilds;
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin);
    }

    /**
     * Needed to support Snippet Generator and Workflow properly.
     */
    public TestType[] getTools() {
        return types;
    }

    /**
     * Needed to support Snippet Generator and Workflow properly
     */
    public String getTestTimeMargin() {
        return String.valueOf(getExtraConfiguration().getTestTimeMargin());
    }

    public TestType[] getTypes() {
        return types;
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

    public boolean isShouldFailIfFrequentTest() {
        return shouldFailIfFrequentTest;
    }

    public String getAgeFailedTest() {
        return ageFailedTest;
    }

    public String getUnstableTests() {
        return unstableTests;
    }

    public String getHistoryBuilds() {
        return historyBuilds;
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
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        perform(build, build.getWorkspace(), launcher, listener);
        return true;
    }

    @Override
    public void perform(final Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {
        XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), isShouldFailIfFrequentTest(), getAgeFailedTest(), getUnstableTests(), getHistoryBuilds(), getExtraConfiguration());
        xUnitProcessor.performXUnit(false, build, workspace, listener);
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), isShouldFailIfFrequentTest(), getAgeFailedTest(), getUnstableTests(), getHistoryBuilds(), getExtraConfiguration());
            xUnitProcessor.performXUnit(true, build, build.getWorkspace(), listener);
        } catch (Throwable t) {
            listener.getLogger().println("[ERROR] - There is an error: " + t.getCause().getMessage());
        }
        //Always exit on success (returned code and status)
        build.setResult(Result.SUCCESS);
        return true;
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    @SuppressWarnings("unused")
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
        public boolean isApplicable(Class type) {
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
                    new SkippedThreshold()
            };
        }

    }

}
