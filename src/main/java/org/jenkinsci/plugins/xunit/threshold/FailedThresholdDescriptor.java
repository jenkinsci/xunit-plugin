/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
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

import org.jenkinsci.Symbol;

import hudson.Extension;

/**
 * @author Gregory Boissinot
 */
@Symbol("failed")
@Extension
public class FailedThresholdDescriptor extends XUnitThresholdDescriptor<FailedThreshold> {

    public FailedThresholdDescriptor() {
        super(FailedThreshold.class);
    }

    public FailedThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    @Override
    public String getDisplayName() {
        return Messages.displayName_failedTests();
    }

    @Override
    public String getUnstableThresholdImgTitle() {
        return Messages.unstableThreshold_failedTests();
    }

    @Override
    public String getUnstableNewThresholdImgTitle() {
        return Messages.unstableNewThreshold_failedTests();
    }

    @Override
    public String getFailureThresholdImgTitle() {
        return Messages.failureThreshold_failedTests();
    }

    @Override
    public String getFailureNewThresholdImgTitle() {
        return Messages.failureNewThreshold_failedTests();
    }

    @Override
    public String getThresholdHelpMessage() {
        return Messages.thresholdHelpMessage_failedTests();
    }
}
