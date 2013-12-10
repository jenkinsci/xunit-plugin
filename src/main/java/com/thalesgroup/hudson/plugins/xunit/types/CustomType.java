/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
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

import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

@SuppressWarnings("unused")
public class CustomType extends TestType {

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomType(String pattern, String customXSL, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
        this.customXSL = customXSL;
    }

    @SuppressWarnings("unused")
    public String getCustomXSL() {
        return customXSL;
    }

    /**
     * Call at Hudson startup for backward compatibility
     *
     * @return an new hudson object
     */
    public Object readResolve() {
        return new CustomType(this.getPattern(), customXSL, this.isFailIfNotNew(), this.isDeleteOutputFiles(), this.isStopProcessingIfError());
    }
}