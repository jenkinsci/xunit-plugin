package org.jenkinsci.plugins.xunit.threshold;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class SkippedThreshold extends XUnitThreshold {

    public SkippedThreshold() {
    }

    @DataBoundConstructor
    public SkippedThreshold(String unstableThreshold, String unstableNewThreshold, String failureThreshold, String failureNewThreshold) {
        super(unstableThreshold, unstableNewThreshold, failureThreshold, failureNewThreshold);
    }

    @Override
    public Result getResultThresholdNumber(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        int skipCount = testResultAction.getSkipCount();

        int previousSkipCount = 0;
        if (previousTestResultAction != null) {
            previousSkipCount = previousTestResultAction.getSkipCount();
        }
        int newSkipCount = skipCount - previousSkipCount;

        return getResultThresholdNumber(log, skipCount, newSkipCount);
    }

    @Override
    public Result getResultThresholdPercent(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        int count = testResultAction.getTotalCount();
        int skippedCount = testResultAction.getSkipCount();
        int percentSkipped = (count == 0)?0:(skippedCount / count) * 100;

        int previousSkippedCount = 0;
        if (previousTestResultAction != null) {
            previousSkippedCount = previousTestResultAction.getSkipCount();
        }
        int newSkippedCount = skippedCount - previousSkippedCount;
        int percentNewSkipped = (count == 0)?0:(newSkippedCount / count) * 100;

        return getResultThresholdPercent(log, percentSkipped, percentNewSkipped);
    }

}
