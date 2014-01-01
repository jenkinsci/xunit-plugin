package org.jenkinsci.plugins.xunit.service;

import com.thalesgroup.dtkit.metrics.model.InputMetric;
import hudson.FilePath;

import java.io.Serializable;


public class XUnitToolInfo implements Serializable {

    private FilePath userContentRoot;

    private final InputMetric inputMetric;

    private final String expandedPattern;

    private final boolean skipNoTestFiles;

    private final boolean failIfNotNew;

    private final boolean deleteOutputFiles;

    private boolean stopProcessingIfError;

    private final long buildTime;

    private final long testTimeMargin;

    private FilePath cusXSLFile;

    public XUnitToolInfo(FilePath userContentRoot, InputMetric inputMetric,
                         String expandedPattern, Boolean skipNoTestFiles, Boolean failIfNotNew,
                         Boolean deleteOutputFiles, Boolean stopProcessingIfError,
                         long buildTime, long testTimeMargin, FilePath cusXSLFile) {
        this.userContentRoot = userContentRoot;
        this.inputMetric = inputMetric;
        this.expandedPattern = expandedPattern;
        this.skipNoTestFiles = skipNoTestFiles;
        this.failIfNotNew = failIfNotNew;
        this.deleteOutputFiles = deleteOutputFiles;
        this.stopProcessingIfError = stopProcessingIfError;
        this.buildTime = buildTime;
        this.testTimeMargin = testTimeMargin;
        this.cusXSLFile = cusXSLFile;
    }

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

    public FilePath getUserContentRoot() {
        return userContentRoot;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }
}
