/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitConversionService;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitLog;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitReportProcessingService;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitValidationService;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitToolInfo;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitTransformer;
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
public class XUnitPublisher extends Recorder implements Serializable {

    private TestType[] types;

    public XUnitPublisher(TestType[] types) {
        this.types = types;
    }

    public TestType[] getTypes() {
        return types;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        JUnitResultArchiver jUnitResultArchiver = project.getPublishersList().get(JUnitResultArchiver.class);
        if (jUnitResultArchiver == null) {
            return new TestResultProjectAction(project);
        }
        return null;
    }


    /**
     * Gets a Test result object (a new one if any)
     *
     * @param build               the current build
     * @param junitFileDir        the parent output JUnit directory
     * @param junitFilePattern    the JUnit search pattern
     * @param existingTestResults the existing test result
     * @param buildTime           the build time
     * @param nowMaster           the time on master
     * @return the test result object
     * @throws XUnitException the plugin exception
     */
    private TestResult getTestResult(final AbstractBuild<?, ?> build,
                                     final File junitFileDir,
                                     final String junitFilePattern,
                                     final TestResult existingTestResults,
                                     final long buildTime, final long nowMaster)
            throws XUnitException {

        try {
            return build.getWorkspace().act(new FilePath.FileCallable<TestResult>() {

                public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                    final long nowSlave = System.currentTimeMillis();
                    FileSet fs = Util.createFileSet(junitFileDir, junitFilePattern);
                    DirectoryScanner ds = fs.getDirectoryScanner();
                    String[] files = ds.getIncludedFiles();

                    if (files.length == 0) {
                        // no test result. Most likely a configuration error or fatal problem
                        throw new IOException("No test report files were found. Configuration error?");
                    }
                    try {
                        if (existingTestResults == null) {
                            return new TestResult(buildTime + (nowSlave - nowMaster), ds);
                        } else {
                            existingTestResults.parse(buildTime + (nowSlave - nowMaster), ds);
                            return existingTestResults;
                        }
                    }
                    catch (IOException ioe) {
                        throw new IOException(ioe);
                    }
                }

            });

        }
        catch (IOException ioe) {
            throw new XUnitException(ioe);
        }
        catch (InterruptedException ie) {
            throw new XUnitException(ie);
        }


    }

    /**
     * Records the test results into the current build and return the number of tests
     *
     * @param build                the current build object
     * @param listener             the current listener object
     * @param junitTargetDirectory the parent JUnit directory
     * @throws com.thalesgroup.hudson.plugins.xunit.exception.XUnitException
     *          the plugin exception if an error occurs
     */

    private void recordTestResult(AbstractBuild<?, ?> build, BuildListener listener, final File junitTargetDirectory) throws XUnitException {

        TestResultAction existingAction = build.getAction(TestResultAction.class);
        final long buildTime = build.getTimestamp().getTimeInMillis();
        final long nowMaster = System.currentTimeMillis();

        TestResult existingTestResults = null;
        if (existingAction != null) {
            existingTestResults = existingAction.getResult();
        }

        TestResult result = getTestResult(build, junitTargetDirectory, "**/TEST-*.xml", existingTestResults, buildTime, nowMaster);

        TestResultAction action;
        if (existingAction == null) {
            action = new TestResultAction(build, result, listener);
        } else {
            action = existingAction;
            action.setResult(result, listener);
        }

        if (result.getPassCount() == 0 && result.getFailCount() == 0) {
            throw new XUnitException("None of the test reports contained any result");
        }

        if (existingAction == null) {
            build.getActions().add(action);
        }

    }


    @Override
    public boolean perform(final AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {

        final XUnitLog xUnitLog = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(listener);
            }
        }).getInstance(XUnitLog.class);


        xUnitLog.info("Starting to record.");

        try {

            //Creation of the output JUnit directory
            final File junitOuputDir = new File(new FilePath(build.getWorkspace(), "generatedJUnitFiles").toURI());
            if (!junitOuputDir.mkdirs()) {
                xUnitLog.warning("Can't create the path " + junitOuputDir + ". Maybe the directory already exists.");
            }

            XUnitReportProcessingService xUnitReportService = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(BuildListener.class).toInstance(listener);
                }
            }).getInstance(XUnitReportProcessingService.class);

            boolean atLeastOneWarningOrErrorProcess = false;
            for (TestType tool : types) {

                xUnitLog.info("Processing " + tool.getDescriptor().getDisplayName());

                if (!xUnitReportService.isEmptyPattern(tool.getPattern())) {

                    //Retrieves the pattern
                    String newExpandedPattern = tool.getPattern();
                    newExpandedPattern = newExpandedPattern.replaceAll("[\t\r\n]+", " ");
                    newExpandedPattern = Util.replaceMacro(newExpandedPattern, build.getEnvironment(listener));

                    //Build a new build info
                    final XUnitToolInfo xUnitToolInfo = new XUnitToolInfo(tool, junitOuputDir, newExpandedPattern, build.getTimeInMillis());

                    // Archiving tool reports into JUnit files
                    XUnitTransformer xUnitTransformer = Guice.createInjector(new AbstractModule() {
                        @Override
                        protected void configure() {
                            bind(BuildListener.class).toInstance(listener);
                            bind(XUnitToolInfo.class).toInstance(xUnitToolInfo);
                            bind(XUnitValidationService.class).in(Singleton.class);
                            bind(XUnitConversionService.class).in(Singleton.class);
                            bind(XUnitLog.class).in(Singleton.class);
                            bind(XUnitReportProcessingService.class).in(Singleton.class);
                        }
                    }).getInstance(XUnitTransformer.class);

                    boolean resultTransformation = build.getWorkspace().act(xUnitTransformer);
                    if (!resultTransformation) {
                        atLeastOneWarningOrErrorProcess = true;
                    }
                }
            }

            if (atLeastOneWarningOrErrorProcess) {
                build.setResult(Result.FAILURE);
                xUnitLog.info("Stopping recording.");
                return true;
            }

            // Process the record of xUnit
            recordTestResult(build, listener, junitOuputDir);


            //Set the mew build status indicator to unstable if there are failded tests
            TestResultAction testResultAction = build.getAction(TestResultAction.class);
            Result curResult = Result.SUCCESS;
            if (testResultAction.getResult().getFailCount() > 0) {
                curResult = Result.UNSTABLE;
            }


            //Delete generated files if triggered
            boolean resultDeletionOK = build.getWorkspace().act(new FilePath.FileCallable<Boolean>() {
                @SuppressWarnings({"ResultOfMethodCallIgnored"})
                public Boolean invoke(File ws, VirtualChannel channel) throws IOException {

                    boolean keepJUnitDirectory = false;
                    for (TestType tool : types) {
                        boolean keepDirectoryTool = false;
                        InputMetric inputMetric = tool.getInputMetric();
                        //All the files will be under a directory the toolName
                        File toolFileParant = new File(junitOuputDir, inputMetric.getToolName());
                        if (tool.isDeleteOutputFiles()) {
                            File[] files = toolFileParant.listFiles();
                            for (File f : files) {
                                if (!f.delete()) {
                                    xUnitLog.warning("Can't delete the file: " + f);
                                }
                            }
                        } else {
                            //Mark the tool file parent directory to no deletion
                            keepDirectoryTool = true;
                        }
                        if (!keepDirectoryTool) {
                            //Delete the tool parent directory
                            toolFileParant.delete();
                        } else {
                            //Mark the parent JUnit directory to set to true
                            keepJUnitDirectory = true;
                        }
                    }
                    if (!keepJUnitDirectory) {
                        junitOuputDir.delete();
                    }


                    return true;
                }
            });
            if (!resultDeletionOK) {
                build.setResult(Result.FAILURE);
                xUnitLog.info("Stopping recording.");
                return true;
            }

            //Keep the previous status result if worse or equal
            Result previousResult = build.getResult();
            if (previousResult.isWorseOrEqualTo(curResult)) {
                build.setResult(previousResult);
                xUnitLog.info("Stopping recording.");
                return true;
            }

            // Fall back case: Set the build status to new build calculated build status
            xUnitLog.info("Setting the build status to " + curResult);
            build.setResult(curResult);
            xUnitLog.info("Stopping recording.");
            return true;

        }
        catch (IOException ie) {
            xUnitLog.error("The plugin hasn't been performed correctly: " + ie.getCause().getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
        catch (XUnitException xe) {
            xUnitLog.error("The plugin hasn't been performed correctly: " + xe.getMessage());
            build.setResult(Result.FAILURE);
            return false;
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

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<TestType> types = Descriptor.newInstancesFromHeteroList(
                    req, formData, "tools", getListXUnitTypeDescriptors());
            return new XUnitPublisher(types.toArray(new TestType[types.size()]));

        }
    }

}




