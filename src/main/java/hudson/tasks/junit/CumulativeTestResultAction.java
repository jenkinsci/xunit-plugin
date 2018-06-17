package hudson.tasks.junit;

import hudson.model.TaskListener;

public class CumulativeTestResultAction {

    private TestResultAction testResultAction;

    public CumulativeTestResultAction(TestResultAction testResultAction) {
        this.testResultAction = testResultAction;
    }

    public void mergeResult(TestResult additionalResult, TaskListener listener) {
        testResultAction.mergeResult(additionalResult, listener);
    }
}
