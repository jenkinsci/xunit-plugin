package org.jenkinsci.plugins.xunit.threshold;

import com.thalesgroup.hudson.plugins.xunit.service.XUnitLog;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
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
    public Result getResultThreshold(XUnitLog log, AbstractBuild<?, ?> build, TestResultAction testResultAction, TestResultAction previousTestResultAction) {

        int skipCount = testResultAction.getSkipCount();

        int previousSkipCount = 0;
        if (previousTestResultAction != null) {
            previousSkipCount = previousTestResultAction.getSkipCount();
        }
        int newSkipCount = skipCount - previousSkipCount;

        return getResultThreshold(log, skipCount, newSkipCount);
    }

}
