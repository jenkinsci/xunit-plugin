/*
The MIT License (MIT)

Copyright (c) 2017, Nikolas Falco

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