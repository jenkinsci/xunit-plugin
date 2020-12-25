/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, CloudBees, inc.
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
package org.jenkinsci.plugins.xunit.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.cps.nodes.StepAtomNode;
import org.jenkinsci.plugins.workflow.cps.nodes.StepStartNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.steps.StageStep;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.types.GoogleTestType;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.common.base.Predicate;

import hudson.FilePath;
import hudson.model.Result;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.PipelineBlockWithTests;

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
        cunit.copyFrom(XUnitResultsStepTest.class.getResourceAsStream("/org/jenkinsci/plugins/xunit/types/cunit/testcase2/input.xml"));

        return job;
    }

    @Test
    public void configRoundTrip() throws Exception {
        SnippetizerTester st = new SnippetizerTester(j);
        FailedThreshold failedThreshold = new FailedThreshold();
        failedThreshold.setUnstableThreshold("1");
        XUnitResultsStep step = new XUnitResultsStep(
                Collections.<TestType>singletonList(new GoogleTestType("input.xml", false, false, false, true)));
        step.setThresholds(Arrays.asList(failedThreshold, new SkippedThreshold()));

        st.assertRoundTrip(step, "xunit thresholds: [failed(unstableThreshold: '1'), skipped()], tools: [GoogleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml', stopProcessingIfError: true)]");

    }

    @Test
    public void singleStep() throws Exception {
        WorkflowJob job = getBaseJob("singleStep");
        job.setDefinition(new CpsFlowDefinition(""
                + "stage('first') {\n"
                + "  node {\n"
                + "    def result = xunit(tools: [GoogleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                          skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                       thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "echo \"total: ${result.totalCount}\"\n"
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

        TestResult nodeTests = action.getResult().getResultByNode(testNode.getId());
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
                + "    def first = xunit(tools: [GoogleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                         skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                      thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "    def second = xunit(tools: [CUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'cunit.xml',\n"
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

        TestResult googleNodeTests = action.getResult().getResultByNode(googleTestNode.getId());
        assertNotNull(googleNodeTests);
        assertEquals(1, googleNodeTests.getSuites().size());
        assertEquals(4, googleNodeTests.getTotalCount());

        TestResult cunitNodeTests = action.getResult().getResultByNode(cunitTestNode.getId());
        assertNotNull(cunitNodeTests);
        assertEquals(1, cunitNodeTests.getSuites().size());
        assertEquals(1, cunitNodeTests.getTotalCount());

        TestResult combinedTests = action.getResult().getResultByNodes(Arrays.asList(googleTestNode.getId(),
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
                + "      def first = xunit(tools: [GoogleTest(deleteOutputFiles: false, failIfNotNew: false, pattern: 'input.xml',\n"
                + "                                           skipNoTestFiles: false, stopProcessingIfError: true)],\n"
                + "                        thresholds: [failed(unstableThreshold: '1'), skipped()])\n"
                + "      assert first.totalCount == 4\n"
                + "    },\n"
                + "    b: {\n"
                + "      def second = xunit(tools: [CUnit(deleteOutputFiles: false, failIfNotNew: false, pattern: 'cunit.xml',\n"
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

        assertBranchResults(r, 1, 4, 2, "a", "first", null);
        assertBranchResults(r, 1, 1, 0, "b", "first", null);
        assertStageResults(r, 2, 5, 2, "first");
    }

    public static void assertBranchResults(WorkflowRun run, int suiteCount, int testCount, int failCount,
            String branchName, String stageName, String innerStageName) {
        FlowExecution execution = run.getExecution();
        DepthFirstScanner scanner = new DepthFirstScanner();
        BlockStartNode aBranch = (BlockStartNode) scanner.findFirstMatch(execution, branchForName(branchName));
        assertNotNull(aBranch);
        TestResult branchResult = assertBlockResults(run, suiteCount, testCount, failCount, aBranch);
        String namePrefix = stageName + " / " + branchName;
        if (innerStageName != null) {
            namePrefix += " / " + innerStageName;
        }
        for (CaseResult c : branchResult.getPassedTests()) {
            assertEquals(namePrefix + " / " + c.getTransformedTestName(), c.getDisplayName());
        }
    }

    public static void assertStageResults(WorkflowRun run, int suiteCount, int testCount, int failCount, String stageName) {
        FlowExecution execution = run.getExecution();
        DepthFirstScanner scanner = new DepthFirstScanner();
        BlockStartNode aStage = (BlockStartNode)scanner.findFirstMatch(execution, stageForName(stageName));
        assertNotNull(aStage);
        assertBlockResults(run, suiteCount, testCount, failCount, aStage);
    }

    private static Predicate<FlowNode> stageForName(final String name) {
        return new Predicate<FlowNode>() {
            @Override
            public boolean apply(@Nullable FlowNode input) {
                return input instanceof StepStartNode &&
                        ((StepStartNode) input).getDescriptor() instanceof StageStep.DescriptorImpl &&
                        input.getDisplayName().equals(name);
            }
        };
    }

    private static TestResult assertBlockResults(WorkflowRun run, int suiteCount, int testCount, int failCount, BlockStartNode blockNode) {
        assertNotNull(blockNode);

        TestResultAction action = run.getAction(TestResultAction.class);
        assertNotNull(action);

        TestResult aResult = action.getResult().getResultForPipelineBlock(blockNode.getId());
        assertNotNull(aResult);

        assertEquals(suiteCount, aResult.getSuites().size());
        assertEquals(testCount, aResult.getTotalCount());
        assertEquals(failCount, aResult.getFailCount());
        if (failCount > 0) {
            MatcherAssert.assertThat(findXUnitSteps(blockNode), CoreMatchers.hasItem(hasWarningAction()));
        } else {
            MatcherAssert.assertThat(findXUnitSteps(blockNode), CoreMatchers.not(CoreMatchers.hasItem(hasWarningAction())));
        }

        PipelineBlockWithTests aBlock = action.getResult().getPipelineBlockWithTests(blockNode.getId());

        assertNotNull(aBlock);
        List<String> aTestNodes = new ArrayList<>(aBlock.nodesWithTests());
        TestResult aFromNodes = action.getResult().getResultByNodes(aTestNodes);
        assertNotNull(aFromNodes);
        assertEquals(aResult.getSuites().size(), aFromNodes.getSuites().size());
        assertEquals(aResult.getFailCount(), aFromNodes.getFailCount());
        assertEquals(aResult.getSkipCount(), aFromNodes.getSkipCount());
        assertEquals(aResult.getPassCount(), aFromNodes.getPassCount());

        return aResult;
    }

    private static List<FlowNode> findXUnitSteps(BlockStartNode blockStart) {
        return new DepthFirstScanner().filteredNodes(
                Collections.singletonList(blockStart.getEndNode()),
                Collections.singletonList(blockStart),
                node -> node instanceof StepAtomNode &&
                        ((StepAtomNode) node).getDescriptor() instanceof XUnitResultsStep.DescriptorImpl
        );
    }

    private static BaseMatcher<FlowNode> hasWarningAction() {
        return new BaseMatcher<FlowNode>() {
            @Override
            public boolean matches(Object item) {
                return item instanceof FlowNode && ((FlowNode) item).getPersistentAction(WarningAction.class) != null;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("a FlowNode with a WarningAction");
            }
        };
    }

    private static Predicate<FlowNode> branchForName(final String name) {
        return new Predicate<FlowNode>() {
            @Override
            public boolean apply(@Nullable FlowNode input) {
                return input != null &&
                        input.getAction(LabelAction.class) != null &&
                        input.getAction(ThreadNameAction.class) != null &&
                        name.equals(input.getAction(ThreadNameAction.class).getThreadName());
            }
        };
    }

}
