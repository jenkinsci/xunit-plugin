/*
The MIT License (MIT)

Copyright (c) 2016, Andrew Bayer, CloudBees Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package org.jenkinsci.plugins.xunit;

import hudson.FilePath;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class XUnitWorkflowTest {

    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    private WorkflowJob getBaseJob(String jobName, String xmlInput) throws Exception {
        WorkflowJob job = jenkinsRule.jenkins.createProject(WorkflowJob.class, jobName);
        FilePath workspace = jenkinsRule.jenkins.getWorkspaceFor(job);
        FilePath input = workspace.child("input.xml");
        input.copyFrom(XUnitWorkflowTest.class.getResourceAsStream(xmlInput));

        return job;
    }

    @Test
    public void xunitBuilderWorkflowStepTest() throws Exception {
        WorkflowJob job = getBaseJob("builder", "/org/jenkinsci/plugins/xunit/types/googletest/testcase2/input.xml");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  step([$class: 'XUnitBuilder', testTimeMargin: '3000', thresholdMode: 1, thresholds: [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '1'], [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']], tools: [[$class: 'GoogleTestType', deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])\n"
                + "}"));

        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
    }

    @Test
    public void xunitPublisherWorkflowStepTest() throws Exception {
        WorkflowJob job = getBaseJob("publisher", "/org/jenkinsci/plugins/xunit/types/googletest/testcase2/input.xml");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  step([$class: 'XUnitPublisher', testTimeMargin: '3000', thresholdMode: 1, thresholds: [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '1'], [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']], tools: [[$class: 'GoogleTestType', deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])\n"
                + "}"));

        jenkinsRule.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
    }

    @Test
    public void xunitBuilderWorkflowStepTestFailIfNoTestRunDisabled() throws Exception {
        WorkflowJob job = getBaseJob("builder-fail-when-no-test-run-disabled", "/org/jenkinsci/plugins/xunit/types/googletest/testcase3/input.xml");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  step([$class: 'XUnitBuilder', testTimeMargin: '3000', failIfNoTestsRun: false, thresholdMode: 1, thresholds: [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '1'], [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']], tools: [[$class: 'GoogleTestType', deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])\n"
                + "}"));

        jenkinsRule.assertBuildStatus(Result.SUCCESS, job.scheduleBuild2(0).get());
    }

    @Test
    public void xunitBuilderWorkflowStepTestFailIfNoTestRunEnabled() throws Exception {
        WorkflowJob job = getBaseJob("builder-fail-when-no-test-run-enabled", "/org/jenkinsci/plugins/xunit/types/googletest/testcase3/input.xml");
        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  step([$class: 'XUnitBuilder', testTimeMargin: '3000', failIfNoTestsRun: true, thresholdMode: 1, thresholds: [[$class: 'FailedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '1'], [$class: 'SkippedThreshold', failureNewThreshold: '', failureThreshold: '', unstableNewThreshold: '', unstableThreshold: '']], tools: [[$class: 'GoogleTestType', deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true]]])\n"
                + "}"));

        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
    }

}
