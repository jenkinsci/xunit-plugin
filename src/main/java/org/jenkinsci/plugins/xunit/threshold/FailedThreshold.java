package org.jenkinsci.plugins.xunit.threshold;

import com.thalesgroup.hudson.plugins.xunit.service.XUnitLog;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
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
    public Result getResultThreshold(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        int failCount = testResultAction.getFailCount();

        int previousFailCount = 0;
        if (previousTestResultAction != null) {
            previousFailCount = previousTestResultAction.getFailCount();
        }
        int newFailCount = failCount - previousFailCount;

        return getResultThreshold(log, failCount, newFailCount);
    }

}
