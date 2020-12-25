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

import java.util.List;

import javax.annotation.Nonnull;

import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.workflow.actions.WarningAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.xunit.ExtraConfiguration;
import org.jenkinsci.plugins.xunit.XUnitProcessor;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResultSummary;
import hudson.tasks.junit.pipeline.JUnitResultsStepExecution;
import hudson.tasks.test.PipelineTestDetails;

public class XUnitResultsStepExecution extends SynchronousNonBlockingStepExecution<TestResultSummary> {
    private transient XUnitResultsStep step;

    public XUnitResultsStepExecution(@Nonnull XUnitResultsStep step, @Nonnull StepContext context) {
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
        Launcher launcher = getContext().get(Launcher.class);

        String nodeId = node.getId();

        if (step.getTools().isEmpty()) {
            throw new AbortException(Messages.XUnitResultsStepExecution_noTool());
        }

        XUnitProcessor xUnitProcessor = new XUnitProcessor(step.getTools().toArray(new TestType[0]),
                step.getThresholds().toArray(new XUnitThreshold[0]),
                step.getThresholdMode(),
                new ExtraConfiguration(step.getTestTimeMarginAsLong(), step.isReduceLog(), step.getSleepTime(), step.isFollowSymlink()));
        List<FlowNode> enclosingBlocks = JUnitResultsStepExecution.getEnclosingStagesAndParallels(node);

        PipelineTestDetails pipelineTestDetails = new PipelineTestDetails();
        pipelineTestDetails.setNodeId(nodeId);
        pipelineTestDetails.setEnclosingBlocks(JUnitResultsStepExecution.getEnclosingBlockIds(enclosingBlocks));
        pipelineTestDetails.setEnclosingBlockNames(JUnitResultsStepExecution.getEnclosingBlockNames(enclosingBlocks));

        TestResultSummary testSummary = xUnitProcessor.process(run, workspace, listener, launcher, step.getTestDataPublishers(), pipelineTestDetails);
        
        Result procResult = xUnitProcessor.getBuildStatus(testSummary, run);
        Result runResult = run.getResult();
        if (runResult == null) {
            runResult = Result.SUCCESS;
        }
        if (procResult.isWorseThan(runResult)) {
            node.addOrReplaceAction(new WarningAction(procResult).withMessage("Some thresholds has been violated"));
            run.setResult(procResult);
        }

        return testSummary;
    }

    private static final long serialVersionUID = 1L;

}
