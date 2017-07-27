package org.jenkinsci.plugins.xunit.pipeline;

import hudson.FilePath;
import hudson.model.Result;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.pipeline.JUnitResultsStep;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XUnitResultsStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @ClassRule
    public static final BuildWatcher buildWatcher = new BuildWatcher();

    private WorkflowJob getBaseJob(String jobName) throws Exception {
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, jobName);
        FilePath workspace = j.jenkins.getWorkspaceFor(job);
        FilePath input = workspace.child("input.xml");
        input.copyFrom(XUnitResultsStepTest.class.getResourceAsStream("/org/jenkinsci/plugins/xunit/types/googletest/testcase2/input.xml"));
        FilePath cunit = workspace.child("cunit.xml");
        cunit.copyFrom(XUnitResultsStepTest.class.getResourceAsStream("/org/jenkinsci/plugins/xunit/types/cunit/testcase2/testresult.xml"));

        return job;
    }

    @Test
    public void singleStep() throws Exception {
        WorkflowJob job = getBaseJob("singleStep");
        job.setDefinition(new CpsFlowDefinition(""
                + "stage('first') {\n"
                + "  node {\n"
                + "    def result = xunit(tools: [googleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                          skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                       thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "    assert result.totalCount == 4\n"
                + "  }\n"
                + "}\n", true));

        WorkflowRun r = job.scheduleBuild2(0).waitForStart();
        j.assertBuildStatus(Result.UNSTABLE, j.waitForCompletion(r));

        TestResultAction action = r.getAction(TestResultAction.class);
        assertNotNull(action);

        assertEquals(1, action.getResult().getSuites().size());
        assertEquals(4, action.getTotalCount());
        assertEquals(1, action.getSkipCount());
        assertEquals(2, action.getFailCount());

        FlowExecution execution = r.getExecution();

        FlowNode testNode = execution.getNode("7");
        assertNotNull(testNode);

        TagsAction tagsAction = testNode.getAction(TagsAction.class);
        assertNotNull(tagsAction);
        assertEquals("true", tagsAction.getTagValue(JUnitResultsStep.HAS_TEST_RESULTS_TAG_NAME));

        TestResult nodeTests = action.getResult().getResultByRunAndNode(r.getExternalizableId(), testNode.getId());
        assertNotNull(nodeTests);
        assertEquals(1, nodeTests.getSuites().size());
        assertEquals(4, nodeTests.getTotalCount());
    }

    @Test
    public void twoSteps() throws Exception {
        WorkflowJob job = getBaseJob("twoSteps");
        job.setDefinition(new CpsFlowDefinition(""
                + "stage('first') {\n"
                + "  node {\n"
                + "    def first = xunit(tools: [googleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                         skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                      thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "    def second = xunit(tools: [cunit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'cunit.xml',\n"
                + "                                     skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                       thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "    assert first.totalCount == 4\n"
                + "    assert second.totalCount == 1\n"
                + "  }\n"
                + "}\n", true));

        WorkflowRun r = job.scheduleBuild2(0).waitForStart();
        j.assertBuildStatus(Result.UNSTABLE, j.waitForCompletion(r));

        TestResultAction action = r.getAction(TestResultAction.class);
        assertNotNull(action);

        assertEquals(2, action.getResult().getSuites().size());
        assertEquals(5, action.getTotalCount());
        assertEquals(1, action.getSkipCount());
        assertEquals(2, action.getFailCount());

        FlowExecution execution = r.getExecution();

        FlowNode googleTestNode = execution.getNode("7");
        assertNotNull(googleTestNode);

        TagsAction googleTagsAction = googleTestNode.getAction(TagsAction.class);
        assertNotNull(googleTagsAction);
        assertEquals("true", googleTagsAction.getTagValue(JUnitResultsStep.HAS_TEST_RESULTS_TAG_NAME));

        FlowNode cunitTestNode = execution.getNode("8");
        assertNotNull(cunitTestNode);

        TagsAction cunitTagsAction = cunitTestNode.getAction(TagsAction.class);
        assertNotNull(cunitTagsAction);
        assertEquals("true", cunitTagsAction.getTagValue(JUnitResultsStep.HAS_TEST_RESULTS_TAG_NAME));

        TestResult googleNodeTests = action.getResult().getResultByRunAndNode(r.getExternalizableId(), googleTestNode.getId());
        assertNotNull(googleNodeTests);
        assertEquals(1, googleNodeTests.getSuites().size());
        assertEquals(4, googleNodeTests.getTotalCount());

        TestResult cunitNodeTests = action.getResult().getResultByRunAndNode(r.getExternalizableId(), cunitTestNode.getId());
        assertNotNull(cunitNodeTests);
        assertEquals(1, cunitNodeTests.getSuites().size());
        assertEquals(1, cunitNodeTests.getTotalCount());

        TestResult combinedTests = action.getResult().getResultByRunAndNodes(r.getExternalizableId(), Arrays.asList(googleTestNode.getId(),
                cunitTestNode.getId()));
        assertNotNull(combinedTests);
        assertEquals(2, combinedTests.getSuites().size());
        assertEquals(5, combinedTests.getTotalCount());
    }
}
