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

import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

public class XUnitTransformerCallable extends MasterToSlaveFileCallable<Void> {
    private static final long serialVersionUID = -8111801428220302087L;

    private XUnitReportProcessorService xUnitReportProcessorService;
    private XUnitConversionService xUnitConversionService;
    private XUnitValidationService xUnitValidationService;
    private XUnitToolInfo xUnitToolInfo;
    private XUnitLog xUnitLog;
    private String processorId;

    @Inject
    public XUnitTransformerCallable(XUnitReportProcessorService xUnitReportProcessorService,
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
     * @param ws the Hudson workspace
     * @param channel the Hudson chanel
     * @return true or false if the conversion fails
     * @throws IOException in case an error occurs during communication with the
     *         Jenkins node where this callable is executed.
     * @throws TransformerException in case an error occurs when test reports
     *         are transformed into JUnit report.
     */
    @Override
    public Void invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
        File junitOutputDir = new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR);
        if (processorId != null) {
            junitOutputDir = new File(junitOutputDir, processorId);
        }
        FileUtils.forceMkdir(junitOutputDir);

        String metricName = xUnitToolInfo.getInputMetric().getToolName();

        // Gets all input files matching the user pattern
        List<String> resultFiles = xUnitReportProcessorService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getPattern());

        // Checks the timestamp for each test file if the UI option is
        // checked (true by default)
        xUnitReportProcessorService.checkIfFindsFilesNewFiles(xUnitToolInfo, resultFiles, ws);

        boolean isStopProcessingIfError = xUnitReportProcessorService.isStopProcessingIfError(xUnitToolInfo);

        for (String curFileName : resultFiles) {
            File curFile = xUnitReportProcessorService.getCurrentReport(ws, curFileName);

            if (!xUnitValidationService.checkFileIsNotEmpty(curFile)) {
                // Ignore the empty result file (some reason)
                String msg = "The result file '" + curFile.getPath() + "' for the metric '" + metricName + "' is empty. The result file has been skipped.";
                if (isStopProcessingIfError) {
                    throw new EmptyReportFileException(msg);
                } else {
                    xUnitLog.warn(msg);
                    continue;
                }
            }

            // Validates Input file
            if (!xUnitValidationService.validateInputFile(xUnitToolInfo, curFile)) {
                String msg = "The result file '" + curFile + "' for the metric '" + metricName + "' is not valid. The result file has been skipped.";
                if (isStopProcessingIfError) {
                    throw new TransformerException(msg);
                } else {
                    xUnitLog.warn(msg);
                    continue;
                }
            }

            // Convert the input file
            File junitTargetFile = xUnitConversionService.convert(xUnitToolInfo, curFile, junitOutputDir);

            // Validates converted file
            if (!xUnitValidationService.validateOutputFile(xUnitToolInfo, curFile, junitTargetFile)) {
                for (ValidationError validatorError : xUnitToolInfo.getInputMetric().getOutputValidationErrors()) {
                    xUnitLog.error(validatorError.getMessage());
                }
                if (isStopProcessingIfError) {
                    String msg = "The converted file for the result file '" + curFile + "' (during conversion process for the metric '" + metricName + "') is not valid. The report file has been skipped.";
                    throw new TransformerException(msg);
                }
            }
        }
        return null;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

}