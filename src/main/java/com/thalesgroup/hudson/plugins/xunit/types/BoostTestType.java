/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit.types;

import com.thalesgroup.hudson.library.tusarconversion.TestsTools;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class BoostTestType extends XUnitType {


    @DataBoundConstructor
    public BoostTestType(String pattern, boolean faildedIfNotNew, boolean deleteJUnitFiles) {
        super(TestsTools.BOOSTTEST, pattern, faildedIfNotNew, deleteJUnitFiles);
    }

    public XUnitTypeDescriptor<?> getDescriptor() {
        return new BoostTestType.DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends XUnitTypeDescriptor<BoostTestType> {

        public DescriptorImpl() {
            super(BoostTestType.class);
        }

        @Override
        public String getDisplayName() {
            return TestsTools.BOOSTTEST.getLabel();
        }

        public String getId() {
            return "boosttest";
        }


    }
}
