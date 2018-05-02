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
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

/**
 * <a href=
 * "http://docs.adacore.com/live/wave/aunit/html/aunit_cb/aunit_cb.html">AUnit</a>
 * is a unit testing framework of the xUnit family for the Ada language,
 * originally developed by Ed Falis and maintained by AdaCore, distributed
 * together with GNAT.
 */
public class AUnitJunitHudsonTestType extends TestType {

    @DataBoundConstructor
    public AUnitJunitHudsonTestType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Symbol("AUnit")
    @Extension
    public static class DescriptorImpl extends TestTypeDescriptor<AUnitJunitHudsonTestType> {

        public DescriptorImpl() {
            super(AUnitJunitHudsonTestType.class, AUnit.class);
        }

    }

    @Override
    public Object readResolve() {
        return super.readResolve();
    }

}
