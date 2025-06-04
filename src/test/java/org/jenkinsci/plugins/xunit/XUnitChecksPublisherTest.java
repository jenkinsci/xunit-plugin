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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@WithJenkins
public class XUnitChecksPublisherTest {

    @TestExtension
    public static final CapturingChecksPublisher.Factory PUBLISHER_FACTORY = new CapturingChecksPublisher.Factory();

    private JenkinsRule rule;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        this.rule = rule;
    }

    @AfterEach
    void tearDown() {
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
    void extractChecksDetailsFailingMultipleTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "someFailed");

        job.setDefinition(new CpsFlowDefinition("""
                stage('first') {
                  node {
                    xunit(testTimeMargin: '3000',
                          thresholdMode: 1,
                          thresholds: [ failed(failureThreshold: '2') ],
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                    )
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        // thresholds are considered for checks
        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("There were test failures"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, failed: 2, passed: 2"));
        assertThat(output.getText().orElseThrow(),
                is("## `modules1.MyTest.test1`\n\n```text\nfailure for test1\n```\n\n\n## `modules1.MyTest.test2`\n\n```text\nerror for test2\n```\n\n\n"));
    }

    @LocalData
    @Test
    void extractChecksDetailsExceedThresholdTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "someFailed");

        job.setDefinition(new CpsFlowDefinition("""
                stage('first') {
                  node {
                    xunit(testTimeMargin: '3000',
                          thresholdMode: 1,
                          thresholds: [ failed(failureThreshold: '0') ],
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                    )
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.FAILURE, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.FAILURE));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("There were test failures"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, failed: 2, passed: 2"));
        assertThat(output.getText().orElseThrow(),
                is("## `modules1.MyTest.test1`\n\n```text\nfailure for test1\n```\n\n\n## `modules1.MyTest.test2`\n\n```text\nerror for test2\n```\n\n\n"));
    }

    @LocalData
    @Test
    void extractChecksDetailsPassingTestResults() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition("""
                \
                node {
                  xunit(testTimeMargin: '3000',
                        skipPublishingChecks: false,
                        tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                  )
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("All tests passed"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, passed: 4"));
        assertThat(output.getText().orElseThrow(), is(""));
    }

    @LocalData
    @Test
    void extractChecksDetailsCustomCheckName() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition("""
                node {
                  xunit(testTimeMargin: '3000',
                        tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)],
                        skipPublishingChecks: false,
                        checksName: 'Custom Checks Name'
                  )
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Custom Checks Name"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("All tests passed"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, passed: 4"));
        assertThat(output.getText().orElseThrow(), is(""));
    }

    @LocalData
    @Test
    void extractChecksDetailsNestedStages() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition("""
                stage('first') { stage('second') {
                  node {
                    xunit(testTimeMargin: '3000',
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                    )
                  }
                }}
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests / first / second"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("All tests passed"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, passed: 4"));
        assertThat(output.getText().orElseThrow(), is(""));
    }

    @Test
    void extractChecksDetailsEmptySuite() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "empty");

        job.setDefinition(new CpsFlowDefinition("""
                stage('first') {
                  node {
                    xunit(testTimeMargin: '3000',
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: true, stopProcessingIfError: true)]
                    )
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests / first"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("No test results found"));
        assertThat(output.getText().orElseThrow(), is(""));
    }

    @LocalData
    @Test
    void extractChecksDetailsAllSkipped() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allSkipped");

        job.setDefinition(new CpsFlowDefinition("""
                stage('all skipped') {
                  node {
                    xunit(testTimeMargin: '3000',
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                    )
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        ChecksDetails checksDetails = getDetail();

        assertThat(checksDetails.getConclusion(), is(ChecksConclusion.SUCCESS));
        assertThat(checksDetails.getName().orElseThrow(), is("Tests / all skipped"));

        ChecksOutput output = checksDetails.getOutput().orElseThrow();

        assertThat(output.getTitle().orElseThrow(), is("All tests were skipped"));
        assertThat(output.getSummary().orElseThrow(), is("total: 4, skipped: 4"));
        assertThat(output.getText().orElseThrow(), is(""));
    }

    @LocalData
    @Test
    void withChecksContext() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");

        job.setDefinition(new CpsFlowDefinition("""
                node {
                  withChecks('With Checks') {
                    xunit(testTimeMargin: '3000',
                          skipPublishingChecks: false,
                          tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                    )
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().orElseThrow(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().orElseThrow(), is("With Checks"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));
    }

    @LocalData
    @Test
    void withChecksContextDeclarative() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");
        job.setDefinition(new CpsFlowDefinition("""
                pipeline {
                  agent any
                  stages {
                    stage('first') {
                      steps {
                        withChecks('With Checks') {
                          xunit(testTimeMargin: '3000',
                                skipPublishingChecks: false,
                                tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)]
                          )
                        }
                      }
                    }
                  }
                }""", true));
        rule.buildAndAssertSuccess(job);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().orElseThrow(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().orElseThrow(), is("With Checks"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));
    }

    @LocalData
    @Test
    void withChecksContextWithCustomName() throws Exception {
        WorkflowJob job = rule.jenkins.createProject(WorkflowJob.class, "allPassing");
        job.setDefinition(new CpsFlowDefinition("""
                stage('first') {
                  node {
                    withChecks('With Checks') {
                      xunit(testTimeMargin: '3000',
                            tools: [JUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: '*.xml', skipNoTestFiles: false, stopProcessingIfError: true)],
                            skipPublishingChecks: false,
                            checksName: 'Custom Checks Name'
                      )
                    }
                  }
                }
                """, true));
        WorkflowRun run = job.scheduleBuild2(0).get();

        rule.assertBuildStatus(Result.SUCCESS, run);

        List<ChecksDetails> checksDetails = getDetails();

        assertThat(checksDetails.size(), is(2));

        assertThat(checksDetails.get(0).getName().orElseThrow(), is("With Checks"));
        assertThat(checksDetails.get(0).getStatus(), is(ChecksStatus.IN_PROGRESS));
        assertThat(checksDetails.get(0).getConclusion(), is(ChecksConclusion.NONE));

        assertThat(checksDetails.get(1).getName().orElseThrow(), is("Custom Checks Name"));
        assertThat(checksDetails.get(1).getStatus(), is(ChecksStatus.COMPLETED));
        assertThat(checksDetails.get(1).getConclusion(), is(ChecksConclusion.SUCCESS));
    }

}
