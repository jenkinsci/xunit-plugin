package org.jenkinsci.plugins.xunit.threshold;

import com.thalesgroup.hudson.plugins.xunit.service.XUnitLog;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.*;
import hudson.tasks.junit.TestResultAction;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public abstract class XUnitThreshold implements ExtensionPoint, Serializable, Describable<XUnitThreshold> {

    private String unstableThreshold;

    private String unstableNewThreshold;

    private String failureThreshold;

    private String failureNewThreshold;

    protected XUnitThreshold() {
    }

    public XUnitThreshold(String unstableThreshold, String unstableNewThreshold, String failureThreshold, String failureNewThreshold) {
        this.unstableThreshold = unstableThreshold;
        this.unstableNewThreshold = unstableNewThreshold;
        this.failureThreshold = failureThreshold;
        this.failureNewThreshold = failureNewThreshold;
    }

    public Descriptor<XUnitThreshold> getDescriptor() {
        return (XUnitThresholdDescriptor<? extends XUnitThreshold>) Hudson.getInstance().getDescriptor(getClass());
    }

    @SuppressWarnings("unused")
    public static DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> all() {
        return Hudson.getInstance().<XUnitThreshold, XUnitThresholdDescriptor<?>>getDescriptorList(XUnitThreshold.class);
    }

    public String getUnstableThreshold() {
        return unstableThreshold;
    }

    public String getUnstableNewThreshold() {
        return unstableNewThreshold;
    }

    public String getFailureThreshold() {
        return failureThreshold;
    }

    public String getFailureNewThreshold() {
        return failureNewThreshold;
    }

    public abstract Result getResultThresholdNumber(XUnitLog log,
                                                    AbstractBuild<?, ?> build,
                                                    TestResultAction testResultAction,
                                                    TestResultAction previousTestResultAction);

    public abstract Result getResultThresholdPercent(XUnitLog log,
                                                     AbstractBuild<?, ?> build,
                                                     TestResultAction testResultAction,
                                                     TestResultAction previousTestResultAction);

    public Result getResultThresholdNumber(XUnitLog log,
                                           int testCount,
                                           int newTestCount) {

        if (isValid(getFailureThreshold())
                && (convertToInteger(getFailureThreshold()) < testCount)) {
            log.infoConsoleLogger("The total number of tests for this category exceeds the specified 'failure' threshold value.");
            return Result.FAILURE;
        }

        if (isValid(getFailureNewThreshold())
                && (convertToInteger(getFailureNewThreshold()) < newTestCount)) {
            log.infoConsoleLogger("The new number of tests for this category exceeds the specified 'new failure' threshold value.");
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold())
                && (convertToInteger(getUnstableThreshold()) < testCount)) {
            log.infoConsoleLogger("The total number of tests for this category exceeds the specified 'unstable' threshold value.");
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold())
                && (convertToInteger(getUnstableNewThreshold()) < newTestCount)) {
            log.infoConsoleLogger("The new number of tests for this category exceeds the specified 'new unstable' threshold value.");
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;

    }

    public Result getResultThresholdPercent(XUnitLog log,
                                            double testPercent,
                                            double newTestPercent) {

        if (isValid(getFailureThreshold())
                && (convertToIntegerPercent(getFailureThreshold()) < testPercent)) {
            log.infoConsoleLogger("The percent of the total number of tests for this category exceeds the specified 'failure' threshold percent value.");
            return Result.FAILURE;
        }

        if (isValid(getUnstableNewThreshold())
                && (convertToIntegerPercent(getFailureNewThreshold()) < newTestPercent)) {
            log.infoConsoleLogger("The percent of the new number of tests for this category exceeds the specified 'new failure' threshold percent value.");
            return Result.FAILURE;
        }

        if (isValid(getUnstableThreshold())
                && (convertToIntegerPercent(getUnstableThreshold()) < testPercent)) {
            log.infoConsoleLogger("The percent of tests for this category exceeds the specified 'unstable' threshold percent value.");
            return Result.UNSTABLE;
        }

        if (isValid(getUnstableNewThreshold())
                && (convertToIntegerPercent(getUnstableNewThreshold()) < newTestPercent)) {
            log.infoConsoleLogger("The percent of the new number of tests for this category exceeds the specified 'new unstable' threshold percent value.");
            return Result.UNSTABLE;
        }

        return Result.SUCCESS;
    }

    private int convertToInteger(String threshold) {
        return Integer.parseInt(threshold);
    }

    private int convertToIntegerPercent(String threshold) {
        String thresholdRemoved = threshold.replace("%", "");
        return Integer.parseInt(thresholdRemoved);
    }

    private boolean isValid(String threshold) {
        if (threshold == null) {
            return false;
        }

        if (threshold.trim().length() == 0) {
            return false;
        }

        try {
            Integer.parseInt(threshold);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
}
