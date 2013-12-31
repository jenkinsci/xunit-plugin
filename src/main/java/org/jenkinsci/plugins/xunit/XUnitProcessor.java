package org.jenkinsci.plugins.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.plugins.xunit.exception.XUnitException;
import org.jenkinsci.plugins.xunit.service.*;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.CustomType;

import java.io.File;
import java.io.IOException;

/**
 * @author Gregory Boissinot
 */
public class XUnitProcessor {

    public static final String GENERATED_JUNIT_DIR = "generatedJUnitFiles";

    private static final int MODE_PERCENT = 2;

    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;

    public XUnitProcessor(TestType[] types, XUnitThreshold[] thresholds, int thresholdMode) {
        this.types = types;
        this.thresholds = thresholds;
        this.thresholdMode = thresholdMode;
    }

    public boolean performXUnit(boolean dryRun, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        final XUnitLog xUnitLog = getXUnitLogObject(listener);
        try {

            xUnitLog.infoConsoleLogger("Starting to record.");

            boolean continueTestProcessing;
            try {
                continueTestProcessing = performTests(xUnitLog, build, listener);
            } catch (StopTestProcessingException e) {
                build.setResult(Result.FAILURE);
                xUnitLog.infoConsoleLogger("There are errors when processing test results.");
                xUnitLog.infoConsoleLogger("Skipping tests recording.");
                xUnitLog.infoConsoleLogger("Stop build.");
                return true;
            }

            if (!continueTestProcessing) {
                xUnitLog.infoConsoleLogger("There are errors when processing test results.");
                xUnitLog.infoConsoleLogger("Skipping tests recording.");
                return true;
            }

            recordTestResult(build, listener, xUnitLog);
            processDeletion(dryRun, build, xUnitLog);
            Result result = getBuildStatus(build, xUnitLog);
            if (result != null) {
                if (!dryRun) {
                    xUnitLog.infoConsoleLogger("Setting the build status to " + result);
                    build.setResult(result);
                } else {
                    xUnitLog.infoConsoleLogger("Through the xUnit plugin, the build status will be set to " + result.toString());
                }
            }
            xUnitLog.infoConsoleLogger("Stopping recording.");
            return true;

        } catch (XUnitException xe) {
            xUnitLog.errorConsoleLogger("The plugin hasn't been performed correctly: " + xe.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private XUnitLog getXUnitLogObject(final BuildListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
            }
        }).getInstance(XUnitLog.class);
    }

    private boolean performTests(XUnitLog xUnitLog, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException, StopTestProcessingException {
        XUnitReportProcessorService xUnitReportService = getXUnitReportProcessorServiceObject(listener);
        boolean findTest = false;
        for (TestType tool : types) {
            xUnitLog.infoConsoleLogger("Processing " + tool.getDescriptor().getDisplayName());

            if (!isEmptyGivenPattern(xUnitReportService, tool)) {
                String expandedPattern = getExpandedResolvedPattern(tool, build, listener);
                XUnitToolInfo xUnitToolInfo = getXUnitToolInfoObject(tool, expandedPattern, build, listener);
                XUnitTransformer xUnitTransformer = getXUnitTransformerObject(xUnitToolInfo, listener);
                boolean result = false;
                try {
                    result = getWorkspace(build).act(xUnitTransformer);
                    findTest = true;
                } catch (NoTestException ne) {
                    xUnitLog.infoConsoleLogger("Fail BUILD.");
                    throw new StopTestProcessingException();
                } catch (SkipTestException se) {
                    xUnitLog.infoConsoleLogger("Skipping the metric tool processing.");
                    continue;
                }

                if (!result && xUnitToolInfo.isStopProcessingIfError()) {
                    xUnitLog.infoConsoleLogger("Fail BUILD because 'set build failed if errors' option is activated.");
                    throw new StopTestProcessingException();
                }
            }


        }
        return findTest;
    }

    private XUnitReportProcessorService getXUnitReportProcessorServiceObject(final BuildListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
            }
        }).getInstance(XUnitReportProcessorService.class);
    }

    private static class StopTestProcessingException extends Exception {
    }

    private boolean isEmptyGivenPattern(XUnitReportProcessorService xUnitReportService, TestType tool) {
        return xUnitReportService.isEmptyPattern(tool.getPattern());
    }

    private String getExpandedResolvedPattern(TestType tool, AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        String newExpandedPattern = tool.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        return Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));
    }

    private XUnitToolInfo getXUnitToolInfoObject(TestType tool, String expandedPattern, AbstractBuild build, final BuildListener listener) throws IOException, InterruptedException {

        InputMetric inputMetric = tool.getInputMetric();
        inputMetric = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
                bind(XUnitLog.class).in(Singleton.class);
                bind(XUnitValidationService.class).in(Singleton.class);
                bind(XUnitConversionService.class).in(Singleton.class);
            }
        }).getInstance(inputMetric.getClass());

        return new XUnitToolInfo(
                new FilePath(new File(Hudson.getInstance().getRootDir(), "userContent")),
                inputMetric,
                expandedPattern,
                tool.isSkipNoTestFiles(),
                tool.isFailIfNotNew(),
                tool.isDeleteOutputFiles(), tool.isStopProcessingIfError(),
                build.getTimeInMillis(),
                (tool instanceof CustomType) ? getWorkspace(build).child(Util.replaceMacro(((CustomType) tool).getCustomXSL(), build.getEnvironment(listener))) : null);

    }

    private FilePath getWorkspace(AbstractBuild build) {
        FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            workspace = build.getProject().getSomeWorkspace();
        }
        return workspace;
    }

    private XUnitTransformer getXUnitTransformerObject(final XUnitToolInfo xUnitToolInfo, final BuildListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
                bind(XUnitToolInfo.class).toInstance(xUnitToolInfo);
                bind(XUnitValidationService.class).in(Singleton.class);
                bind(XUnitConversionService.class).in(Singleton.class);
                bind(XUnitLog.class).in(Singleton.class);
                bind(XUnitReportProcessorService.class).in(Singleton.class);
            }
        }).getInstance(XUnitTransformer.class);
    }

    private TestResultAction getTestResultAction(AbstractBuild<?, ?> build) {
        return build.getAction(TestResultAction.class);
    }

    private TestResultAction getPreviousTestResultAction(AbstractBuild<?, ?> build) {
        AbstractBuild previousBuild = build.getPreviousBuild();
        if (previousBuild == null) {
            return null;
        }
        return getTestResultAction(previousBuild);
    }

    /**
     * Records the test results into the current build and return the number of tests
     */
    private void recordTestResult(AbstractBuild<?, ?> build, BuildListener listener, XUnitLog xUnitLog) throws XUnitException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long nowMaster = System.currentTimeMillis();

        TestResult existingTestResults = null;
        if (existingAction != null) {
            existingTestResults = existingAction.getResult();
        }

        TestResult result = getTestResult(build, "**/TEST-*.xml", existingTestResults, buildTime, nowMaster);
        if (result != null) {
            TestResultAction action;
            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }

            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                xUnitLog.warningConsoleLogger("All test reports are empty.");
            }

            if (existingAction == null) {
                build.getActions().add(action);
            }
        }
    }

    /**
     * Gets a Test result object (a new one if any)
     *
     * @param build               the current build
     * @param junitFilePattern    the JUnit search pattern
     * @param existingTestResults the existing test result
     * @param buildTime           the build time
     * @param nowMaster           the time on master
     * @return the test result object
     * @throws XUnitException the plugin exception
     */
    private TestResult getTestResult(final AbstractBuild<?, ?> build,
                                     final String junitFilePattern,
                                     final TestResult existingTestResults,
                                     final long buildTime, final long nowMaster)
            throws XUnitException {

        try {
            return getWorkspace(build).act(new FilePath.FileCallable<TestResult>() {

                public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                    final long nowSlave = System.currentTimeMillis();
                    File generatedJunitDir = new File(ws, GENERATED_JUNIT_DIR);
                    //Try to create the file if it was deleted or something was wrong
                    if (!generatedJunitDir.mkdirs()) {
                        throw new XUnitException("Cannot create " + generatedJunitDir.getAbsolutePath());
                    }
                    FileSet fs = Util.createFileSet(generatedJunitDir, junitFilePattern);
                    DirectoryScanner ds = fs.getDirectoryScanner();
                    String[] files = ds.getIncludedFiles();

                    if (files.length == 0) {
                        // no test result. Most likely a configuration error or fatal problem
                        return null;

                    }
                    try {
                        if (existingTestResults == null) {
                            return new TestResult(buildTime + (nowSlave - nowMaster), ds, true);
                        } else {
                            existingTestResults.parse(buildTime + (nowSlave - nowMaster), ds);
                            return existingTestResults;
                        }
                    } catch (IOException ioe) {
                        throw new IOException(ioe);
                    }
                }

            });

        } catch (IOException ioe) {
            throw new XUnitException(ioe.getMessage(), ioe);
        } catch (InterruptedException ie) {
            throw new XUnitException(ie.getMessage(), ie);
        }
    }

    private Result getBuildStatus(AbstractBuild<?, ?> build, XUnitLog xUnitLog) {
        Result curResult = getResultWithThreshold(xUnitLog, build);
        Result previousResultStep = build.getResult();
        if (curResult != null) {
            if (previousResultStep == null) {
                return curResult;
            }
            if (previousResultStep.isWorseOrEqualTo(curResult)) {
                curResult = previousResultStep;
            }
            return curResult;
        }
        return null;
    }

    private Result getResultWithThreshold(XUnitLog log, AbstractBuild<?, ?> build) {
        TestResultAction testResultAction = getTestResultAction(build);
        TestResultAction previousTestResultAction = getPreviousTestResultAction(build);
        if (testResultAction == null) {
            return Result.FAILURE;
        } else {
            return processResultThreshold(log, build, testResultAction, previousTestResultAction);
        }
    }

    private Result processResultThreshold(XUnitLog log,
                                          AbstractBuild<?, ?> build,
                                          TestResultAction testResultAction,
                                          TestResultAction previousTestResultAction) {

        if (thresholds != null) {
            for (XUnitThreshold threshold : thresholds) {
                log.infoConsoleLogger(String.format("Check '%s' threshold.", threshold.getDescriptor().getDisplayName()));
                Result result;
                if (MODE_PERCENT == thresholdMode) {
                    result = threshold.getResultThresholdPercent(log, build, testResultAction, previousTestResultAction);
                } else {
                    result = threshold.getResultThresholdNumber(log, build, testResultAction, previousTestResultAction);
                }
                if (result.isWorseThan(Result.SUCCESS)) {
                    return result;
                }
            }
        }

        return Result.SUCCESS;
    }


    private void processDeletion(boolean dryRun, AbstractBuild<?, ?> build, XUnitLog xUnitLog) throws XUnitException {
        try {
            boolean keepJUnitDirectory = false;
            for (TestType tool : types) {
                InputMetric inputMetric = tool.getInputMetric();

                if (dryRun || tool.isDeleteOutputFiles()) {
                    getWorkspace(build).child(GENERATED_JUNIT_DIR + "/" + inputMetric.getToolName()).deleteRecursive();
                } else {
                    //Mark the tool file parent directory to no deletion
                    keepJUnitDirectory = true;
                }
            }
            if (!keepJUnitDirectory) {
                getWorkspace(build).child(GENERATED_JUNIT_DIR).deleteRecursive();
            }
        } catch (IOException ioe) {
            throw new XUnitException("Problem on deletion", ioe);
        } catch (InterruptedException ie) {
            throw new XUnitException("Problem on deletion", ie);
        }
    }

}
