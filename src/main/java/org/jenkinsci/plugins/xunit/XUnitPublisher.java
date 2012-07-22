package org.jenkinsci.plugins.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.service.*;
import com.thalesgroup.hudson.plugins.xunit.types.CustomType;
import hudson.*;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import net.sf.json.JSONObject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.lib.dryrun.DryRun;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Class that converting custom reports to Junit reports and records them
 *
 * @author Gregory Boissinot
 */
@SuppressWarnings({"unchecked", "unused"})
public class XUnitPublisher extends Recorder implements DryRun, Serializable {

    public static final String GENERATED_JUNIT_DIR = "generatedJUnitFiles";

    private TestType[] types;

    private XUnitThreshold[] thresholds;

    private int thresholdMode;

    private static final int MODE_NUMBER = 1;
    private static final int MODE_PERCENT = 2;

    public XUnitPublisher(TestType[] types, XUnitThreshold[] thresholds) {
        this.types = types;
        this.thresholds = thresholds;
    }

    public XUnitPublisher(TestType[] types, XUnitThreshold[] thresholds, int thresholdMode) {
        this.types = types;
        this.thresholds = thresholds;
        this.thresholdMode = thresholdMode;
    }

    public TestType[] getTypes() {
        return types;
    }

    public XUnitThreshold[] getThresholds() {
        return thresholds;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        JUnitResultArchiver jUnitResultArchiver = project.getPublishersList().get(JUnitResultArchiver.class);
        if (jUnitResultArchiver == null) {
            return new TestResultProjectAction(project);
        }
        return null;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        return performXUnit(false, build, listener);
    }

    public boolean performDryRun(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            performXUnit(true, build, listener);
        } catch (Throwable t) {
            listener.getLogger().println("[ERROR] - There is an error: " + t.getCause().getMessage());
        }
        //Always exit on success (returned code and status)
        build.setResult(Result.SUCCESS);
        return true;
    }


    private boolean performXUnit(boolean dryRun, AbstractBuild<?, ?> build, BuildListener listener)
            throws IOException, InterruptedException {
        final XUnitLog xUnitLog = getXUnitLogObject(listener);
        try {

            xUnitLog.infoConsoleLogger("Starting to record.");

            boolean noProcessingErrors = performTests(xUnitLog, build, listener);
            if (!noProcessingErrors) {
                build.setResult(Result.FAILURE);
                xUnitLog.infoConsoleLogger("Stopping recording.");
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

    private XUnitReportProcessorService getXUnitReportProcessorServiceObject(final BuildListener listener) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
            }
        }).getInstance(XUnitReportProcessorService.class);
    }

    private boolean performTests(XUnitLog xUnitLog, AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        XUnitReportProcessorService xUnitReportService = getXUnitReportProcessorServiceObject(listener);
        boolean noProcessingErrors = true;
        for (TestType tool : types) {
            xUnitLog.infoConsoleLogger("Processing " + tool.getDescriptor().getDisplayName());
            if (!isEmptyGivenPattern(xUnitReportService, tool)) {
                String expandedPattern = getExpandedResolvedPattern(tool, build, listener);
                XUnitToolInfo xUnitToolInfo = getXUnitToolInfoObject(tool, expandedPattern, build);
                XUnitTransformer xUnitTransformer = getXUnitTransformerObject(xUnitToolInfo, listener);
                boolean resultTransformation = getWorkspace(build).act(xUnitTransformer);
                if (!resultTransformation) {
                    noProcessingErrors = true;
                }
            }
        }
        return noProcessingErrors;
    }

    private boolean isEmptyGivenPattern(XUnitReportProcessorService xUnitReportService, TestType tool) {
        return xUnitReportService.isEmptyPattern(tool.getPattern());
    }

    private String getExpandedResolvedPattern(TestType tool, AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        String newExpandedPattern = tool.getPattern();
        newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
        return Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));
    }

    private XUnitToolInfo getXUnitToolInfoObject(TestType tool, String expandedPattern, AbstractBuild build) {
        return new XUnitToolInfo(
                new FilePath(new File(Hudson.getInstance().getRootDir(), "userContent")),
                tool.getInputMetric(),
                expandedPattern,
                tool.isFaildedIfNotNew(),
                tool.isDeleteOutputFiles(), tool.isStopProcessingIfError(),
                build.getTimeInMillis(),
                (tool instanceof CustomType) ? getWorkspace(build).child(((CustomType) tool).getCustomXSL()) : null);
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
                    FileSet fs = Util.createFileSet(new File(ws, GENERATED_JUNIT_DIR), junitFilePattern);
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
        if (curResult != null && previousResultStep != null) {
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

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    @SuppressWarnings("unused")
    public static final class XUnitDescriptorPublisher extends BuildStepDescriptor<Publisher> {

        public XUnitDescriptorPublisher() {
            super(XUnitPublisher.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.xUnit_PublisherName();
        }

        @Override
        public boolean isApplicable(Class type) {
            return true;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/xunit/help.html";
        }

        public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListXUnitTypeDescriptors() {
            return TestTypeDescriptor.all();
        }

        public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
            return XUnitThresholdDescriptor.all();
        }

        public XUnitThreshold[] getListXUnitThresholdInstance() {
            return new XUnitThreshold[]{
                    new FailedThreshold(),
                    new SkippedThreshold()
            };
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<TestType> types = Descriptor.newInstancesFromHeteroList(
                    req, formData, "tools", getListXUnitTypeDescriptors());
            List<XUnitThreshold> thresholds = Descriptor.newInstancesFromHeteroList(
                    req, formData, "thresholds", getListXUnitThresholdDescriptors());
            int thresholdMode = formData.getInt("thresholdMode");
            return new XUnitPublisher(types.toArray(new TestType[types.size()]), thresholds.toArray(new XUnitThreshold[thresholds.size()]), thresholdMode);
        }
    }

}




