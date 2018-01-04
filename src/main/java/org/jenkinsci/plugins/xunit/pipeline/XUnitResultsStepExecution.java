package org.jenkinsci.plugins.xunit.pipeline;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.TestResultSummary;
import hudson.tasks.junit.pipeline.JUnitResultsStepExecution;
import hudson.tasks.test.PipelineTestDetails;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.jenkinsci.plugins.xunit.ExtraConfiguration;
import org.jenkinsci.plugins.xunit.XUnitProcessor;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;

import javax.annotation.Nonnull;
import java.util.List;

public class XUnitResultsStepExecution extends SynchronousStepExecution<TestResultSummary> {
    private transient XUnitResultsStep step;

    public XUnitResultsStepExecution(@Nonnull XUnitResultsStep step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected TestResultSummary run() throws Exception {
        FilePath workspace = getContext().get(FilePath.class);
        workspace.mkdirs();
        Run<?,?> run = getContext().get(Run.class);
        TaskListener listener = getContext().get(TaskListener.class);
        FlowNode node = getContext().get(FlowNode.class);

        String nodeId = node.getId();

        if (step.getTools().isEmpty() || step.getThresholds().isEmpty()) {
            throw new AbortException("At least one tool and at least one threshold must be specified for the xunit step.");
        }

        XUnitProcessor xUnitProcessor = new XUnitProcessor(step.getTools().toArray(new TestType[0]),
                step.getThresholds().toArray(new XUnitThreshold[0]),
                step.getThresholdMode(),
                new ExtraConfiguration(step.getTestTimeMargin()));
        List<FlowNode> enclosingBlocks = JUnitResultsStepExecution.getEnclosingStagesAndParallels(node);

        PipelineTestDetails pipelineTestDetails = new PipelineTestDetails();
        pipelineTestDetails.setNodeId(nodeId);
        pipelineTestDetails.setEnclosingBlocks(JUnitResultsStepExecution.getEnclosingBlockIds(enclosingBlocks));
        pipelineTestDetails.setEnclosingBlockNames(JUnitResultsStepExecution.getEnclosingBlockNames(enclosingBlocks));
        TestResultAction action = xUnitProcessor.performAndGetAction(run, pipelineTestDetails, workspace, listener);
        if (action != null) {
            // TODO: Once JENKINS-43995 lands, update this to set the step status instead of the entire build.
            if (action.getResult().getFailCount() > 0) {
                getContext().setResult(Result.UNSTABLE);
            }

            return new TestResultSummary(action.getResult().getResultByNode(nodeId));
        }

        return new TestResultSummary();
    }

    private static final long serialVersionUID = 1L;

}
