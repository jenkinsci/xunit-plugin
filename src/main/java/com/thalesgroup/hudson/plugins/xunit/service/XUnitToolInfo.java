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

package com.thalesgroup.hudson.plugins.xunit.service;

import com.thalesgroup.dtkit.metrics.model.InputMetric;
import hudson.FilePath;

import java.io.Serializable;


public class XUnitToolInfo implements Serializable {

    private final InputMetric inputMetric;

    private final String expandedPattern;

    private final boolean failIfNotNew;

    private final boolean deleteOutputFiles;

    private boolean stopProcessingIfError;

    private final long buildTime;

    private FilePath cusXSLFile;

    public XUnitToolInfo(InputMetric inputMetric, String expandedPattern, Boolean failIfNotNew, Boolean deleteOutputFiles, Boolean stopProcessingIfError, long buildTime, FilePath cusXSLFile) {
        this.inputMetric = inputMetric;
        this.expandedPattern = expandedPattern;
        this.failIfNotNew = failIfNotNew;
        this.deleteOutputFiles = deleteOutputFiles;
        this.stopProcessingIfError = stopProcessingIfError;
        this.buildTime = buildTime;
        this.cusXSLFile = cusXSLFile;
    }

//    public void setCusXSLFile(File cusXSLFile) {
//        this.cusXSLFile = cusXSLFile;
//    }

    public FilePath getCusXSLFile() {
        return cusXSLFile;
    }

    public InputMetric getInputMetric() {
        return inputMetric;
    }

    public String getExpandedPattern() {
        return expandedPattern;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public boolean isFailIfNotNew() {
        return failIfNotNew;
    }

    public boolean isDeleteOutputFiles() {
        return deleteOutputFiles;
    }

    public boolean isStopProcessingIfError() {
        return stopProcessingIfError;
    }
}
