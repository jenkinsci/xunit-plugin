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

package org.jenkinsci.plugins.xunit.types;

import org.jenkinsci.Symbol;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

/**
 * @author Gregory Boissinot
 */
@SuppressWarnings("serial")
public class CustomType extends AbstractTestType {

    private final String customXSL;

    public CustomType(String pattern, String customXSL, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
        this.customXSL = customXSL;
    }

    @DataBoundConstructor
    public CustomType(String pattern, String customXSL) {
        super(pattern);
        this.customXSL = customXSL;
    }

    public String getCustomXSL() {
        return customXSL;
    }

    @Symbol("Custom")
    @Extension
    public static class CustomInputMetricDescriptor extends TestTypeDescriptor<CustomType> {

        public CustomInputMetricDescriptor() {
            super(CustomType.class, CustomInputMetric.class);
        }

        public boolean isCustomType() {
            return true;
        }
    }

}
