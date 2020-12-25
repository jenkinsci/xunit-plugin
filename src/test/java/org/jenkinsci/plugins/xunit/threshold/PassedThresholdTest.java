/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017, Nikolas Falco
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
package org.jenkinsci.plugins.xunit.threshold;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.junit.Assert;
import org.junit.Test;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResultSummary;

public class PassedThresholdTest {

    @Test
    public void mark_build_as_success_if_there_is_a_passed_test() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setFailureThreshold("1");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();

        TestResultSummary actualResult = new TestResultSummary(0, 0, 1, 1);

        Result result = passedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        
        Assert.assertEquals(Result.SUCCESS, result);
    }

    @Test
    public void mark_build_as_failed_if_there_is_no_passed_test() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setFailureThreshold("1");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        TestResultSummary actualResult = new TestResultSummary(0, 1, 0, 1);
        
        Result result = passedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        
        Assert.assertEquals(Result.FAILURE, result);
    }

    @Test
    public void mark_build_as_unstable_if_there_is_no_passed_test() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setUnstableThreshold("1");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        TestResultSummary actualResult = new TestResultSummary(0, 1, 0, 1);

        Result result = passedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void percent_of_test_are_passed() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setUnstableThreshold("100");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        int totalTests = 100;
        TestResultSummary actualResult = new TestResultSummary(0, 10, 90, totalTests);
        
        Result result = passedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void there_is_at_least_a_new_passed_test() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setFailureThreshold("1");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        TestResultSummary actualResult = new TestResultSummary(0, 0, 100, 100);
        TestResultSummary previousResult = new TestResultSummary(0, 0, 99, 99);
        
        Result result = passedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);
        
        Assert.assertEquals(Result.SUCCESS, result);
    }

    @Test
    public void there_is_no_new_passed_test() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setUnstableNewThreshold("1");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();

        TestResultSummary actualResult = new TestResultSummary(0, 1, 99, 100);
        TestResultSummary previousResult = new TestResultSummary(0, 0, 99, 99);
        
        Result result = passedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);
        
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void there_is_a_new_percent_of_test_passed() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setUnstableNewThreshold("5");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        int totalTests = 100;

        TestResultSummary actualResult = new TestResultSummary(0, 0, 99, totalTests);
        TestResultSummary previousResult = new TestResultSummary(0, 10, 90, totalTests);

        Result result = passedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);
        
        Assert.assertEquals(Result.SUCCESS, result);
    }

    @Test
    public void the_new_percent_of_test_passed_less_than_expected() {
        XUnitThreshold passedThreshold = spy(new PassedThreshold());
        passedThreshold.setUnstableNewThreshold("5");
        doReturn(new PassedThresholdDescriptor()).when(passedThreshold).getDescriptor();
        
        int totalTests = 100;
        
        TestResultSummary actualResult = new TestResultSummary(0, 5, 95, totalTests);
        TestResultSummary previousResult = new TestResultSummary(0, 0, 99, 99);
        
        Result result = passedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);
        
        Assert.assertEquals(Result.UNSTABLE, result);
    }

}