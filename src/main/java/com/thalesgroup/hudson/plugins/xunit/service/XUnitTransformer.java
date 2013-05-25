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
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import org.jenkinsci.plugins.xunit.XUnitPublisher;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class XUnitTransformer extends XUnitService implements FilePath.FileCallable<Boolean>, Serializable {

    private XUnitReportProcessorService xUnitReportProcessorService;

    private XUnitConversionService xUnitConversionService;

    private XUnitValidationService xUnitValidationService;

    private XUnitToolInfo xUnitToolInfo;

    private XUnitLog xUnitLog;

    @Inject
    @SuppressWarnings("unused")
    void load(
            XUnitReportProcessorService xUnitReportProcessorService,
            XUnitConversionService xUnitConversionService,
            XUnitValidationService xUnitValidationService,
            XUnitToolInfo xUnitToolInfo,
            XUnitLog xUnitLog) {
        this.xUnitReportProcessorService = xUnitReportProcessorService;
        this.xUnitValidationService = xUnitValidationService;
        this.xUnitConversionService = xUnitConversionService;
        this.xUnitToolInfo = xUnitToolInfo;
        this.xUnitLog = xUnitLog;
    }

    /**
     * Invocation
     *
     * @param ws      the Hudson workspace
     * @param channel the Hudson chanel
     * @return true or false if the conversion fails
     * @throws IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        try {

            File junitOuputDir = new File(ws, XUnitPublisher.GENERATED_JUNIT_DIR);
            if (!junitOuputDir.exists() && !junitOuputDir.mkdirs()) {
                String msg = "Can't create the path " + junitOuputDir + ". Maybe the directory already exists.";
                xUnitLog.warningConsoleLogger(msg);
                warningSystemLogger(msg);
            }

            String metricName = xUnitToolInfo.getInputMetric().getToolName();

            //Gets all input files matching the user pattern
            List<String> resultFiles = xUnitReportProcessorService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getExpandedPattern());
            int nbTestFiles = resultFiles.size();
            if (nbTestFiles == 0 && xUnitToolInfo.isSkipNoTestFiles()) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "Ignore.";
                xUnitLog.warningConsoleLogger(msg);
                return true;
            }

            if (nbTestFiles == 0) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'. Configuration error?.";
                xUnitLog.errorConsoleLogger(msg);
                errorSystemLogger(msg);
                return false;
            }

            //Checks the timestamp for each test file if the UI option is checked (true by default)
            if (!xUnitReportProcessorService.checkIfFindsFilesNewFiles(xUnitToolInfo, resultFiles, ws)) {
                return false;
            }


            boolean atLeastOneWarningOrError = false;
            for (String curFileName : resultFiles) {

                File curFile = xUnitReportProcessorService.getCurrentReport(ws, curFileName);

                boolean isStopProcessingIfError = xUnitReportProcessorService.isStopProcessingIfError(xUnitToolInfo);

                if (!xUnitValidationService.checkFileIsNotEmpty(curFile)) {
                    //Ignore the empty result file (some reason)
                    String msg = "The result file '" + curFile.getPath() + "' for the metric '" + metricName + "' is empty. The result file has been skipped.";
                    if (isStopProcessingIfError) {
                        xUnitLog.errorConsoleLogger(msg);
                        errorSystemLogger(msg);
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                        errorSystemLogger(msg);
                        continue;
                    }
                }

                //Validates Input file
                if (!xUnitValidationService.validateInputFile(xUnitToolInfo, curFile)) {
                    String msg = "The result file '" + curFile + "' for the metric '" + metricName + "' is not valid. The result file has been skipped.";
                    if (isStopProcessingIfError) {
                        xUnitLog.errorConsoleLogger(msg);
                        errorSystemLogger(msg);
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                        errorSystemLogger(msg);
                        continue;
                    }
                }

                //Convert the input file
                File junitTargetFile = xUnitConversionService.convert(xUnitToolInfo, curFile, ws, junitOuputDir);

                //Validates converted file
                if (!xUnitValidationService.validateOutputFile(xUnitToolInfo, curFile, junitTargetFile)) {
                    String msg = "The converted file for the result file '" + curFile + "' (during conversion process for the metric '" + metricName + "') is not valid. The report file has been skipped.";
                    if (isStopProcessingIfError) {
                        xUnitLog.errorConsoleLogger(msg);
                        errorSystemLogger(msg);
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                        errorSystemLogger(msg);
                        continue;
                    }
                }
            }

            if (atLeastOneWarningOrError) {
                String msg = "There is at least one problem. Check the Jenkins system log for more information. (if you don't have configured yet the system log before, you have to rebuild).";
                xUnitLog.errorConsoleLogger(msg);
                return false;
            }

        } catch (Exception xe) {
            String msg = xe.getMessage();
            if (msg != null) {
                xUnitLog.errorConsoleLogger(msg);
            }
            xe.printStackTrace();
            throw new IOException2("There are some problems during the conversion into JUnit reports: ", xe);
        }

        return true;
    }

}
