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
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
public class XUnitBuilder extends Builder implements SimpleBuildStep {

    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;

    /**
     * Computed
     */
    private XUnitProcessor xUnitProcessor;

    public XUnitBuilder(TestType[] types, XUnitThreshold[] thresholds) {
        this.types = Arrays.copyOf(types, types.length);
        this.thresholds = Arrays.copyOf(thresholds, thresholds.length);
        this.thresholdMode = 1;
    }

    @DataBoundConstructor
    public XUnitBuilder(TestType[] tools, XUnitThreshold[] thresholds, int thresholdMode, String testTimeMargin) {
        this.types = Arrays.copyOf(tools, tools.length);
        this.thresholds = Arrays.copyOf(thresholds, thresholds.length);
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitDefaultValues.TEST_REPORT_TIME_MARGING;
        if (testTimeMargin != null && testTimeMargin.trim().length() != 0) {
            longTestTimeMargin = Long.parseLong(testTimeMargin);
        }
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin);
    }

    /**
     * Needed to support Snippet Generator and Workflow properly.
     */
    public TestType[] getTools() {
        return Arrays.copyOf(types, types.length);
    }

    /**
     * Needed to support Snippet Generator and Workflow properly
     */
    public String getTestTimeMargin() {
        return String.valueOf(getExtraConfiguration().getTestTimeMargin());
    }

    public TestType[] getTypes() {
        return Arrays.copyOf(types, types.length);
    }

    public XUnitThreshold[] getThresholds() {
        return Arrays.copyOf(thresholds, thresholds.length);
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
    public boolean perform(@Nonnull final AbstractBuild<?, ?> build, @Nonnull Launcher launcher, @Nonnull final BuildListener listener)
            throws InterruptedException, IOException {
        FilePath ws = build.getWorkspace();
        if (ws == null) {
            throw new IllegalArgumentException("Build " + build.getDisplayName() + " has a null workspace");
        }
        perform(build, ws, launcher, listener);
        return true;
    }

    @Override
    public void perform(@Nonnull final Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull final TaskListener listener)
            throws InterruptedException, IOException {
        XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
        xUnitProcessor.performXUnit(false, build, null, workspace, listener);
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
            xUnitProcessor.performXUnit(true, build, null, build.getWorkspace(), listener);
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
    @SuppressWarnings("unused")
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
