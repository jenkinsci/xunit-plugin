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

import com.google.inject.Inject;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitToolInfo;
import hudson.Util;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class XUnitReportProcessingService implements Serializable {

    private XUnitLog xUnitLog;

    @Inject
    @SuppressWarnings("unused")
    public void setxUnitLog(XUnitLog xUnitLog) {
        this.xUnitLog = xUnitLog;
    }

    /**
     * Tests if the pattern is empty.
     *
     * @param pattern the given pattern of the current test tool
     * @return true if empty or blank, false otherwise
     */
    public boolean isEmptyPattern(String pattern) {
        return pattern == null || pattern.trim().length() == 0;
    }

    /**
     * Gets all reports from the given parent path and the pattern.
     *
     * @param xUnitToolInfo the xunit tool wrapper
     * @param parentPath    parent
     * @param pattern       pattern to search files
     * @return an array of strings
     */
    public List<String> findReports(XUnitToolInfo xUnitToolInfo, File parentPath, String pattern) {

        String toolName = xUnitToolInfo.getTestType().getDescriptor().getDisplayName();

        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = "[" + toolName + "] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + toolName + "'?";
            xUnitLog.error(msg);
        } else {
            String msg = "[" + toolName + "] - " + xunitFiles.length + " test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'.";
            xUnitLog.info(msg);
        }
        return Arrays.asList(xunitFiles);
    }


    /**
     * Checks if all the finds files are new file
     *
     * @param xUnitToolInfo the wrapped object
     * @param files         the file list
     * @param workspace     the root location of the file list
     * @return true if all files are new, false otherwise
     */
    public boolean checkIfFindsFilesNewFiles(XUnitToolInfo xUnitToolInfo, List<String> files, File workspace) {

        TestType testTool = xUnitToolInfo.getTestType();

        if (testTool.isFaildedIfNotNew()) {
            ArrayList<File> oldResults = new ArrayList<File>();
            for (String value : files) {
                File reportFile = new File(workspace, value);
                // if the file was not updated this build, that is a problem
                if (xUnitToolInfo.getBuildTime() - 3000 > reportFile.lastModified()) {
                    oldResults.add(reportFile);
                }
            }

            if (!oldResults.isEmpty()) {
                long localTime = System.currentTimeMillis();
                if (localTime < xUnitToolInfo.getBuildTime() - 1000) {
                    // build time is in the the future. clock on this slave must be running behind
                    String msg = "Clock on this slave is out of sync with the master, and therefore \n" +
                            "I can't figure out what test results are new and what are old.\n" +
                            "Please keep the slave clock in sync with the master.";
                    xUnitLog.error(msg);
                    return false;
                }

                String msg = "Test reports were found but not all of them are new. Did all the tests run?\n";
                for (File f : oldResults) {
                    msg += String.format("  * %s is %s old\n", f, Util.getTimeSpanString(xUnitToolInfo.getBuildTime() - f.lastModified()));
                }
                xUnitLog.error(msg);
                return false;
            }
        }

        return true;
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
}
