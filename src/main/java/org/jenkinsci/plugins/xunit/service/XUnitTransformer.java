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
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.jenkinsci.plugins.xunit.NoFoundTestException;
import org.jenkinsci.plugins.xunit.OldTestReportException;
import org.jenkinsci.plugins.xunit.SkipTestException;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

public class XUnitTransformer extends MasterToSlaveFileCallable<Boolean> {
    private static final long serialVersionUID = -8111801428220302087L;

    private XUnitReportProcessorService xUnitReportProcessorService;
    private XUnitConversionService xUnitConversionService;
    private XUnitValidationService xUnitValidationService;
    private XUnitToolInfo xUnitToolInfo;
    private XUnitLog xUnitLog;
    private String processorId;

    @Inject
    public XUnitTransformer(XUnitReportProcessorService xUnitReportProcessorService,
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
     * @throws IOException in case an error occurs during communication with the Jenkins node where this callable is executed.
     */
    @Override
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
        try {
            File junitOutputDir = new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR);
            if (processorId != null) {
                junitOutputDir = new File(junitOutputDir, processorId);
            }
            if (!junitOutputDir.exists() && !junitOutputDir.mkdirs()) {
                String msg = "Can't create the path " + junitOutputDir + ". Maybe the directory already exists.";
                xUnitLog.warn(msg);
            }

            String metricName = xUnitToolInfo.getInputMetric().getToolName();

            //Gets all input files matching the user pattern
            List<String> resultFiles = xUnitReportProcessorService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getExpandedPattern());
            int nbTestFiles = resultFiles.size();
            if (nbTestFiles == 0 && xUnitToolInfo.isSkipNoTestFiles()) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'.";
                xUnitLog.warn(msg);
                throw new SkipTestException();
            }

            if (nbTestFiles == 0) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'. Configuration error?.";
                xUnitLog.error(msg);
                throw new NoFoundTestException();
            }

            //Checks the timestamp for each test file if the UI option is checked (true by default)
            xUnitReportProcessorService.checkIfFindsFilesNewFiles(xUnitToolInfo, resultFiles, ws);

            boolean atLeastOneWarningOrError = false;
            for (String curFileName : resultFiles) {

                File curFile = xUnitReportProcessorService.getCurrentReport(ws, curFileName);

                boolean isStopProcessingIfError = xUnitReportProcessorService.isStopProcessingIfError(xUnitToolInfo);

                if (!xUnitValidationService.checkFileIsNotEmpty(curFile)) {
                    //Ignore the empty result file (some reason)
                    String msg = "The result file '" + curFile.getPath() + "' for the metric '" + metricName + "' is empty. The result file has been skipped.";
                    if (isStopProcessingIfError) {
                        xUnitLog.error(msg);
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                        continue;
                    }
                }

                //Validates Input file
                if (!xUnitValidationService.validateInputFile(xUnitToolInfo, curFile)) {
                    String msg = "The result file '" + curFile + "' for the metric '" + metricName + "' is not valid. The result file has been skipped.";
                    if (isStopProcessingIfError) {
                        xUnitLog.error(msg);
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                        continue;
                    }
                }

                //Convert the input file
                File junitTargetFile = xUnitConversionService.convert(xUnitToolInfo, curFile, junitOutputDir);

                //Validates converted file
                if (!xUnitValidationService.validateOutputFile(xUnitToolInfo, curFile, junitTargetFile)) {
                    String msg = "The converted file for the result file '" + curFile + "' (during conversion process for the metric '" + metricName + "') is not valid. The report file has been skipped.";
                    xUnitLog.error(msg);
                    for (ValidationError validatorError : xUnitToolInfo.getInputMetric().getOutputValidationErrors()) {
                        xUnitLog.error(validatorError.getMessage());
                    }
                    if (isStopProcessingIfError) {
                        return false;
                    } else {
                        atLeastOneWarningOrError = true;
                    }
                }
            }

            if (atLeastOneWarningOrError) {
                String msg = "There is at least one problem. Check the Jenkins system log for more information. (if you don't have configured yet the system log before, you have to rebuild).";
                xUnitLog.error(msg);
                return false;
            }

        } catch (SkipTestException se) {
            throw new SkipTestException();
        } catch (NoFoundTestException se) {
            throw new NoFoundTestException();
        } catch (OldTestReportException oe) {
            throw new OldTestReportException();
        } catch (Exception xe) {
            String msg = xe.getMessage();
            if (msg != null) {
                xUnitLog.error(msg);
            }
            throw new IOException("There are some problems during the conversion into JUnit reports: ", xe);
        }

        return true;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

}