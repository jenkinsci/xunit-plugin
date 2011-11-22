package org.jenkinsci.plugins.xunit.threshold;

import hudson.Extension;

/**
 * @author Gregory Boissinot
 */
@Extension
public class SkippedThresholdDescriptor extends XUnitThresholdDescriptor<SkippedThreshold> {

    public SkippedThresholdDescriptor() {
        super(SkippedThreshold.class);
    }

    public SkippedThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    @Override
    public String getDisplayName() {
        return Messages.displayName_skippedTests();
    }

    @Override
    public String getUnstableThresholdImgTitle() {
        return Messages.unstableThreshold_skippedTests();
    }

    @Override
    public String getUnstableNewThresholdImgTitle() {
        return Messages.failureNewThreshold_skippedTests();
    }

    @Override
    public String getFailureThresholdImgTitle() {
        return Messages.failureThreshold_skippedTests();
    }

    @Override
    public String getFailureNewThresholdImgTitle() {
        return Messages.failureNewThreshold_skippedTests();
    }

    @Override
    public String getThresholdHelpMessage() {
        return Messages.thresholdHelpMessage_skippedTests();
    }

}
