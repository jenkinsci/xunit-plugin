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

import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.util.converter.ConvertException;
import com.thalesgroup.dtkit.util.validator.ValidatorError;
import com.thalesgroup.dtkit.util.validator.ValidatorException;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    public static final String JUNIT_FILE_POSTFIX = ".xml";
    public static final String JUNIT_FILE_PREFIX = "TEST-";

    private final BuildListener listener;

    private final File junitOuputDir;

    private final TestType[] types;

    private final long buildTime;

    public XUnitTransformer(BuildListener listener, File junitOuputDir, long buildTime, TestType[] types) {
        this.junitOuputDir = junitOuputDir;
        this.listener = listener;
        this.buildTime = buildTime;
        this.types = types;
    }


    /**
     * Tests if the pattern is empty
     *
     * @param pattern the given pattern of the current test tool
     * @return true if empty or blank, false otherwise
     */
    private boolean isEmptyPattern(String pattern) {
        return pattern == null || pattern.trim().length() == 0;

    }


    /**
     * Invocation
     *
     * @param ws      the Hudson workspace
     * @param channel the Hudson chanel
     * @return true or false if the convertion fails
     * @throws IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        try {
            boolean isInvoked = false;
            for (TestType tool : types) {
                if (!isEmptyPattern(tool.getPattern())) {
                    isInvoked = true;
                    boolean result = processTool(ws, tool);
                    if (!result) {
                        return result;
                    }
                }
            }

            //None of the test were processed
            if (!isInvoked) {
                String msg = "[ERROR] - No test report files were found. Configuration error?";
                XUnitLog.log(listener, msg);
                return false;
            }
        }
        catch (XUnitException xe) {
            throw new IOException2("Problem on converting into JUnit reports.", xe);
        }


        return true;
    }

    /**
     * /**
     * Gets all reports from the given parent path and the pattern, while
     * filtering out all files that were created before the given time.
     *
     * @param testTool        the current test tool
     * @param buildTime       the build time
     * @param parentPath      parent
     * @param pattern         pattern to search files
     * @param faildedIfNotNew indicated if the tests time need to be checked
     * @return an array of strings
     */
    private List<String> findReports(TestType testTool, long buildTime, File parentPath, String pattern, boolean faildedIfNotNew) {

        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        File baseDir = ds.getBasedir();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = "[ERROR] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + testTool.getDescriptor().getDisplayName() + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + testTool.getDescriptor().getDisplayName() + "'?";
            XUnitLog.log(listener, msg);
            return null;
        }


        //Checks the timestamp for each test file if the UI option is checked (true by default)
        if (faildedIfNotNew) {
            ArrayList<File> oldResults = new ArrayList<File>();
            for (String value : xunitFiles) {
                File reportFile = new File(baseDir, value);
                // if the file was not updated this build, that is a problem
                if (buildTime - 3000 > reportFile.lastModified()) {
                    oldResults.add(reportFile);
                }
            }

            if (!oldResults.isEmpty()) {
                long localTime = System.currentTimeMillis();
                if (localTime < buildTime - 1000) {
                    // build time is in the the future. clock on this slave must be running behind
                    String msg = "[ERROR] - Clock on this slave is out of sync with the master, and therefore \n" +
                            "I can't figure out what test results are new and what are old.\n" +
                            "Please keep the slave clock in sync with the master.";
                    XUnitLog.log(listener, msg);
                    return null;
                }

                String msg = "[ERROR] Test reports were found but not all of them are new. Did all the tests run?\n";
                for (File f : oldResults) {
                    msg += String.format("  * %s is %s old\n", f, Util.getTimeSpanString(buildTime - f.lastModified()));
                }
                XUnitLog.log(listener, msg);
                return null;
            }
        }

        return Arrays.asList(xunitFiles);
    }

    /**
     * Process the conversion of the current test tool
     *
     * @param ws       the Hudson workspace
     * @param testTool the current test tool
     * @return true if the conversion and the validation is OK, false otherwise
     * @throws XUnitException the plugin exception if an error occurs
     */
    private boolean processTool(File ws, TestType testTool) throws XUnitException {
        try {

            //Gets the associated inputMetric object
            InputMetric inputMetric = testTool.getInputMetric();
            if (inputMetric == null) {
                throw new RuntimeException("The associated input metric object to the tool " + testTool + " is null.");
            }

            //Retrieves the pattern
            String curPattern = testTool.getPattern();
            curPattern = curPattern.replaceAll("[\t\r\n]+", " ");
            //curPattern = Util.replaceMacro(curPattern, owner.getEnvironment(listener));

            //Gets all input files matching the user pattern
            List<String> resultFiles = findReports(testTool, buildTime, ws, curPattern, testTool.isFaildedIfNotNew());
            if (resultFiles == null || resultFiles.size() == 0) {
                return false;
            }

            XUnitLog.log(listener, "[" + testTool.getDescriptor().getDisplayName() + "] - Processing " + resultFiles.size() + " files with the pattern '" + testTool.getPattern() + "' relative to '" + ws + "'.");
            for (String resultFileName : resultFiles) {

                File resultFile = new File(ws, resultFileName);

                if (resultFile.length() == 0) {
                    //Ignore the empty result file (some reason)
                    String msg = "[WARNING] - The file '" + resultFile.getPath() + "' is empty. This file has been ignored.";
                    XUnitLog.log(listener, msg);
                    continue;
                }

                //Validates the input file (nom empty)
                if (!inputMetric.validateInputFile(resultFile)) {

                    //Ignores invalid files
                    XUnitLog.log(listener, "[WARNING] - The file '" + resultFile + "' is an invalid file.");
                    for (ValidatorError validatorError : inputMetric.getInputValidationErrors()) {
                        XUnitLog.log(listener, "[WARNING] " + validatorError.toString());
                    }
                    XUnitLog.log(listener, "[WARNING] - The file '" + resultFile + "' has been ignored.");
                    continue;
                }

                // Process the conversion
                File parent = new File(junitOuputDir, inputMetric.getToolName());
                parent.mkdirs();
                if (!parent.exists()) {
                    throw new XUnitException("Can't create " + parent);
                }
                File junitTargetFile = new File(parent, JUNIT_FILE_PREFIX + resultFile.hashCode() + JUNIT_FILE_POSTFIX);
                XUnitLog.log(listener, "[INFO] - Converting '" + resultFile + "' .");
                inputMetric.convert(resultFile, junitTargetFile);

                //Validates the output
                boolean validateOutput = inputMetric.validateOutputFile(junitTargetFile);
                if (!validateOutput) {
                    XUnitLog.log(listener, "[ERROR] - The converted file for the input file '" + resultFile + "' doesn't match the JUnit format");
                    for (ValidatorError validatorError : inputMetric.getOutputValidationErrors()) {
                        XUnitLog.log(listener, "[ERROR] " + validatorError.toString());
                    }
                    return false;
                }
            }

        }
        catch (ValidatorException vae) {
            throw new XUnitException("Validation failed", vae);
        }
        catch (ConvertException ce) {
            throw new XUnitException("Conversion failed", ce);
        }


        return true;
    }

}
