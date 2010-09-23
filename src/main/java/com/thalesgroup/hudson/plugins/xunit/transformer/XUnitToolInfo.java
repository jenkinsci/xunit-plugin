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

package com.thalesgroup.hudson.plugins.xunit.transformer;

import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;

import java.io.File;
import java.io.Serializable;


public class XUnitToolInfo implements Serializable {

    private File cusXSLFile;

    private final TestType testType;

//    private final File junitOutputDir;

    private final String expandedPattern;

    private final long buildTime;

    public XUnitToolInfo(TestType testType, String expandedPattern, long buildTime) {
        this.testType = testType;
//        this.junitOutputDir = junitOutputDir;
        this.expandedPattern = expandedPattern;
        this.buildTime = buildTime;
    }

    public void setCusXSLFile(File cusXSLFile) {
        this.cusXSLFile = cusXSLFile;
    }

    public File getCusXSLFile() {
        return cusXSLFile;
    }

    public TestType getTestType() {
        return testType;
    }

//    public File getJunitOutputDir() {
//        return junitOutputDir;
//    }

    public String getExpandedPattern() {
        return expandedPattern;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public String getToolName(){
        return testType.getDescriptor().getDisplayName();
    }
}
