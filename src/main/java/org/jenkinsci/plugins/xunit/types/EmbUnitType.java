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
 * <a href="http://embunit.sourceforge.net/embunit">Embedded Unit</a> is unit
 * testing framework for Embedded C System.
 * <p>
 * It's design was copied from JUnit and CUnit and more, and then adapted
 * somewhat for Embedded C System. Embedded Unit does not require std C libs.
 * All objects are allocated to const area.
 * 
 * @author Gregory Boissinot
 */
@SuppressWarnings("serial")
public class EmbUnitType extends AbstractTestType {

    @DataBoundConstructor
    public EmbUnitType(String pattern) {
        super(pattern);
    }

    @Symbol("embUnit")
    @Extension
    public static class EmbUnitTypeDescriptor extends TestTypeDescriptor<EmbUnitType> {

        public EmbUnitTypeDescriptor() {
            super(EmbUnitType.class, EmbUnitInputMetric.class);
        }

    }
}
