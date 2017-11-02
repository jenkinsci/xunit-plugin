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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.PipelineTestDetails;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.exception.XUnitException;
import org.jenkinsci.plugins.xunit.service.*;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.CustomType;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Gregory Boissinot
 */
@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "generatedJunitDir.mkdirs() result is not needed")
public class XUnitProcessor implements Serializable {
    private static final long serialVersionUID = 1L;
    private TestType[] types;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;

    public XUnitProcessor(TestType[] types, XUnitThreshold[] thresholds, int thresholdMode, ExtraConfiguration extraConfiguration) {
        if (types == null) {
            throw new NullPointerException("The types section is required.");
        }
        this.types = Arrays.copyOf(types, types.length);
        this.thresholds = Arrays.copyOf(thresholds, thresholds.length);
        this.thresholdMode = thresholdMode;
        this.extraConfiguration = extraConfiguration;
    }

    public boolean performXunit(boolean dryRun, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        return performXUnit(dryRun, build, null, build.getWorkspace(), listener);
    }

    @Deprecated
    public boolean performXUnit(boolean dryRun, Run<?, ?> build, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        return performXUnit(dryRun, build, null, workspace, listener);
    }

    /**
     * @since 1.103
     */
    public boolean performXUnit(boolean dryRun, Run<?, ?> build, PipelineTestDetails pipelineTestDetails, FilePath workspace,
                                TaskListener listener) throws IOException, InterruptedException {
        final XUnitLog xUnitLog = getXUnitLogObject(listener);
        try {

            xUnitLog.infoConsoleLogger("Starting to record.");

            boolean continueTestProcessing;
            try {
                continueTestProcessing = performTests(xUnitLog, build, workspace, listener);
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

            recordTestResult(build, workspace, listener, xUnitLog, pipelineTestDetails);
            processDeletion(dryRun, workspace, xUnitLog);
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

    @CheckForNull
    public TestResultAction performAndGetAction(Run<?, ?> build, PipelineTestDetails pipelineTestDetails, FilePath workspace,
                                                TaskListener listener)
            throws IOException, InterruptedException, StopTestProcessingException, XUnitException {
        final XUnitLog xUnitLog = getXUnitLogObject(listener);
        try {

            xUnitLog.infoConsoleLogger("Starting to record.");
            synchronized (build) {
                boolean continueTestProcessing;
                try {
                    continueTestProcessing = performTests(xUnitLog, build, workspace, listener);
                } catch (StopTestProcessingException e) {
                    xUnitLog.infoConsoleLogger("There are errors when processing test results.");
                    xUnitLog.infoConsoleLogger("Skipping tests recording.");
                    xUnitLog.infoConsoleLogger("Stop build.");
                    throw e;
                }

                if (!continueTestProcessing) {
                    xUnitLog.infoConsoleLogger("There are errors when processing test results.");
                    xUnitLog.infoConsoleLogger("Skipping tests recording.");
                    return null;
                }

                recordTestResult(build, workspace, listener, xUnitLog, pipelineTestDetails);
                // dryRun is true so that we delete the generated files for future runs.
                processDeletion(true, workspace, xUnitLog);
            }
            return getTestResultAction(build);
        } catch (XUnitException xe) {
            xUnitLog.errorConsoleLogger("The plugin hasn't been performed correctly: " + xe.getMessage());
            throw xe;
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
        for (TestType tool : types) {
            xUnitLog.infoConsoleLogger("Processing " + tool.getDescriptor().getDisplayName());

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
                        xUnitLog.infoConsoleLogger("Failing BUILD.");
                        throw new StopTestProcessingException();
                    }

                    if (ie instanceof SkipTestException) {
                        xUnitLog.infoConsoleLogger("Skipping the metric tool processing.");
                        continue;
                    }

                    if (ie instanceof OldTestReportException) {
                        xUnitLog.infoConsoleLogger("Failing BUILD.");
                        throw new StopTestProcessingException();
                    }

                    xUnitLog.warningConsoleLogger("Caught exception of unexpected type " + ie.getClass() + ", rethrowing");
                    throw ie;
                }

                if (!result && xUnitToolInfo.isStopProcessingIfError()) {
                    xUnitLog.infoConsoleLogger("Failing BUILD because 'set build failed if errors' option is activated.");
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

    private String getExpandedResolvedPattern(TestType tool, Run build, TaskListener listener) throws IOException, InterruptedException {
        String newExpandedPattern = tool.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        return Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));
    }

    private XUnitToolInfo getXUnitToolInfoObject(final TestType tool, final String expandedPattern, final Run build, final FilePath workspace, final TaskListener listener) throws IOException, InterruptedException {

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
                new FilePath(new File(Hudson.getInstance().getRootDir(), "userContent")),
                inputMetric,
                expandedPattern,
                tool.isSkipNoTestFiles(),
                tool.isFailIfNotNew(),
                tool.isDeleteOutputFiles(), tool.isStopProcessingIfError(),
                build.getTimeInMillis(),
                this.extraConfiguration.getTestTimeMargin(),
                (tool instanceof CustomType) ? getCustomStylesheet(tool, build, workspace, listener) : null);

    }

    private FilePath getCustomStylesheet(final TestType tool, final Run build, final FilePath workspace, final TaskListener listener) throws IOException, InterruptedException {

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
        return Guice.createInjector(new AbstractModule() {
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
    }

    private TestResultAction getTestResultAction(Run<?, ?> build) {
        return build.getAction(TestResultAction.class);
    }

    private TestResultAction getPreviousTestResultAction(Run<?, ?> build) {
        Run previousBuild = build.getPreviousBuild();
        if (previousBuild == null) {
            return null;
        }
        return getTestResultAction(previousBuild);
    }

    private void recordTestResult(Run<?, ?> build, FilePath workspace, TaskListener listener, XUnitLog xUnitLog,
                                  PipelineTestDetails pipelineTestDetails) throws XUnitException {
        synchronized (build) {
            TestResultAction action = build.getAction(TestResultAction.class);
            final long buildTime = build.getTimestamp().getTimeInMillis();
            final long nowMaster = System.currentTimeMillis();

            boolean appending = false;
            TestResult result = getTestResult(workspace, "**/TEST-*.xml", null, buildTime, nowMaster,
                    pipelineTestDetails);
            if (action == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                appending = true;
                result.freeze(action);
                action.mergeResult(result, listener);
            }

            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                xUnitLog.warningConsoleLogger("All test reports are empty.");
            }

            if (appending) {
                try {
                    build.save();
                } catch (IOException e) {
                    throw new XUnitException("Error saving build", e);
                }
            } else {
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
     * @param pipelineTestDetails A {@link PipelineTestDetails} instance containing Pipeline-related additional arguments.
     * @return the test result object
     * @throws XUnitException the plugin exception
     */
    private TestResult getTestResult(final FilePath workspace,
                                     final String junitFilePattern,
                                     final TestResult existingTestResults,
                                     final long buildTime, final long nowMaster,
                                     final PipelineTestDetails pipelineTestDetails)
            throws XUnitException {

        try {
            return workspace.act(new jenkins.SlaveToMasterFileCallable<TestResult>() {

                public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                    final long nowSlave = System.currentTimeMillis();
                    File generatedJunitDir = new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR);
                    //Ignore return value
                    generatedJunitDir.mkdirs();
                    FileSet fs = Util.createFileSet(generatedJunitDir, junitFilePattern);
                    DirectoryScanner ds = fs.getDirectoryScanner();
                    String[] files = ds.getIncludedFiles();

                    if (files.length == 0) {
                        // no test result. Most likely a configuration error or fatal problem
                        return null;

                    }
                    try {
                        if (existingTestResults == null) {
                            return new TestResult(buildTime + (nowSlave - nowMaster), ds, true,
                                    pipelineTestDetails);
                        } else {
                            existingTestResults.parse(buildTime + (nowSlave - nowMaster), ds, pipelineTestDetails);
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
                log.infoConsoleLogger(String.format("Check '%s' threshold.", threshold.getDescriptor().getDisplayName()));
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
            boolean keepJUnitDirectory = false;
            for (TestType tool : types) {
                InputMetric inputMetric = tool.getInputMetric();

                if (dryRun || tool.isDeleteOutputFiles()) {
                    workspace.child(XUnitDefaultValues.GENERATED_JUNIT_DIR + "/" + inputMetric.getToolName()).deleteRecursive();
                } else {
                    //Mark the tool file parent directory to no deletion
                    keepJUnitDirectory = true;
                }
            }
            if (!keepJUnitDirectory) {
                workspace.child(XUnitDefaultValues.GENERATED_JUNIT_DIR).deleteRecursive();
            }
        } catch (IOException ioe) {
            throw new XUnitException("Problem on deletion", ioe);
        } catch (InterruptedException ie) {
            throw new XUnitException("Problem on deletion", ie);
        }
    }

}
