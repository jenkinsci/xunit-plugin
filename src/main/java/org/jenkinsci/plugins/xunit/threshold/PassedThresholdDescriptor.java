/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Falco Nikolas
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

@Symbol("passed")
@Extension
public class PassedThresholdDescriptor extends XUnitThresholdDescriptor<PassedThreshold> {

    public PassedThresholdDescriptor() {
        super(PassedThreshold.class);
    }

    public PassedThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    @Override
    public String getDisplayName() {
        return Messages.PassedThreshold_displayName();
    }

    @Override
    public String getUnstableThresholdImgTitle() {
        return Messages.PassedThreshold_unstableThreshold();
    }

    @Override
    public String getUnstableNewThresholdImgTitle() {
        return Messages.PassedThreshold_unstableNewThreshold();
    }

    @Override
    public String getFailureThresholdImgTitle() {
        return Messages.PassedThreshold_failureThreshold();
    }

    @Override
    public String getFailureNewThresholdImgTitle() {
        return Messages.PassedThreshold_failureNewThreshold();
    }

    @Override
    public String getThresholdHelpMessage() {
        return Messages.PassedThreshold_thresholdHelpMessage();
    }
}
