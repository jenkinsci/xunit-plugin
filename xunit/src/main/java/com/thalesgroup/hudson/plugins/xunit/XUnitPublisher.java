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

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

import com.thalesgroup.hudson.plugins.xunit.model.TypeConfig;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitTransformer;

/**
 * Class that converting custom reports to Junit reports and records them
 * 
 * @author Gregory Boissinot
 *   
 */
public class XUnitPublisher extends hudson.tasks.Publisher implements Serializable {
  

    private static final long serialVersionUID = 1L;

    private XUnitConfig config = new XUnitConfig();

    public static final XUnitDescriptor DESCRIPTOR = new XUnitDescriptor();

    @Override
    public Action getProjectAction(hudson.model.Project project) {
         return new TestResultProjectAction(project);
    }

    public XUnitConfig getConfig() {
        return config;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
    	
        boolean result=false;

        //Create the temporary target junit dir
		FilePath junitTargetFilePath = new FilePath(build.getProject().getWorkspace(),"xunitTemp");        
        if (junitTargetFilePath.exists()) {
            junitTargetFilePath.deleteRecursive();
        }
        junitTargetFilePath.mkdirs();

        //Compute module roots
        final FilePath[] moduleRoots= build.getProject().getModuleRoots();
        final boolean multipleModuleRoots= moduleRoots != null && moduleRoots.length > 1;
        final FilePath moduleRoot= multipleModuleRoots ? build.getProject().getWorkspace() : build.getProject().getModuleRoot();

        try{
        	// Archiving tools report files into Junit files
        	XUnitTransformer transformer = new XUnitTransformer(listener,  this.config, junitTargetFilePath);
        	result = moduleRoot.act(transformer);
        	if (!result) {
        		build.setResult(Result.FAILURE);
        	} else {
        		result = recordTestResult(build, listener, junitTargetFilePath, "TEST-*.xml");
        	}
        }
        catch (IOException2 ioe){
        	throw new IOException("xUnithasn't been perfomed correctly.", ioe);
        }
        finally{
            //Detroy temporary target junit dir
            try{
            	junitTargetFilePath.deleteRecursive();        	
            }
            catch (IOException ioe){
            	//ignore            	
            }
            catch (InterruptedException ie){
            	//ignore
            }
        }


        return result;
    }

    @Override
    public XUnitDescriptor getDescriptor() {
        return DESCRIPTOR;        
    }


    /**
     * Record the test results into the current build.
     * @param junitFilePattern
     * @param build
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(final AbstractBuild<?,?> build,
    								 final BuildListener listener,
    								 final FilePath junitTargetFilePath,
    								 final String junitFilePattern)
            throws InterruptedException, IOException {


        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();

            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitTargetFilePath, junitFilePattern, build, existingTestResults, buildTime);

            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }

            if(result.getPassCount()==0 && result.getFailCount()==0){
            	throw new AbortException("None of the test reports contained any result");
            }

        } catch (AbortException e) {
            if(build.getResult()==Result.FAILURE)
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

        if(action.getResult().getFailCount()>0)
        	build.setResult(Result.UNSTABLE);

        return true;
    }

    /**
     * Collect the test results from the files
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

        TestResult result = build.getProject().getWorkspace().act(new FilePath.FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {

                FileSet fs = Util.createFileSet(temporaryJunitDirFile, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();
                if(files.length==0) {
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


    public static final class XUnitDescriptor extends BuildStepDescriptor<Publisher> {

        public XUnitDescriptor() {
            super(XUnitPublisher.class);
            load();            
        }

        @Override
        public String getDisplayName() {
            return com.thalesgroup.hudson.plugins.xunit.util.Messages.XUnit_Publiser_Name();
        }
        
        @Override
        public boolean isApplicable(Class type) {
            return true;
        }        

        @Override
        public String getHelpFile() {
            return "/plugin/xunit/help.html";
        }

        @Override
        public Publisher newInstance(StaplerRequest req) throws FormException {
            XUnitPublisher pub = new XUnitPublisher();

            List<TypeConfig> tools = pub.getConfig().getTestTools();
            for (TypeConfig typeConfig:tools){
                String value = req.getParameter("config."+typeConfig.getName()+".pattern");
                typeConfig.setPattern(value);
            }

            pub.getConfig().getCustomTools().addAll(req.bindParametersToList(TypeConfig.class, "xunit.customentry."));

            return pub;
        }


        public XUnitConfig getConfig() {
            return new XUnitConfig();
        }
    }
}
