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

import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class CppUnitType extends XUnitType {

    public XUnitTypeDescriptor<?> getDescriptor() {
        return new CppUnitType.DescriptorImpl();
    }

    @Deprecated
    public CppUnitType(String pattern) {
        super(pattern);
    }

    public CppUnitType(String pattern, boolean faildedIfNotNew) {
        super(pattern, faildedIfNotNew);
    }

    public String getXsl() {
        return "cppunit-to-junit.xsl";
    }

    @Extension
    public static class DescriptorImpl extends XUnitTypeDescriptor<CppUnitType> {

        public DescriptorImpl() {
            super(CppUnitType.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.xUnit_cppUnitType_label();
        }

        public CppUnitType newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new CppUnitType(formData.getString("pattern"), formData.getBoolean("faildedIfNotNew"));
        }

    }
}
