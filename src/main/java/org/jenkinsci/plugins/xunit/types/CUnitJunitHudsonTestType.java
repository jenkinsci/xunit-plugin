/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016, Schneider Electric
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (t&he "Software"), to deal
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
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

/**
 * <a href="http://cunit.sourceforge.net">CUnit</a> is a lightweight system for
 * writing, administering, and running unit tests in C.
 * <p>
 * It provides C programmers a basic testing functionality with a flexible
 * variety of user interfaces.
 */
public class CUnitJunitHudsonTestType extends TestType {

    @DataBoundConstructor
    public CUnitJunitHudsonTestType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Symbol("CUnit")
    @Extension
    public static class DescriptorImpl extends TestTypeDescriptor<CUnitJunitHudsonTestType> {

        public DescriptorImpl() {
            super(CUnitJunitHudsonTestType.class, CUnit.class);
        }

    }

    @Override
    public Object readResolve() {
        return super.readResolve();
    }

}
