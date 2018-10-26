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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.junit.Assert;
import org.junit.Test;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResult;
import hudson.util.ReflectionUtils;

public class SkippedThresholdTest {

    @Test
    public void mark_build_as_success_when_skipped_tests_not_exceed_skipped_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setFailureThreshold("1");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();

        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, 2);
        setSkippedCount(actualResult, 1);
        
        Result result = skippedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.SUCCESS, result);
    }

    @Test
    public void mark_build_as_failed_when_skipped_tests_exceeds_skipped_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setFailureThreshold("1");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, 2);
        setSkippedCount(actualResult, 2);
        
        Result result = skippedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.FAILURE, result);
    }

    @Test
    public void mark_build_as_unstable_when_skipped_tests_exceeds_skipped_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setUnstableThreshold("1");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, 10);
        setSkippedCount(actualResult, 5);
        
        Result result = skippedThreshold.getResultThresholdNumber(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void mark_build_as_failed_when_skipped_tests_exceeds_skipped_percent_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setFailureThreshold("49");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, 10);
        setSkippedCount(actualResult, 5);
        
        Result result = skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.FAILURE, result);
    }

    @Test
    public void mark_build_as_unstable_when_skipped_tests_exceeds_skipped_percent_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setUnstableThreshold("49");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, 10);
        setSkippedCount(actualResult, 5);
        
        Result result = skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void mark_build_as_success_when_new_tests_not_exceed_skipped_percent_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setUnstableNewThreshold("10");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        int totalTests = 100;

        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, totalTests);
        setSkippedCount(actualResult, 10);

        TestResult previousResult = new TestResult();
        setTotalCount(previousResult, totalTests);
        setSkippedCount(previousResult, 0);

        Result result = skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.SUCCESS, result);
    }

    @Test
    public void mark_build_as_unstable_when_new_tests_exceeds_skipped_percent_threshold() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        skippedThreshold.setUnstableNewThreshold("9");
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();
        
        int totalTests = 100;
        
        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, totalTests);
        setSkippedCount(actualResult, 10);
        
        TestResult previousResult = new TestResult();
        setTotalCount(previousResult, totalTests);
        setSkippedCount(previousResult, 0);
        
        Result result = skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, null);
        Assert.assertEquals(Result.UNSTABLE, result);
    }

    @Test
    public void verify_skipped_percent_on_new_test_is_calculated_correctly() {
        XUnitThreshold skippedThreshold = spy(new SkippedThreshold());
        doReturn(new SkippedThresholdDescriptor()).when(skippedThreshold).getDescriptor();

        int totalTests = 100;

        TestResult actualResult = new TestResult();
        setTotalCount(actualResult, totalTests);
        setSkippedCount(actualResult, 9);

        TestResult previousResult = new TestResult();
        setTotalCount(previousResult, totalTests);
        setSkippedCount(previousResult, 5);

        Result result = skippedThreshold.getResultThresholdPercent(mock(XUnitLog.class), mock(Run.class), actualResult, previousResult);

        Assert.assertEquals(Result.SUCCESS, result);
        verify(skippedThreshold).getResultThresholdPercent(any(XUnitLog.class), eq(9d), eq(4d));
    }

    private void setTotalCount(TestResult result, Object value) {
        Field field = ReflectionUtils.findField(TestResult.class, "totalTests");
        field.setAccessible(true);
        ReflectionUtils.setField(field, result, value);
    }
    private void setSkippedCount(TestResult result, Object value) {
        Field field = ReflectionUtils.findField(TestResult.class, "skippedTestsCounter");
        field.setAccessible(true);
        ReflectionUtils.setField(field, result, value);
    }

}