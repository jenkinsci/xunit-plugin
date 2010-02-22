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

import com.thalesgroup.hudson.plugins.xunit.types.BoostTestType;
import com.thalesgroup.hudson.plugins.xunit.types.CustomType;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.Shell;
import hudson.tasks.junit.TestResultAction;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class XUnitPublisherIntegrationTest extends HudsonTestCase {


    /**
     * Must be failed because the file timestamp is not up-to-date
     */
    public void testPeformAnUnstableFailedIfNotNewTest1() throws Exception {

        //Creating a first checkout project and run it
        FreeStyleProject project1 = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project1.setScm(new MultiFileSCM(files));
        FreeStyleBuild buildProject1 = project1.scheduleBuild2(0).get();

        //Creating a second project
        //Using the workspace of the first buidl of the the first project
        FreeStyleProject project2 = createFreeStyleProject();
        project2.setCustomWorkspace(buildProject1.getWorkspace().getRemote());

        //Adding an xUnit publisher
        String pattern = boostFileName;
        project2.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));

        //Made it old
        File boostTestFile = new File(buildProject1.getWorkspace().getRemote(), boostFileName);
        boostTestFile.setLastModified(0);

        //Launch the build
        FreeStyleBuild buildProject2 = project2.scheduleBuild2(0).get();

        //The test must failed due to an out of date of the test result file
        assertBuildStatus(Result.FAILURE, buildProject2);
    }

    /**
     * Must be success because the file timestamo is not checked and all tests are OK
     */
    public void testPeformAnUnstableFailedIfNotNewTest2() throws Exception {

        //Creating a first checkout project and run it
        FreeStyleProject project1 = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project1.setScm(new MultiFileSCM(files));
        FreeStyleBuild buildProject1 = project1.scheduleBuild2(0).get();

        //Creating a second project
        //Using the workspace of the first buidl of the the first project
        FreeStyleProject project2 = createFreeStyleProject();
        project2.setCustomWorkspace(buildProject1.getWorkspace().getRemote());

        //Adding an xUnit publisher
        String pattern = boostFileName;
        project2.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, false, true)}));

        //Made it old
        File boostTestFile = new File(buildProject1.getWorkspace().getRemote(), boostFileName);
        boostTestFile.setLastModified(0);

        //Launch the build
        FreeStyleBuild buildProject2 = project2.scheduleBuild2(0).get();

        //The test must success even if there is an out of date of the test result file
        assertBuildStatus(Result.SUCCESS, buildProject2);
    }


    public void testPeformAnUnstableTest() throws Exception {

        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);

        String boostFileName = "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("touch " + boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //Build status
        assertBuildStatus(Result.UNSTABLE, build);

        //Build log
        StringBuffer expectedLog = new StringBuffer();
        expectedLog.append("[xUnit] Starting to record.\n");
        expectedLog.append("[xUnit] [Boost Test Library] - Use the embedded style sheet.\n");
        expectedLog.append("[xUnit] [Boost Test Library] - Processing 1 files with the pattern '" + pattern + "' relative to '" + build.getWorkspace().getRemote() + "'.\n");
        expectedLog.append("[xUnit] Setting the build status to UNSTABLE\n");
        expectedLog.append("[xUnit] Stopping recording.");
        assertLogContains(expectedLog.toString(), build);
    }

    public void testCustomToolWithCustomStyleSheet() throws Exception {

        FreeStyleProject project = createFreeStyleProject();

        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(2);
        String cpptestResultFileName = "cpptestresult.xml";
        files.add(new SingleFileSCM(cpptestResultFileName, getClass().getResource(cpptestResultFileName)));
        String cpptestStyleSheet = "cpptest-to-junit.xsl";
        files.add(new SingleFileSCM(cpptestStyleSheet, getClass().getResource(cpptestStyleSheet)));
        project.setScm(new MultiFileSCM(files));

        project.getBuildersList().add(new Shell("touch " + cpptestResultFileName));
        String pattern = cpptestResultFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new CustomType(pattern, cpptestStyleSheet, true, true)}));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        //Build status
        assertBuildStatus(Result.UNSTABLE, build);

        //Build log
        StringBuffer expectedLog = new StringBuffer();
        expectedLog.append("[xUnit] Starting to record.\n");
        expectedLog.append("[xUnit] [Custom Tool] - Use the style sheet found into the workspace.\n");
        expectedLog.append("[xUnit] [Custom Tool] - Processing 1 files with the pattern '" + pattern + "' relative to '" + build.getWorkspace().getRemote() + "'.\n");
        expectedLog.append("[xUnit] Setting the build status to UNSTABLE\n");
        expectedLog.append("[xUnit] Stopping recording.");
        assertLogContains(expectedLog.toString(), build);
    }


    public void testPreviousFailedWithFailedTests() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch " + boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getFailCount());
    }


    public void testPreviousFailedWithOnlySuccess() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch " + boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getFailCount());
    }


    public void testPreviousFailedWithErrors() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boosttesterror = "boosttesterror.xml";
        files.add(new SingleFileSCM(boosttesterror, getClass().getResource(boosttesterror)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("wrong command"));
        project.getBuildersList().add(new Shell("touch " + boosttesterror));
        String pattern = boosttesterror;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.FAILURE, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getFailCount());
    }

    public void testPreviousSuccessWithOnlySuccess() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestsuccess.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch " + boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.SUCCESS, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(1, result.getTotalCount());
        assertEquals(0, result.getFailCount());
    }

    public void testPreviousSuccessWithFailedTests() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boostFileName = "boosttestunstable.xml";
        files.add(new SingleFileSCM(boostFileName, getClass().getResource(boostFileName)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch " + boostFileName));
        String pattern = boostFileName;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        //Build status must propagated the FAILURE
        assertBuildStatus(Result.UNSTABLE, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2, result.getTotalCount());
        assertEquals(1, result.getFailCount());
    }

    public void testPreviousSuccessWithErrors() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        List<SingleFileSCM> files = new ArrayList<SingleFileSCM>(1);
        String boosttesterror = "boosttesterror.xml";
        files.add(new SingleFileSCM(boosttesterror, getClass().getResource(boosttesterror)));
        project.setScm(new MultiFileSCM(files));
        project.getBuildersList().add(new Shell("echo SUCCESS"));
        project.getBuildersList().add(new Shell("touch " + boosttesterror));
        String pattern = boosttesterror;
        project.getPublishersList().add(new XUnitPublisher(new XUnitType[]{new BoostTestType(pattern, true, true)}));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertBuildStatus(Result.UNSTABLE, build);
        TestResultAction result = build.getAction(hudson.tasks.junit.TestResultAction.class);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getFailCount());
    }

}
