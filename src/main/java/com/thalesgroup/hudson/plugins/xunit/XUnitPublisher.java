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

import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitTransformer;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
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
@SuppressWarnings("unchecked")
public class XUnitPublisher extends Recorder implements Serializable {


    private static final long serialVersionUID = 1L;
    private static final String JUNIT_FILE_PATTERN = "**/TEST-*.xml";


    public TestType[] types;

    public XUnitPublisher(TestType[] types) {
        this.types = types;
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
     * @param build   the current build
     * @param junitFileDir   the parent output JUnit directory
     * @param junitFilePattern  the JUnit search pattern
     * @param existingTestResults the existing test result
     * @param buildTime   the build time
     * @param nowMaster   the time on master
     * @return  the test result object
     * @throws XUnitException  the plugin exception
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
     * Record the test results into the current build and return the number of tests
     *
     * @param build the current build object
     * @param listener the current listener object
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

        TestResult result = getTestResult(build, junitTargetDirectory, JUNIT_FILE_PATTERN, existingTestResults, buildTime, nowMaster);

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
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        XUnitLog.log(listener, "Starting to record.");

        Result previousResult = build.getResult();

        try {

            //Creation of the output JUnit directory
            final File junitOuputDir = new File(new FilePath(build.getWorkspace(), "generatedJUnitFiles").toURI());
            if (!junitOuputDir.mkdirs()) {
                XUnitLog.log(listener, "Can't create the path " + junitOuputDir + ". Maybe the directory already exists.");
            }

            // Archiving tools reports into JUnit files
            XUnitTransformer xUnitTransformer = new XUnitTransformer(listener, junitOuputDir, build.getTimeInMillis(), types);
            boolean resultTransformation = build.getWorkspace().act(xUnitTransformer);
            if (!resultTransformation) {
                build.setResult(Result.FAILURE);
                XUnitLog.log(listener, "Stopping recording.");
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
            build.getWorkspace().act(new FilePath.FileCallable<Boolean>() {
                public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                    boolean keep = false;
                    for (TestType tool : types) {
                        InputMetric inputMetric = tool.getInputMetric();
                        File parent = new File(junitOuputDir, inputMetric.getToolName());
                        parent.delete();
                    }
                    if (!keep) {
                        junitOuputDir.delete();
                    }


                    return true;
                }
            });

            //Keep the previous status result if worse or equal
            if (previousResult.isWorseOrEqualTo(curResult)) {
                build.setResult(previousResult);
                XUnitLog.log(listener, "Stopping recording.");
                return true;
            }

            // Fall back case: Set the build status to new build calculated build status
            XUnitLog.log(listener, "Setting the build status to " + curResult);
            build.setResult(curResult);
            XUnitLog.log(listener, "Stopping recording.");
            return true;

        }
        catch (XUnitException xe) {
            XUnitLog.log(listener, "The plugin hasn't been performed correctly: " + xe.getMessage());
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




