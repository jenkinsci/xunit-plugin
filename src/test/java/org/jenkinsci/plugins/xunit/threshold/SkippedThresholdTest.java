package org.jenkinsci.plugins.xunit.threshold;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.junit.Test;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultAction;

public class SkippedThresholdTest {

    @Test
    public void verify_skipped_percent() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        doReturn(Result.SUCCESS).when(skippedThreshold).getResultThresholdPercent(any(XUnitLog.class), anyDouble(), anyDouble());

        int totalTests = 100;

        TestResultAction actualResult = mock(TestResultAction.class);
        when(actualResult.getTotalCount()).thenReturn(totalTests);
        when(actualResult.getSkipCount()).thenReturn(9);

        TestResultAction previousResult = mock(TestResultAction.class);
        when(previousResult.getTotalCount()).thenReturn(totalTests);
        when(previousResult.getSkipCount()).thenReturn(5);

        skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);

        verify(skippedThreshold).getResultThresholdPercent(any(XUnitLog.class), eq(9d), eq(4d));
    }

}