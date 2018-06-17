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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.service.NoTestFoundException;
import org.jenkinsci.plugins.xunit.service.XUnitConversionService;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.jenkinsci.plugins.xunit.service.XUnitReportProcessorService;
import org.jenkinsci.plugins.xunit.service.XUnitToolInfo;
import org.jenkinsci.plugins.xunit.service.XUnitTransformerCallable;
import org.jenkinsci.plugins.xunit.service.XUnitValidationService;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.CustomType;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

import hudson.FilePath;
import hudson.Util;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
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
    private static final class ReportParserCallable extends jenkins.SlaveToMasterFileCallable<TestResult> {
        private static final long serialVersionUID = 1L;

        private final String junitFilePattern;
        private final long buildTime;
        private final long nowMaster;
        private final TestResult existingTestResults;
        private final String processorId;

        public ReportParserCallable(long buildTime,
                                    @Nonnull String junitFilePattern,
                                    long nowMaster,
                                    TestResult existingTestResults,
                                    String processorId) {
            this.buildTime = buildTime;
            this.junitFilePattern = junitFilePattern;
            this.nowMaster = nowMaster;
            this.existingTestResults = existingTestResults;
            this.processorId = processorId;
        }

        @Override
        public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
            final long nowSlave = System.currentTimeMillis();
            File generatedJUnitDir = new File(new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR), processorId);
            FileUtils.forceMkdir(generatedJUnitDir);
            FileSet fs = Util.createFileSet(generatedJUnitDir, junitFilePattern);
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] files = ds.getIncludedFiles();

            if (files.length == 0) {
                // no test result. Most likely a configuration error or fatal
                // problem
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

    public XUnitProcessor(@Nonnull TestType[] tools,
                          @CheckForNull XUnitThreshold[] thresholds,
                          int thresholdMode,
                          @Nonnull ExtraConfiguration extraConfiguration) {
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

    public void process(Run<?, ?> build, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        final XUnitLog xUnitLog = new XUnitLog(listener);
        xUnitLog.info("Starting to record.");

        boolean success = processTestsReport(xUnitLog, build, workspace, listener);

        if (!success) {
            xUnitLog.info("Skipping tests recording.");
            return;
        }

        recordTestResult(build, workspace, listener, xUnitLog);
        processDeletion(workspace, xUnitLog);
        Result result = getBuildStatus(build, xUnitLog);
        if (result != null) {
            xUnitLog.info("Setting the build status to " + result);
            build.setResult(result);
        }
        xUnitLog.info("Stopping recording.");
    }

    private boolean processTestsReport(XUnitLog xUnitLog,
                                       Run<?, ?> build,
                                       FilePath workspace,
                                       TaskListener listener) throws IOException, InterruptedException {
        XUnitReportProcessorService xUnitReportService = new XUnitReportProcessorService(xUnitLog);
        for (TestType tool : tools) {
            xUnitLog.info("Processing " + tool.getDescriptor().getDisplayName());

            if (!isEmptyGivenPattern(xUnitReportService, tool)) {
                String expandedPattern = getExpandedResolvedPattern(tool, build, listener);
                XUnitToolInfo xUnitToolInfo = buildXUnitToolInfo(tool, expandedPattern, build, workspace, listener, xUnitLog);
                XUnitTransformerCallable xUnitTransformer = newXUnitTransformer(xUnitToolInfo, xUnitLog);
                try {
                    workspace.act(xUnitTransformer);
                    return true;
                } catch (NoTestFoundException e) {
                    if (xUnitToolInfo.isSkipNoTestFiles()) {
                        xUnitLog.info(e.getMessage());
                        continue;
                    }
                    throw e;
                }
            }
        }
        return false;
    }

    private boolean isEmptyGivenPattern(XUnitReportProcessorService xUnitReportService, TestType tool) {
        return xUnitReportService.isEmptyPattern(tool.getPattern());
    }

    private String getExpandedResolvedPattern(TestType tool,
                                              Run<?, ?> build,
                                              TaskListener listener) throws IOException, InterruptedException {
        String newExpandedPattern = tool.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        return Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));
    }

    private XUnitToolInfo buildXUnitToolInfo(final TestType tool,
                                             final String pattern,
                                             final Run<?, ?> build,
                                             final FilePath workspace,
                                             final TaskListener listener,
                                             final XUnitLog xUnitLog) throws IOException, InterruptedException {

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

        String xslContent = null;
        if (tool instanceof CustomType) {
            xslContent = getCustomStylesheet(tool, build, workspace, listener);
        } else if (inputMetric instanceof InputMetricXSL) {
            xslContent = getUserStylesheet(tool, xUnitLog);
        }
        return new XUnitToolInfo(inputMetric, pattern, tool.isSkipNoTestFiles(), tool.isFailIfNotNew(), tool.isDeleteOutputFiles(), tool.isStopProcessingIfError(), build.getTimeInMillis(), this.extraConfiguration.getTestTimeMargin(), xslContent);

    }

    private String getUserStylesheet(final TestType tool, final XUnitLog xUnitLog) throws IOException, InterruptedException {
        InputMetricXSL inputMetricXSL = (InputMetricXSL) tool.getInputMetric();
        File userContent = new File(Jenkins.getActiveInstance().getRootDir(), "userContent");
        File userXSLFilePath = new File(userContent, inputMetricXSL.getUserContentXSLDirRelativePath());
        if (!userXSLFilePath.exists()) {
            return null;
        }
        xUnitLog.info("Using the custom user stylesheet in JENKINS_HOME.");
        FilePath xslFile = new FilePath(userXSLFilePath);
        return IOUtils.toString(xslFile.read(), "UTF-8");
    }

    private String getCustomStylesheet(final TestType tool,
                                       final Run<?, ?> build,
                                       final FilePath workspace,
                                       final TaskListener listener) throws IOException, InterruptedException {

        final String customXSLPath = Util.replaceMacro(((CustomType) tool).getCustomXSL(), build.getEnvironment(listener));

        // Try full path
        FilePath customXSLFilePath = new FilePath(new File(customXSLPath));
        if (!customXSLFilePath.exists()) {
            // Try from workspace
            customXSLFilePath = workspace.child(customXSLPath);
        }

        if (!customXSLFilePath.exists()) {
            throw new FileNotFoundException("The given xsl '" + customXSLPath + "'doesn't exist.");
        }
        // FIXME it is on slave
        return IOUtils.toString(customXSLFilePath.read(), "UTF-8");
    }

    private XUnitTransformerCallable newXUnitTransformer(final XUnitToolInfo xUnitToolInfo, final XUnitLog xUnitLog) {
        // TODO why use Guice in this manner it's the quite the same of
        // instantiate classes directly
        XUnitTransformerCallable transformer = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(XUnitToolInfo.class).toInstance(xUnitToolInfo);
                bind(XUnitValidationService.class).in(Singleton.class);
                bind(XUnitConversionService.class).in(Singleton.class);
                bind(XUnitLog.class).toInstance(xUnitLog);
                bind(XUnitReportProcessorService.class).in(Singleton.class);
            }
        }).getInstance(XUnitTransformerCallable.class);
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

    private void recordTestResult(Run<?, ?> build, FilePath workspace, TaskListener listener, XUnitLog xUnitLog) throws IOException, InterruptedException {
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
     * @param workspace the build's workspace
     * @param junitFilePattern the JUnit search pattern
     * @param existingTestResults the existing test result
     * @param buildTime the build time
     * @param nowMaster the time on master
     * @return the test result object
     * @throws InterruptedException
     * @throws IOException
     */
    private TestResult getTestResult(final FilePath workspace,
                                     final String junitFilePattern,
                                     final TestResult existingTestResults,
                                     final long buildTime,
                                     final long nowMaster) throws IOException, InterruptedException {

        return workspace.act(new ReportParserCallable(buildTime, junitFilePattern, nowMaster, existingTestResults, processorId));
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

    private void processDeletion(FilePath workspace, XUnitLog xUnitLog) throws IOException, InterruptedException {
        FilePath generatedJunitDir = workspace.child(XUnitDefaultValues.GENERATED_JUNIT_DIR).child(processorId);

        boolean keepJUnitDirectory = false;
        for (TestType tool : tools) {
            InputMetric inputMetric = tool.getInputMetric();

            if (tool.isDeleteOutputFiles()) {
                generatedJunitDir.child(inputMetric.getToolName()).deleteRecursive();
            } else {
                // Mark the tool file parent directory to no deletion
                keepJUnitDirectory = true;
            }
        }
        if (!keepJUnitDirectory) {
            generatedJunitDir.deleteRecursive();
        }
    }

}
