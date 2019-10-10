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

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.QueryParameter;

import hudson.DescriptorExtensionList;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;

/**
 * @author Gregory Boissinot
 */
public abstract class XUnitThresholdDescriptor<T extends XUnitThreshold> extends Descriptor<XUnitThreshold> {

    public XUnitThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    public static DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> all() {
        return Jenkins.get().getDescriptorList(XUnitThreshold.class);
    }

    public abstract String getUnstableThresholdImgTitle();

    public abstract String getUnstableNewThresholdImgTitle();

    public abstract String getFailureThresholdImgTitle();

    public abstract String getFailureNewThresholdImgTitle();

    public abstract String getThresholdHelpMessage();

    public FormValidation doCheckUnstableThreshold(@CheckForNull @QueryParameter(fixEmpty = true) final String unstableThreshold) {
        return validate(unstableThreshold);
    }

    public FormValidation doCheckUnstableNewThreshold(@CheckForNull @QueryParameter(fixEmpty = true) final String unstableNewThreshold) {
        return validate(unstableNewThreshold);
    }

    public FormValidation doCheckFailureThreshold(@CheckForNull @QueryParameter(fixEmpty = true) final String failureThreshold) {
        return validate(failureThreshold);
    }

    public FormValidation doCheckFailureNewThreshold(@CheckForNull @QueryParameter(fixEmpty = true) final String failureNewThreshold) {
        return validate(failureNewThreshold);
    }

    private FormValidation validate(final String threshold) {
        if (Util.fixEmptyAndTrim(threshold) != null) {
            try {
                Integer.parseInt(threshold);
            } catch (NumberFormatException e) {
                return FormValidation.error(Messages.XUnitThresholdDescriptor_checkThreshold(threshold));
            }
        }
        return FormValidation.ok();
    }

}