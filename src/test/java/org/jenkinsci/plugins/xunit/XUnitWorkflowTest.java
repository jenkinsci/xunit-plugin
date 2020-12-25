/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Andrew Bayer, CloudBees Inc., Nikolas Falco
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

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import hudson.FilePath;
import hudson.model.Result;

public class XUnitWorkflowTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private WorkflowJob getBaseJob(String jobName) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, jobName);
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath input = workspace.child("input.xml");
        input.copyFrom(XUnitWorkflowTest.class.getResourceAsStream("/org/jenkinsci/plugins/xunit/types/googletest/testcase2/input.xml"));

        return job;
    }

    @Test
    public void xunitPublisherWorkflowStepTest() throws Exception {
        WorkflowJob job = getBaseJob("publisher");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  step([$class: 'XUnitPublisher', testTimeMargin: '3000', thresholdMode: 1, thresholds: [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '1'], [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']], tools: [[$class: 'GoogleTestType', deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])\n"
                + "}", true));

        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
    }

    @Issue("JENKINS-37611")
    @Test
    public void xunitPipelineStepDefinition() throws Exception {
        WorkflowJob job = getBaseJob("readablePublisherPipeline");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  xunit(testTimeMargin: '3000',"
                + "        thresholdMode: 1,"
                + "        thresholds: [ failed(unstableThreshold: '1'), skipped() ],"
                + "        tools: [ GoogleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true) ]"
                + "  )\n"
                + "}", true));

        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
    }

    @LocalData
    @Issue("JENKINS-52202")
    @Test
    public void xunitParallelStep() throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, "JENKINS-52202");

        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  parallel(\n"
                + "    dateiEins: {\n"
                + "        dir('file1') {\n"
                + "          xunit(testTimeMargin: '3000',\n"
                + "                thresholdMode: 1,\n"
                + "                thresholds: [ failed(failureThreshold: '1') ],\n"
                + "                tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'TEST-*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "          )\n"
                + "        }\n"
                + "    },\n"
                + "    dateiZwei: {\n"
                + "        dir('file2') {\n"
                + "          xunit(testTimeMargin: '3000',\n"
                + "                thresholdMode: 1,\n"
                + "                thresholds: [ failed(failureThreshold: '1') ],\n"
                + "                tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'TEST-*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "          )\n"
                + "        }\n"
                + "    }\n"
                + "  )\n"
                + "}", true));
        WorkflowRun run = job.scheduleBuild2(0).get();
        jenkinsRule.assertBuildStatus(Result.SUCCESS, run);
        jenkinsRule.assertLogNotContains(Messages.xUnitProcessor_emptyReport(), run);
    }

}
