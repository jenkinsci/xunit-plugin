package org.jenkinsci.plugins.xunit.pipeline;

import com.google.common.collect.ImmutableSet;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class XUnitResultsStep extends Step {
    private final List<TestType> tools = new ArrayList<>();
    private final List<XUnitThreshold> thresholds = new ArrayList<>();
    private int thresholdMode = 1;
    private long testTimeMargin = 3000L;

    @DataBoundConstructor
    public XUnitResultsStep(@Nonnull List<TestType> tools, @Nonnull List<XUnitThreshold> thresholds) {
        this.tools.addAll(tools);
        this.thresholds.addAll(thresholds);
    }

    @Nonnull
    public List<TestType> getTools() {
        return tools;
    }

    @Nonnull
    public List<XUnitThreshold> getThresholds() {
        return thresholds;
    }

    @DataBoundSetter
    public void setThresholdMode(int thresholdMode) {
        this.thresholdMode = thresholdMode;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    @DataBoundSetter
    public void setTestTimeMargin(long testTimeMargin) {
        this.testTimeMargin = testTimeMargin;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new XUnitResultsStepExecution(this, context);
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
            return "Transform and archive various test result formats";
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

        public List<XUnitThreshold> getListXUnitThresholdInstance() {
            return Arrays.asList(new FailedThreshold(), new SkippedThreshold());
        }

    }
}
