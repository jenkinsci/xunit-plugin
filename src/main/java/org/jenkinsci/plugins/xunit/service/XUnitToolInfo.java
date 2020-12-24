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

package org.jenkinsci.plugins.xunit.service;

import java.io.Serializable;

import org.jenkinsci.lib.dtkit.model.InputMetric;

public class XUnitToolInfo implements Serializable {
    private static final long serialVersionUID = 8171724502903934676L;

    private final InputMetric inputMetric;
    private final String pattern;
    private String excludesPattern;
    private final boolean skipNoTestFiles;
    private final boolean failIfNotNew;
    private final boolean deleteOutputFiles;
    private final boolean stopProcessingIfError;
    private boolean followSymlink;
    private final long buildTime;
    private final long testTimeMargin;
    private final long sleepTime;
    public long getSleepTime() {
        return sleepTime;
    }

    private final String xslContent;

    public XUnitToolInfo(InputMetric inputMetric,
                         String pattern,
                         Boolean skipNoTestFiles,
                         boolean failIfNotNew,
                         boolean deleteOutputFiles,
                         boolean stopProcessingIfError,
                         long buildTime,
                         long testTimeMargin,
                         long sleepTime,
                         String xslContent) {
        this(inputMetric, pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError, true, buildTime, testTimeMargin, sleepTime, xslContent);
    }

    public XUnitToolInfo(InputMetric inputMetric,
                         String pattern, Boolean skipNoTestFiles,
                         boolean failIfNotNew, boolean deleteOutputFiles,
                         boolean stopProcessingIfError, boolean followSymlink,
                         long buildTime, long testTimeMargin, long sleepTime, String xslContent) {
        this.inputMetric = inputMetric;
        this.pattern = pattern;
        this.skipNoTestFiles = skipNoTestFiles;
        this.failIfNotNew = failIfNotNew;
        this.deleteOutputFiles = deleteOutputFiles;
        this.stopProcessingIfError = stopProcessingIfError;
        this.followSymlink = followSymlink;
        this.buildTime = buildTime;
        this.testTimeMargin = testTimeMargin;
        this.sleepTime = sleepTime;
        this.xslContent = xslContent;
    }

    public String getXSLFile() {
        return xslContent;
    }

    public InputMetric getInputMetric() {
        return inputMetric;
    }

    public String getPattern() {
        return pattern;
    }

    public long getBuildTime() {
        return buildTime;
    }

    public boolean isSkipNoTestFiles() {
        return skipNoTestFiles;
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

    public long getTestTimeMargin() {
        return testTimeMargin;
    }

    public boolean isFollowSymlink() {
        return followSymlink;
    }

    public void setFollowSymlink(boolean followSymlink) {
        this.followSymlink = followSymlink;
    }

    public String getExcludesPattern() {
        return excludesPattern;
    }

    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }
}
