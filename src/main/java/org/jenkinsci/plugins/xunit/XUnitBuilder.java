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

import javax.annotation.CheckForNull;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

/**
 * @author Gregory Boissinot
 */
public class XUnitBuilder extends Builder implements SimpleBuildStep {

    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;

    public XUnitBuilder(@CheckForNull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds) {
        this.types = (tools != null ? Arrays.copyOf(tools, tools.length) : new TestType[0]);
        this.thresholds = (thresholds != null ? Arrays.copyOf(thresholds, thresholds.length) : new XUnitThreshold[0]);
        this.thresholdMode = 1;
    }

    @DataBoundConstructor
    public XUnitBuilder(@CheckForNull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds, int thresholdMode, @CheckForNull String testTimeMargin) {
        this(tools, thresholds);
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitDefaultValues.TEST_REPORT_TIME_MARGING;
        if (testTimeMargin != null && testTimeMargin.trim().length() != 0) {
            longTestTimeMargin = Long.parseLong(testTimeMargin);
        }
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin);
    }

    /*
     * Needed to support Snippet Generator and Workflow properly.
     */
    public TestType[] getTools() {
        return types;
    }

    /*
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

    @Override
    public void perform(final Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {
        XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
        xUnitProcessor.performXUnit(false, build, workspace, listener);
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
            xUnitProcessor.performXUnit(true, build, build.getWorkspace(), listener);
        } catch (Throwable t) {
            listener.getLogger().println("[ERROR] - There is an error: " + t.getCause().getMessage());
        }
        //Always exit on success (returned code and status)
        build.setResult(Result.SUCCESS);
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class XUnitDescriptorBuilder extends BuildStepDescriptor<Builder> {

        public XUnitDescriptorBuilder() {
            super(XUnitBuilder.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.xUnit_BuilderName();
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
                    new SkippedThreshold()
            };
        }
    }

}
