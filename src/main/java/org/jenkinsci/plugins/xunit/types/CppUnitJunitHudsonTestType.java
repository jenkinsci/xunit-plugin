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
 * <a href="http://cppunit.sourceforge.net/doc/cvs/index.html">CppUnit</a> is a
 * unit testing framework module for the C++ programming language.
 * <p>
 * The first port of JUnit to C++ was done by Michael Feathers. His versions can
 * be found on the XProgramming software page. They are os-specific, so Jerome
 * Lacoste provided a port to Unix/Solaris. His version can be found on the same
 * page. The CppUnit project has combined and built on this work.
 */
@SuppressWarnings("serial")
public class CppUnitJunitHudsonTestType extends AbstractTestType {

    public CppUnitJunitHudsonTestType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @DataBoundConstructor
    public CppUnitJunitHudsonTestType(String pattern) {
        super(pattern);
    }

    @Symbol("CppUnit")
    @Extension
    public static class DescriptorImpl extends TestTypeDescriptor<CppUnitJunitHudsonTestType> {
        public DescriptorImpl() {
            super(CppUnitJunitHudsonTestType.class, CppUnit.class);
        }
    }

}