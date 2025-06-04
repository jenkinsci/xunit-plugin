/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Nikolas Falco
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.types.CppTestJunitHudsonTestType;
import org.jenkinsci.plugins.xunit.types.GoogleTestType;
import org.jenkinsci.plugins.xunit.types.JUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class XUnitPublisherTest {

    public static class SpyXUnitPublisher extends XUnitPublisher {

        public SpyXUnitPublisher(TestType[] tools, XUnitThreshold[] thresholds, int thresholdMode,
                                 String testTimeMargin) {
            super(tools, thresholds, thresholdMode, testTimeMargin);
        }

        @Override
        public void perform(Run<?, ?> build,
                            FilePath workspace,
                            Launcher launcher,
                            TaskListener listener) throws InterruptedException, IOException {
            super.perform(build, workspace, launcher, listener);
            assertEquals(Result.SUCCESS, build.getResult(),
                    "Unexpected build FAILURE setup by the first publisher");
        }

        @SuppressWarnings("rawtypes")
        @Override
        public BuildStepDescriptor getDescriptor() {
            return new BuildStepDescriptor<>(SpyXUnitPublisher.class) {

                @Override
                public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                    return true;
                }
            };
        }
    }

    public static class SpyDataPublisher extends TestDataPublisher {

        private boolean isCalled;

        public boolean isCalled() {
            return this.isCalled;
        }

        @Override
        public TestResultAction.Data contributeTestData(Run<?, ?> run, @NonNull FilePath workspace,
                                                        Launcher launcher, TaskListener listener, TestResult testResult)
                throws IOException, InterruptedException {
            this.isCalled = true;
            return super.contributeTestData(run, workspace, launcher, listener, testResult);
        }
    }

    private JenkinsRule jenkinsRule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkinsRule = rule;
    }

    @LocalData
    @Issue("JENKINS-47194")
    @Test
    void different_build_steps_use_separate_output_folders_and_use_new_instance_of_TestResult_against_validate_thresholds()
            throws Exception {
        FreeStyleProject job = jenkinsRule.jenkins.createProject(FreeStyleProject.class,
                "JENKINS-47194");

        TestType[] tools1 = new TestType[]{
                new JUnitType("module1/*.xml", false, false, false, true)};

        XUnitThreshold threshold1 = new FailedThreshold();
        threshold1.setFailureThreshold("2");
        // this publisher should not fails since the failure threshold is equals
        // to that of the failed counter of the test result
        job.getPublishersList()
                .add(new SpyXUnitPublisher(tools1, new XUnitThreshold[]{threshold1}, 1, "3000"));

        TestType[] tools2 = new TestType[]{
                new JUnitType("module2/*.xml", false, false, false, true)};
        XUnitThreshold threshold2 = new FailedThreshold();
        threshold2.setFailureThreshold("2");
        // this publisher should not fails since the failure threshold is equals
        // to that of the failed counter of the test result. The failed count
        // should not takes in account any results from previous publishers
        job.getPublishersList()
                .add(new XUnitPublisher(tools2, new XUnitThreshold[]{threshold2}, 1, "3000"));

        FreeStyleBuild build = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, build);

        TestResultAction testResultAction = build.getAction(TestResultAction.class);
        assertNotNull(testResultAction);
        assertEquals(9, testResultAction.getTotalCount());
        assertEquals(4, testResultAction.getFailCount());
    }

    @LocalData
    @Issue("JENKINS-52253")
    @Test
    void process_multiple_tools() throws Exception {
        FreeStyleProject job = jenkinsRule.jenkins.createProject(FreeStyleProject.class,
                "JENKINS-52253");

        TestType[] tools = new TestType[]{
                new GoogleTestType("googletest.xml", false, false, true, true),
                new CppTestJunitHudsonTestType("cpptest.xml", false, false, true, true)};

        job.getPublishersList().add(new XUnitPublisher(tools, new XUnitThreshold[]{}, 1, "3000"));

        FreeStyleBuild build = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, build);

        TestResultAction testResultAction = build.getAction(TestResultAction.class);
        assertNotNull(testResultAction);
        assertEquals(5, testResultAction.getTotalCount());
        assertEquals(2, testResultAction.getFailCount());
        assertEquals(1, testResultAction.getSkipCount());
    }

    @LocalData
    @Issue("JENKINS-51645")
    @Test
    void test_publishers_are_run() throws Exception {
        FreeStyleProject job = jenkinsRule.jenkins.createProject(FreeStyleProject.class,
                "JENKINS-51645");

        TestType[] tools = new TestType[]{
                new GoogleTestType("input.xml", false, false, false, true)};

        XUnitPublisher publisher = new XUnitPublisher(tools, new XUnitThreshold[]{}, 1, "3000");

        SpyDataPublisher dataPublisher = new SpyDataPublisher();
        Set<TestDataPublisher> dataPublishers = new HashSet<>(1);
        dataPublishers.add(dataPublisher);

        publisher.setTestDataPublishers(dataPublishers);

        job.getPublishersList().add(publisher);

        job.scheduleBuild2(0).get();
        assertThat(dataPublisher.isCalled, is(true));
    }

}