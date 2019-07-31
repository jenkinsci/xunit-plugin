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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.google.inject.Inject;

import hudson.Util;

public class XUnitReportProcessorService implements Serializable {
    private static final long serialVersionUID = 2640258179567685368L;

    private XUnitLog xUnitLog;

    @Inject
    public XUnitReportProcessorService(XUnitLog xUnitLog) {
        this.xUnitLog = xUnitLog;
    }

    /**
     * Tests if the pattern is empty.
     *
     * @param pattern the given pattern of the current test tool
     * @return true if empty or blank, false otherwise
     */
    public boolean isEmptyPattern(String pattern) {
        return Util.fixEmptyAndTrim(pattern) == null;
    }

    /**
     * Gets all reports from the given parent path and the pattern.
     *
     * @param xUnitToolInfo the xunit tool wrapper
     * @param parentPath parent
     * @param pattern pattern to search files
     * @return an array of strings
     * @throws NoTestFoundException when not report files were founded
     */
    public List<String> findReports(XUnitToolInfo xUnitToolInfo, File parentPath, String pattern) throws NoTestFoundException {
        String toolName = xUnitToolInfo.getInputMetric().getLabel();

        FileSet fs = Util.createFileSet(parentPath, pattern);
        fs.setFollowSymlinks(false) // Without this, we could traverse past project boundry if a bad symlink exists
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = Messages.XUnitReportProcessorService_reportsNotFound(toolName, pattern, parentPath);
            throw new NoTestFoundException(msg);
        } else {
            String msg = Messages.XUnitReportProcessorService_reportsFound(toolName, xunitFiles.length, pattern, parentPath);
            xUnitLog.info(msg);
        }
        return Arrays.asList(xunitFiles);
    }

    /**
     * Checks if all the finds files are new file.
     *
     * @param xUnitToolInfo the wrapped object
     * @param files the file list
     * @param workspace the root location of the file list
     * @throws NoNewTestReportException when the report file is not updated
     *         during this build is setup to fail
     */
    public void checkIfFindsFilesNewFiles(XUnitToolInfo xUnitToolInfo, List<String> files, File workspace) throws NoNewTestReportException {

        if (xUnitToolInfo.isFailIfNotNew()) {
            ArrayList<File> oldResults = new ArrayList<>();
            for (String value : files) {
                File reportFile = new File(workspace, value);
                // if the file was not updated this build, that is a problem
                if (xUnitToolInfo.getBuildTime() - xUnitToolInfo.getTestTimeMargin() > reportFile.lastModified()) {
                    oldResults.add(reportFile);
                }
            }

            if (!oldResults.isEmpty()) {
                long localTime = System.currentTimeMillis();
                if (localTime < xUnitToolInfo.getBuildTime() - 1000) {
                    // build time is in the the future. clock on this slave must
                    // be running behind
                    throw new NoNewTestReportException(Messages.XUnitReportProcessorService_clockOutOfSync());
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(String.format("Test reports were found but not all of them are new. Did all the tests run?%n"));
                for (File f : oldResults) {
                    stringBuilder.append(String.format("  * %s is %s old%n", f, Util.getTimeSpanString(xUnitToolInfo.getBuildTime()
                            - f.lastModified())));
                }
                String msg = stringBuilder.toString();
                throw new NoNewTestReportException(msg);
            }
        }
    }

    /**
     * Gets a file from a root file and a name
     *
     * @param root the root path
     * @param name the filename
     * @return the current file
     */
    public File getCurrentReport(File root, String name) {
        return new File(root, name);
    }

    /**
     * Check if we stop the processing for an error
     *
     * @param xUnitToolInfo the wrapped object
     * @return true if the xUnit must stop at the first error
     */
    public boolean isStopProcessingIfError(XUnitToolInfo xUnitToolInfo) {
        return xUnitToolInfo.isStopProcessingIfError();
    }
}
