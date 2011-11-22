package org.jenkinsci.plugins.xunit.threshold;

import hudson.Extension;

/**
 * @author Gregory Boissinot
 */
@Extension
public class FailedThresholdDescriptor extends XUnitThresholdDescriptor<FailedThreshold> {

    public FailedThresholdDescriptor() {
        super(FailedThreshold.class);
    }

    public FailedThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    @Override
    public String getDisplayName() {
        return Messages.displayName_failedTests();
    }

    @Override
    public String getUnstableThresholdImgTitle() {
        return Messages.unstableThreshold_failedTests();
    }

    @Override
    public String getUnstableNewThresholdImgTitle() {
        return Messages.failureNewThreshold_failedTests();
    }

    @Override
    public String getFailureThresholdImgTitle() {
        return Messages.failureThreshold_failedTests();
    }

    @Override
    public String getFailureNewThresholdImgTitle() {
        return Messages.failureNewThreshold_failedTests();
    }

    @Override
    public String getThresholdHelpMessage() {
        return Messages.thresholdHelpMessage_failedTests();
    }
}
