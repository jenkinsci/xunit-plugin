/*
* The MIT License (MIT)
*
* Copyright (c) 2014, Gregory Boissinot
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package org.jenkinsci.plugins.xunit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.exception.XUnitException;
import org.jenkinsci.plugins.xunit.service.XUnitConversionService;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.service.XUnitReportProcessorService;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.service.XUnitTransformer;
import org.jenkinsci.plugins.xunit.service.XUnitValidationService;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.CustomType;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.util.IOUtils;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
public class XUnitProcessor {

    /**
     * Parse generated JUnit report on a slave node.
     * <p>
     * This class is external to ensure serialisation is not broken as anonymous
     * class requires that whole outer class was serialisable too.
     **/
    private static final class ReportParser extends jenkins.SlaveToMasterFileCallable<TestResult> {
        private static final long serialVersionUID = 1L;

        private final String junitFilePattern;
        private final long buildTime;
        private final long nowMaster;
        private final TestResult existingTestResults;
        private final String processorId;

        public ReportParser(long buildTime, @Nonnull String junitFilePattern, long nowMaster, TestResult existingTestResults, String processorId) {
            this.buildTime = buildTime;
            this.junitFilePattern = junitFilePattern;
            this.nowMaster = nowMaster;
            this.existingTestResults = existingTestResults;
            this.processorId = processorId;
        }

        @Override
        public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
            final long nowSlave = System.currentTimeMillis();
            File generatedJunitDir = new File(new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR), processorId);
            IOUtils.mkdirs(generatedJunitDir);
            FileSet fs = Util.createFileSet(generatedJunitDir, junitFilePattern);
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] files = ds.getIncludedFiles();

            if (files.length == 0) {
                // no test result. Most likely a configuration error or fatal problem
                return null;

            }
            if (existingTestResults == null) {
                return new TestResult(buildTime + (nowSlave - nowMaster), ds, true);
            } else {
                existingTestResults.parse(buildTime + (nowSlave - nowMaster), ds);
                return existingTestResults;
            }
        }
    }

    private final TestType[] tools;
    private final XUnitThreshold[] thresholds;
    private final int thresholdMode;
    private final ExtraConfiguration extraConfiguration;
    private final String processorId;

    public XUnitProcessor(@Nonnull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds, int thresholdMode, @Nonnull ExtraConfiguration extraConfiguration) {
        if (tools == null) {
            throw new IllegalArgumentException("The tools section is required.");
        }
        if (extraConfiguration == null) {
            throw new IllegalArgumentException("The extra configuration is required.");
        }
        this.tools = Arrays.copyOf(tools, tools.length);
        this.thresholds = thresholds != null ? Arrays.copyOf(thresholds, thresholds.length) : new XUnitThreshold[0];
        this.thresholdMode = thresholdMode;
        this.extraConfiguration = extraConfiguration;
        this.processorId = UUID.randomUUID().toString();
    }

    public boolean performXunit(boolean dryRun, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        return performXUnit(dryRun, build, build.getWorkspace(), listener);
    }

    public boolean performXUnit(boolean dryRun, Run<?, ?> build, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        final XUnitLog xUnitLog = getXUnitLogObject(listener);
        try {
            xUnitLog.info("Starting to record.");

            boolean continueTestProcessing;
            try {
                continueTestProcessing = performTests(xUnitLog, build, workspace, listener);
            } catch (StopTestProcessingException e) {
                build.setResult(Result.FAILURE);
                xUnitLog.info("There are errors when processing test results.");
                xUnitLog.info("Skipping tests recording.");
                xUnitLog.info("Stop build.");
                return true;
            }

            if (!continueTestProcessing) {
                xUnitLog.info("There are errors when processing test results.");
                xUnitLog.info("Skipping tests recording.");
                return true;
            }

            recordTestResult(build, workspace, listener, xUnitLog);
            processDeletion(dryRun, workspace, xUnitLog);
            Result result = getBuildStatus(build, xUnitLog);
            if (result != null) {
                if (!dryRun) {
                    xUnitLog.info("Setting the build status to " + result);
                    build.setResult(result);
                } else {
                    xUnitLog.info("Through the xUnit plugin, the build status will be set to " + result.toString());
                }
            }
            xUnitLog.info("Stopping recording.");
            return true;

        } catch (XUnitException xe) {
            xUnitLog.error("The plugin hasn't been performed correctly: " + xe.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    private XUnitLog getXUnitLogObject(final TaskListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TaskListener.class).toInstance(listener);
            }
        }).getInstance(XUnitLog.class);
    }

    private boolean performTests(XUnitLog xUnitLog, Run<?, ?> build, FilePath workspace, TaskListener listener) throws IOException, InterruptedException, StopTestProcessingException {
        XUnitReportProcessorService xUnitReportService = getXUnitReportProcessorServiceObject(listener);
        boolean findTest = false;
        for (TestType tool : tools) {
            xUnitLog.info("Processing " + tool.getDescriptor().getDisplayName());

            if (!isEmptyGivenPattern(xUnitReportService, tool)) {
                String expandedPattern = getExpandedResolvedPattern(tool, build, listener);
                XUnitToolInfo xUnitToolInfo = getXUnitToolInfoObject(tool, expandedPattern, build, workspace, listener);
                XUnitTransformer xUnitTransformer = getXUnitTransformerObject(xUnitToolInfo, listener);
                boolean result = false;
                try {
                    result = workspace.act(xUnitTransformer);
                    findTest = true;
                } catch (InterruptedException ie) {
                    // handled tunneled exceptions
                    Throwable originalException = null;
                    Throwable cause = ie.getCause();
                    while (cause != null) {
                        originalException = cause;
                        cause = cause.getCause();
                    }
                    if (originalException instanceof InterruptedException)
                        ie = (InterruptedException) originalException;

                    if (ie instanceof NoFoundTestException) {
                        xUnitLog.info("Failing BUILD.");
                        throw new StopTestProcessingException();
                    }

                    if (ie instanceof SkipTestException) {
                        xUnitLog.info("Skipping the metric tool processing.");
                        continue;
                    }

                    if (ie instanceof OldTestReportException) {
                        xUnitLog.info("Failing BUILD.");
                        throw new StopTestProcessingException();
                    }

                    xUnitLog.warn("Caught exception of unexpected type " + ie.getClass() + ", rethrowing");
                    throw ie;
                }

                if (!result && xUnitToolInfo.isStopProcessingIfError()) {
                    xUnitLog.info("Failing BUILD because 'set build failed if errors' option is activated.");
                    throw new StopTestProcessingException();
                }
            }
        }
        return findTest;
    }

    private XUnitReportProcessorService getXUnitReportProcessorServiceObject(final TaskListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TaskListener.class).toInstance(listener);
            }
        }).getInstance(XUnitReportProcessorService.class);
    }

    private static class StopTestProcessingException extends Exception {
    }

    private boolean isEmptyGivenPattern(XUnitReportProcessorService xUnitReportService, TestType tool) {
        return xUnitReportService.isEmptyPattern(tool.getPattern());
    }

    private String getExpandedResolvedPattern(TestType tool, Run<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        String newExpandedPattern = tool.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        return Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));
    }

    private XUnitToolInfo getXUnitToolInfoObject(final TestType tool, final String expandedPattern, final Run<?, ?> build, final FilePath workspace, final TaskListener listener) throws IOException, InterruptedException {

        InputMetric inputMetric = tool.getInputMetric();
        inputMetric = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TaskListener.class).toInstance(listener);
                bind(XUnitLog.class).in(Singleton.class);
                bind(XUnitValidationService.class).in(Singleton.class);
                bind(XUnitConversionService.class).in(Singleton.class);
            }
        }).getInstance(inputMetric.getClass());

        return new XUnitToolInfo(
                new FilePath(new File(Jenkins.getActiveInstance().getRootDir(), "userContent")),
                inputMetric,
                expandedPattern,
                tool.isSkipNoTestFiles(),
                tool.isFailIfNotNew(),
                tool.isDeleteOutputFiles(), tool.isStopProcessingIfError(),
                build.getTimeInMillis(),
                this.extraConfiguration.getTestTimeMargin(),
                (tool instanceof CustomType) ? getCustomStylesheet(tool, build, workspace, listener) : null);

    }

    private FilePath getCustomStylesheet(final TestType tool, final Run<?, ?> build, final FilePath workspace, final TaskListener listener) throws IOException, InterruptedException {

        final String customXSLPath = Util.replaceMacro(((CustomType) tool).getCustomXSL(), build.getEnvironment(listener));

        //Try full path
        FilePath customXSLFilePath = new FilePath(new File(customXSLPath));
        if (!customXSLFilePath.exists()) {
            //Try from workspace
            customXSLFilePath = workspace.child(customXSLPath);
        }

        if (!customXSLFilePath.exists()) {
            throw new XUnitException("The given xsl '" + customXSLPath + "'doesn't exist.");
        }

        return customXSLFilePath;
    }

    private XUnitTransformer getXUnitTransformerObject(final XUnitToolInfo xUnitToolInfo, final TaskListener listener) {
        // TODO why use Guice in this manner it's the quite the same of instantiate classes directly
        XUnitTransformer transformer = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TaskListener.class).toInstance(listener);
                bind(XUnitToolInfo.class).toInstance(xUnitToolInfo);
                bind(XUnitValidationService.class).in(Singleton.class);
                bind(XUnitConversionService.class).in(Singleton.class);
                bind(XUnitLog.class).in(Singleton.class);
                bind(XUnitReportProcessorService.class).in(Singleton.class);
            }
        }).getInstance(XUnitTransformer.class);
        transformer.setProcessorId(processorId);
        return transformer;
    }

    private TestResultAction getTestResultAction(Run<?, ?> build) {
        return build.getAction(TestResultAction.class);
    }

    private TestResultAction getPreviousTestResultAction(Run<?, ?> build) {
        Run<?, ?> previousBuild = build.getPreviousCompletedBuild();
        if (previousBuild == null) {
            return null;
        }
        return getTestResultAction(previousBuild);
    }

    private void recordTestResult(Run<?, ?> build, FilePath workspace, TaskListener listener, XUnitLog xUnitLog) throws XUnitException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long nowMaster = System.currentTimeMillis();

        TestResult existingTestResults = null;
        if (existingAction != null) {
            existingTestResults = existingAction.getResult();
        }

        TestResult result = getTestResult(workspace, "**/TEST-*.xml", existingTestResults, buildTime, nowMaster);
        if (result != null) {
            TestResultAction action;
            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }

            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                xUnitLog.warn("All test reports are empty.");
            }

            if (existingAction == null) {
                build.addAction(action);
            }
        }
    }

    /**
     * Gets a Test result object (a new one if any)
     *
     * @param workspace           the build's workspace
     * @param junitFilePattern    the JUnit search pattern
     * @param existingTestResults the existing test result
     * @param buildTime           the build time
     * @param nowMaster           the time on master
     * @return the test result object
     * @throws XUnitException the plugin exception
     */
    private TestResult getTestResult(final FilePath workspace,
                                     final String junitFilePattern,
                                     final TestResult existingTestResults,
                                     final long buildTime, final long nowMaster)
            throws XUnitException {

        try {
            return workspace.act(new ReportParser(buildTime, junitFilePattern, nowMaster, existingTestResults, processorId));
        } catch (IOException | InterruptedException e) {
            throw new XUnitException(e.getMessage(), e);
        }
    }

    private Result getBuildStatus(Run<?, ?> build, XUnitLog xUnitLog) {
        Result curResult = getResultWithThreshold(xUnitLog, build);
        Result previousResultStep = build.getResult();
        if (previousResultStep == null) {
            return curResult;
        }
        if (previousResultStep != Result.NOT_BUILT && previousResultStep.isWorseOrEqualTo(curResult)) {
            curResult = previousResultStep;
        }
        return curResult;
    }

    private Result getResultWithThreshold(XUnitLog log, Run<?, ?> build) {
        TestResultAction testResultAction = getTestResultAction(build);
        TestResultAction previousTestResultAction = getPreviousTestResultAction(build);
        if (testResultAction == null) {
            return Result.FAILURE;
        } else {
            return processResultThreshold(log, build, testResultAction, previousTestResultAction);
        }
    }

    private Result processResultThreshold(XUnitLog log,
                                          Run<?, ?> build,
                                          TestResultAction testResultAction,
                                          TestResultAction previousTestResultAction) {

        if (thresholds != null) {
            for (XUnitThreshold threshold : thresholds) {
                log.info(String.format("Check '%s' threshold.", threshold.getDescriptor().getDisplayName()));
                Result result;
                if (XUnitDefaultValues.MODE_PERCENT == thresholdMode) {
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

    private void processDeletion(boolean dryRun, FilePath workspace, XUnitLog xUnitLog) throws XUnitException {
        try {
            FilePath generatedJunitDir = workspace.child(XUnitDefaultValues.GENERATED_JUNIT_DIR).child(processorId);

            boolean keepJUnitDirectory = false;
            for (TestType tool : tools) {
                InputMetric inputMetric = tool.getInputMetric();

                if (dryRun || tool.isDeleteOutputFiles()) {
                    generatedJunitDir.child(inputMetric.getToolName()).deleteRecursive();
                } else {
                    //Mark the tool file parent directory to no deletion
                    keepJUnitDirectory = true;
                }
            }
            if (!keepJUnitDirectory) {
                generatedJunitDir.deleteRecursive();
            }
        } catch (IOException | InterruptedException e) {
            throw new XUnitException("Problem on deletion", e);
        }
    }

}
