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

import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.util.converter.ConvertException;
import com.thalesgroup.dtkit.util.validator.ValidatorError;
import com.thalesgroup.dtkit.util.validator.ValidatorException;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitToolInfo;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
import hudson.Util;
import hudson.model.BuildListener;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class XUnitService {

    private BuildListener buildListener;

    public XUnitService(BuildListener buildListener) {
        this.buildListener = buildListener;
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
     * @param xUnitToolInfo
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
            String msg = "[" + toolName + "] - [ERROR] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + toolName + "'?";
            XUnitLog.log(buildListener, msg);
        } else {
            String msg = "[" + toolName + "] - [INFO] - " + xunitFiles.length  + " test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + toolName + "'.";
            XUnitLog.log(buildListener, msg);
        }
        return Arrays.asList(xunitFiles);
    }


    /**
     * Validates an input file
     *
     * @param xUnitToolInfo the xUnit tool info wrapper
     * @param inputFile     the current input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation exceptions
     */
    public boolean validateInputFile(XUnitToolInfo xUnitToolInfo, File inputFile) throws XUnitException {

        InputMetric inputMetric = xUnitToolInfo.getTestType().getInputMetric();

        //Validates the input file (nom empty)
        try {
            if (!inputMetric.validateInputFile(inputFile)) {

                //Ignores invalid files
                XUnitLog.log(buildListener, "[WARNING] - The file '" + inputFile + "' is an invalid file.");
                for (ValidatorError validatorError : inputMetric.getInputValidationErrors()) {
                    XUnitLog.log(buildListener, "[WARNING] " + validatorError.toString());
                }

                return false;
            }
        } catch (ValidatorException ve) {
            throw new XUnitException("Validation error on input", ve);
        }
        return true;
    }


    /**
     * Validates the converted file against a JUnit format
     *
     * @param xUnitToolInfo   the xUnit info wrapper object
     * @param inputFile       the input metric from the conversion
     * @param junitTargetFile the converted input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation exceptions
     */
    public boolean validateOutputFile(XUnitToolInfo xUnitToolInfo, File inputFile, File junitTargetFile) throws XUnitException {
        InputMetric inputMetric = xUnitToolInfo.getTestType().getInputMetric();

        try {
            //Validates the output
            boolean validateOutput = inputMetric.validateOutputFile(junitTargetFile);
            if (!validateOutput) {
                XUnitLog.log(buildListener, "[ERROR] - The converted file for the input file '" + inputFile + "' doesn't match the JUnit format");
                for (ValidatorError validatorError : inputMetric.getOutputValidationErrors()) {
                    XUnitLog.log(buildListener, "[ERROR] " + validatorError.toString());
                }
                return false;
            }

        }
        catch (ValidatorException ve) {
            throw new XUnitException("Validation error on output", ve);
        }

        return true;
    }


    /**
     * Convert the inputFile into a JUnit output file
     *
     * @param xUnitToolInfo        the xUnit info wrapper object
     * @param inputFile            the input file to be converted
     * @param junitOutputDirectory the output parent directory that contains the JUnit output file
     * @return the converted file
     * @throws XUnitException an XUnitException is thrown if there is a convertion error.
     */
    public File convert(XUnitToolInfo xUnitToolInfo, File inputFile, File junitOutputDirectory) throws XUnitException {

        InputMetric inputMetric = xUnitToolInfo.getTestType().getInputMetric();

        final String JUNIT_FILE_POSTFIX = ".xml";
        final String JUNIT_FILE_PREFIX = "TEST-";
        File parent = new File(junitOutputDirectory, inputMetric.getToolName());
        parent.mkdirs();
        if (!parent.exists()) {
            throw new XUnitException("Can't create " + parent);
        }
        File junitTargetFile = new File(parent, JUNIT_FILE_PREFIX + inputFile.hashCode() + JUNIT_FILE_POSTFIX);
        XUnitLog.log(buildListener, "[INFO] - Converting '" + inputFile + "' .");
        try {
            inputMetric.convert(inputFile, junitTargetFile);
        } catch (ConvertException ce) {
            throw new XUnitException("Convertion error", ce);
        }

        return junitTargetFile;
    }


    /**
     * Check if all the finds files are new file
     *
     * @param xUnitToolInfo the wrapped object
     * @param files
     * @param workspace
     * @return
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
                    String msg = "[ERROR] - Clock on this slave is out of sync with the master, and therefore \n" +
                            "I can't figure out what test results are new and what are old.\n" +
                            "Please keep the slave clock in sync with the master.";
                    XUnitLog.log(buildListener, msg);
                    return false;
                }

                String msg = "[ERROR] Test reports were found but not all of them are new. Did all the tests run?\n";
                for (File f : oldResults) {
                    msg += String.format("  * %s is %s old\n", f, Util.getTimeSpanString(xUnitToolInfo.getBuildTime() - f.lastModified()));
                }
                XUnitLog.log(buildListener, msg);
                return false;
            }
        }

        return true;
    }

    /**
     * Get a file with the combinason of a root and a name
     *
     * @param root the root path
     * @param name the filename
     * @return the current file
     */
    public File getCurrentFile(File root, String name) {
        return new File(root, name);
    }

}
