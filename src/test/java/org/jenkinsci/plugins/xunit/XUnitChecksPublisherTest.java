/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022, JoC0de
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

import hudson.model.Result;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksOutput;
import io.jenkins.plugins.checks.api.ChecksStatus;
import io.jenkins.plugins.checks.util.CapturingChecksPublisher;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XUnitChecksPublisherTest {

    @Rule
    public final JenkinsRule rule = new JenkinsRule();

    @TestExtension
    public final static CapturingChecksPublisher.Factory PUBLISHER_FACTORY = new CapturingChecksPublisher.Factory();

    @After
    public void clearPublishedChecks() {
        PUBLISHER_FACTORY.getPublishedChecks().clear();
    }

    private ChecksDetails getDetail() {
        List<ChecksDetails> details = getDetails();
        assertThat(details.size(), is(1));
        return details.get(0);
    }

    private List<ChecksDetails> getDetails() {
        return PUBLISHER_FACTORY.getPublishedChecks();
    }

    @LocalData
    @Test
    public void extractChecksDetailsFailingMultipleTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "someFailed");

        job.setDefinition(new CpsFlowDefinition("stage('first') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          thresholdMode: 1,\n"
                + "          thresholds: [ failed(failureThreshold: '2') ],\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        // thresholds are considered for checks
        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("There were test failures"));
        assertThat(output.getSummary().get(), is("total: 4, failed: 2, passed: 2"));
        assertThat(output.getText().get(), is("## `modules1.MyTest.test1`\n\n```text\nfailure for test1\n```\n\n\n## `modules1.MyTest.test2`\n\n```text\nerror for test2\n```\n\n\n"));

    }

    @LocalData
    @Test
    public void extractChecksDetailsExceedThresholdTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "someFailed");

        job.setDefinition(new CpsFlowDefinition("stage('first') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          thresholdMode: 1,\n"
                + "          thresholds: [ failed(failureThreshold: '0') ],\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.FAILURE, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.FAILURE));
        assertThat(checksDetails.getName().get(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("There were test failures"));
        assertThat(output.getSummary().get(), is("total: 4, failed: 2, passed: 2"));
        assertThat(output.getText().get(), is("## `modules1.MyTest.test1`\n\n```text\nfailure for test1\n```\n\n\n## `modules1.MyTest.test2`\n\n```text\nerror for test2\n```\n\n\n"));

    }

    @LocalData
    @Test
    public void extractChecksDetailsPassingTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  xunit(testTimeMargin: '3000',\n"
                + "        skipPublishingChecks: false,\n"
                + "        tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "  )\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("All tests passed"));
        assertThat(output.getSummary().get(), is("total: 4, passed: 4"));
        assertThat(output.getText().get(), is(""));

    }

    @LocalData
    @Test
    public void extractChecksDetailsCustomCheckName() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  xunit(testTimeMargin: '3000',\n"
                + "        tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "        skipPublishingChecks: false,\n"
                + "        checksName: 'Custom Checks Name'\n"
                + "  )\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Custom Checks Name"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("All tests passed"));
        assertThat(output.getSummary().get(), is("total: 4, passed: 4"));
        assertThat(output.getText().get(), is(""));
    }

    @LocalData
    @Test
    public void extractChecksDetailsNestedStages() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition("stage('first') { stage('second') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests / first / second"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("All tests passed"));
        assertThat(output.getSummary().get(), is("total: 4, passed: 4"));
        assertThat(output.getText().get(), is(""));
    }

    @Test
    public void extractChecksDetailsEmptySuite() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "empty");

        job.setDefinition(new CpsFlowDefinition("stage('first') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: true, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("No test results found"));
        assertThat(output.getText().get(), is(""));
    }

    @LocalData
    @Test
    public void extractChecksDetailsAllSkipped() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allSkipped");

        job.setDefinition(new CpsFlowDefinition("stage('all skipped') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests / all skipped"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("There were no test executions"));
        assertThat(output.getSummary().get(), is("total: 4, skipped: 4"));
        assertThat(output.getText().get(), is(""));
    }

    @LocalData
    @Test
    public void extractChecksDetailsNonePassed() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "nonePassed");

        job.setDefinition(new CpsFlowDefinition("stage('none passed') {\n"
                + "  node {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().get(), is("Tests / none passed"));

        ChecksOutput output = checksDetails.getOutput().get();

        assertThat(output.getTitle().get(), is("There were test failures"));
        assertThat(output.getSummary().get(), is("total: 3, failed: 2, skipped: 1"));
        assertThat(output.getText().get(), is("## `modules1.MyTest.test1`\n\n```text\nfailure for test1\n```\n\n\n## `modules1.MyTest.test2`\n\n```text\nerror for test2\n```\n\n\n"));
    }

    @LocalData
    @Test
    public void withChecksContext() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition(""
                + "node {\n"
                + "  withChecks('With Checks') {\n"
                + "    xunit(testTimeMargin: '3000',\n"
                + "          skipPublishingChecks: false,\n"
                + "          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "    )\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().get(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().get(), is("With Checks"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));

    }

    @LocalData
    @Test
    public void withChecksContextDeclarative() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");
        job.setDefinition(new CpsFlowDefinition("pipeline {\n"
                + "  agent any\n"
                + "  stages {\n"
                + "    stage('first') {\n"
                + "      steps {\n"
                + "        withChecks('With Checks') {\n"
                + "          xunit(testTimeMargin: '3000',\n"
                + "                skipPublishingChecks: false,\n"
                + "                tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]\n"
                + "          )\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "}", true));
        rule.buildAndAssertSuccess(job);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().get(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().get(), is("With Checks"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));

    }

    @LocalData
    @Test
    public void withChecksContextWithCustomName() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");
        job.setDefinition(new CpsFlowDefinition("stage('first') {\n"
                + "  node {\n"
                + "    withChecks('With Checks') {\n"
                + "      xunit(testTimeMargin: '3000',\n"
                + "            tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "            skipPublishingChecks: false,\n"
                + "            checksName: 'Custom Checks Name'\n"
                + "      )\n"
                + "    }\n"
                + "  }\n"
                + "}\n", true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().get(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().get(), is("Custom Checks Name"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));

    }

}
