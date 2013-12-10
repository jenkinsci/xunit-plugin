package org.jenkinsci.plugins.xunit.threshold;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class FailedThreshold extends XUnitThreshold {

    public FailedThreshold() {
    }

    @DataBoundConstructor
    public FailedThreshold(String unstableThreshold, String unstableNewThreshold, String failureThreshold, String failureNewThreshold) {
        super(unstableThreshold, unstableNewThreshold, failureThreshold, failureNewThreshold);
    }

    @Override
    public Result getResultThresholdNumber(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        int failedCount = testResultAction.getFailCount();

        int previousFailedCount = 0;
        if (previousTestResultAction != null) {
            previousFailedCount = previousTestResultAction.getFailCount();
        }
        int newFailedCount = failedCount - previousFailedCount;


        return getResultThresholdNumber(log, failedCount, newFailedCount);
    }

    @Override
    public Result getResultThresholdPercent(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        double count = testResultAction.getTotalCount();

        double failedCount = testResultAction.getFailCount();
        double percentFailed = (failedCount / count) * 100;

        double previousFailedCount = 0;
        if (previousTestResultAction != null) {
            previousFailedCount = previousTestResultAction.getFailCount();
        }
        double newFailedCount = failedCount - previousFailedCount;
        double percentNewFailed = (newFailedCount / count) * 100;

        return getResultThresholdPercent(log, percentFailed, percentNewFailed);
    }

}
