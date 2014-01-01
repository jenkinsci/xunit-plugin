package org.jenkinsci.plugins.xunit;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class XUnitBuilder extends Builder {

    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;

    /**
     * Computed
     */
    private XUnitProcessor xUnitProcessor;

    public XUnitBuilder(TestType[] types, XUnitThreshold[] thresholds) {
        this.types = types;
        this.thresholds = thresholds;
        this.thresholdMode = 1;
    }

    @DataBoundConstructor
    public XUnitBuilder(TestType[] tools, XUnitThreshold[] thresholds, int thresholdMode, String testTimeMargin) {
        this.types = tools;
        this.thresholds = thresholds;
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitDefaultValues.TEST_REPORT_TIME_MARGING;
        if (testTimeMargin != null && testTimeMargin.trim().length() != 0) {
            longTestTimeMargin = Long.parseLong(testTimeMargin);
        }
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin);
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
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
        return xUnitProcessor.performXUnit(false, build, listener);
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            XUnitProcessor xUnitProcessor = new XUnitProcessor(getTypes(), getThresholds(), getThresholdMode(), getExtraConfiguration());
            xUnitProcessor.performXUnit(true, build, listener);
        } catch (Throwable t) {
            listener.getLogger().println("[ERROR] - There is an error: " + t.getCause().getMessage());
        }
        //Always exit on success (returned code and status)
        build.setResult(Result.SUCCESS);
        return true;
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
