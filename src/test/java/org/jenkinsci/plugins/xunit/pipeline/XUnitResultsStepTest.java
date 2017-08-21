package org.jenkinsci.plugins.xunit.pipeline;

import hudson.FilePath;
import hudson.model.Result;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.pipeline.JUnitResultsStepTest;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.types.GoogleTestType;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.Collections;

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
    public void configRoundTrip() throws Exception {
        SnippetizerTester st = new SnippetizerTester(j);
        FailedThreshold failedThreshold = new FailedThreshold();
        failedThreshold.setUnstableThreshold("1");
        XUnitResultsStep step = new XUnitResultsStep(
                Collections.<TestType>singletonList(new GoogleTestType("input.xml", false, false, false, true)),
                Arrays.asList(failedThreshold, new SkippedThreshold())
        );

        st.assertRoundTrip(step, "xunit thresholds: [failed(unstableThreshold: '1'), skipped()], tools: [googleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', skipNoTestFiles: false, stopProcessingIfError: true)]");

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

        FlowNode cunitTestNode = execution.getNode("8");
        assertNotNull(cunitTestNode);

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

    @Test
    public void parallelBranches() throws Exception {
        WorkflowJob job = getBaseJob("parallelBranches");
        job.setDefinition(new CpsFlowDefinition(""
                + "stage('first') {\n"
                + "  node {\n"
                + "    parallel(a: {\n"
                + "      def first = xunit(tools: [googleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                           skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                        thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "      assert first.totalCount == 4\n"
                + "    },\n"
                + "    b: {\n"
                + "      def second = xunit(tools: [cunit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'cunit.xml',\n"
                + "                                       skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                         thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "      assert second.totalCount == 1\n"
                + "    })\n"
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

        JUnitResultsStepTest.assertBranchResults(r, 1, 4, "a", "first");
        JUnitResultsStepTest.assertBranchResults(r, 1, 1, "b", "first");
        JUnitResultsStepTest.assertStageResults(r, 2, 5, "first");
    }
}
