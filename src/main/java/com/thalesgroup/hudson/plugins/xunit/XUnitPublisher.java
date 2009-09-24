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

import hudson.*;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.IOException2;
import hudson.Extension;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Constructor;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitTransformer;
import com.thalesgroup.hudson.plugins.xunit.types.*;
import com.thalesgroup.hudson.plugins.xunit.model.TypeConfig;
import net.sf.json.JSONObject;

/**
 * Class that converting custom reports to Junit reports and records them
 *
 * @author Gregory Boissinot
 */
public class XUnitPublisher extends hudson.tasks.Publisher implements Serializable {


    private static final long serialVersionUID = 1L;

    public XUnitType[] types;

    private XUnitPublisher(XUnitType[] types) {
        this.types = types;
    }


    @Override
    public Action getProjectAction(hudson.model.Project project) {
        return new TestResultProjectAction(project);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        if ((build.getResult().equals(Result.SUCCESS))
                || (build.getResult().equals(Result.UNSTABLE))) {

            //Create the temporary target junit dir
            FilePath junitTargetFilePath = new FilePath(build.getWorkspace(), "xunitTemp");
            if (junitTargetFilePath.exists()) {
                junitTargetFilePath.deleteRecursive();
            }
            junitTargetFilePath.mkdirs();

            try {

                // Archiving tools report files into Junit files
                XUnitTransformer transformer = new XUnitTransformer(listener, build.getTimestamp().getTimeInMillis(), build.getEnvironment(listener), types, junitTargetFilePath);
                boolean result = build.getWorkspace().act(transformer);
                if (!result) {
                    build.setResult(Result.FAILURE);
                } else {
                    result = recordTestResult(build, listener, junitTargetFilePath, "TEST-*.xml");
                }

            }
            catch (IOException2 ioe) {
                throw new IOException2("xUnit hasn't been performed correctly.", ioe);
            }

            finally {
                //Detroy temporary target junit dir
                try {
                    junitTargetFilePath.deleteRecursive();
                }
                catch (IOException ioe) {
                    //ignore
                }
                catch (InterruptedException ie) {
                    //ignore
                }
            }

        } else {
            XUnitLog.log(listener, "Build failed. Publishing xUnit skipped.");
        }


        return true;
    }

    /**
     * Record the test results into the current build.
     *
     * @param build
     * @param listener
     * @param junitFilePattern
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(final AbstractBuild<?, ?> build,
                                     final BuildListener listener,
                                     final FilePath junitTargetFilePath,
                                     final String junitFilePattern)
            throws InterruptedException, IOException {


        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();
            final long nowMaster = System.currentTimeMillis();

            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }

            //TestResult result = getTestResult(junitTargetFilePath, junitFilePattern, build, existingTestResults, buildTime);
            TestResult result = build.getWorkspace().act(
                    new ParseResultCallable(junitTargetFilePath, junitFilePattern, existingTestResults, buildTime, nowMaster));


            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }

            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                throw new AbortException("None of the test reports contained any result");
            }

        } catch (AbortException e) {
            if (build.getResult() == Result.FAILURE)
                // most likely a build failed before it gets to the test phase.
                // don't report confusing error message.
                return true;

            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        if (existingAction == null) {
            build.getActions().add(action);
        }

        if (action.getResult().getFailCount() > 0)
            build.setResult(Result.UNSTABLE);

        return true;
    }

    /**
     * Collect the test results from the files
     *
     * @param junitFilePattern
     * @param build
     * @param existingTestResults existing test results to add results to
     * @param buildTime
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult(final FilePath temporaryJunitFilePath,
                                     final String junitFilePattern,
                                     final AbstractBuild<?, ?> build,
                                     final TestResult existingTestResults,
                                     final long buildTime)
            throws IOException, InterruptedException {


        final File temporaryJunitDirFile = new File(temporaryJunitFilePath.toURI());

        TestResult result = build.getWorkspace().act(new FilePath.FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {

                FileSet fs = Util.createFileSet(temporaryJunitDirFile, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();
                if (files.length == 0) {
                    // no test result. Most likely a configuration error or fatal problem
                    throw new AbortException("No test report files were found. Configuration error?");
                }
                if (existingTestResults == null) {
                    return new TestResult(buildTime, ds);
                } else {
                    existingTestResults.parse(buildTime, ds);
                    return existingTestResults;
                }
            }
        });
        return result;
    }


    private static final class ParseResultCallable implements
            FilePath.FileCallable<TestResult> {

        final FilePath temporaryJunitFilePath;
        final String junitFilePattern;
        final TestResult existingTestResults;
        long buildTime;
        long nowMaster;


        private ParseResultCallable(
                final FilePath temporaryJunitFilePath,
                final String junitFilePattern,
                final TestResult existingTestResults,
                final long buildTime, long nowMaster) {
            this.temporaryJunitFilePath = temporaryJunitFilePath;
            this.junitFilePattern = junitFilePattern;
            this.existingTestResults = existingTestResults;
            this.buildTime = buildTime;
            this.nowMaster = nowMaster;
        }

        public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
            final long nowSlave = System.currentTimeMillis();
            File temporaryJunitDirFile = null;
            try {
                temporaryJunitDirFile = new File(temporaryJunitFilePath.toURI());
            }
            catch (InterruptedException ie) {

            }
            FileSet fs = Util.createFileSet(temporaryJunitDirFile, junitFilePattern);
            DirectoryScanner ds = fs.getDirectoryScanner();
            String[] files = ds.getIncludedFiles();
            if (files.length == 0) {
                // no test result. Most likely a configuration error or fatal problem
                throw new AbortException("No test report files were found. Configuration error?");
            }
            if (existingTestResults == null) {
                return new TestResult(buildTime + (nowSlave - nowMaster), ds);
            } else {
                existingTestResults.parse(buildTime, ds);
                return existingTestResults;
            }


        }
    }


    @Extension
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

        public DescriptorExtensionList<XUnitType, XUnitTypeDescriptor<?>> getListXUnitTypeDescriptors() {
            return XUnitTypeDescriptor.all();
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            List<XUnitType> types = Descriptor.newInstancesFromHeteroList(
                    req, formData, "tools", getListXUnitTypeDescriptors());
            return new XUnitPublisher(types.toArray(new XUnitType[types.size()]));

        }
    }


    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }


    /**
     * Initializes members that were not present in previous versions of this plug-in.
     *
     * @return the created object
     */
    private Object readResolve() {

        try {
            
            if (config != null) {
                HashMap<String, Class> map = new HashMap<String, Class>();
                map.put("phpunit", PHPUnitType.class);
                map.put("aunit", AUnitType.class);
                map.put("cppunit", CppUnitType.class);
                map.put("unittest", UnitTestType.class);
                map.put("nunit", NUnitType.class);
                map.put("mstest", MSTestType.class);
                map.put("boosttest", BoostTestType.class);

                List<XUnitType> xunitTypeList = new ArrayList<XUnitType>();

                types = new XUnitType[0];

                for (TypeConfig typeConfig : config.getTestTools()) {
                    String pattern = typeConfig.getPattern();
                    if (pattern != null && pattern.trim().length()!=0) {
                        //xunitTypeList.add((XUnitType) (map.get(typeConfig.getName()).newInstance()));
                        Constructor<XUnitType> constructor= map.get(typeConfig.getName()).getConstructor(String.class);
                        XUnitType xunitType = constructor.newInstance(pattern);
                        xunitTypeList.add(xunitType);
                    }
                }
                types = xunitTypeList.toArray(new XUnitType[xunitTypeList.size()]);
            }
        }
        catch (Exception e) {

        }

        return this;
    }


    // Backward compatibility. Do not remove.
    // CPPCHECK:OFF
    @Deprecated
    private transient XUnitConfig config;


}




