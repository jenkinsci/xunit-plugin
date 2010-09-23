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

import com.google.inject.Inject;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitConversionService;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitLog;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitReportProcessingService;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitValidationService;
import com.thalesgroup.hudson.plugins.xunit.XUnitPublisher;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private XUnitReportProcessingService xUnitReportProcessingService;

    private XUnitConversionService xUnitConversionService;

    private XUnitValidationService xUnitValidationService;

    private XUnitToolInfo xUnitToolInfo;

    private XUnitLog xUnitLog;

    @Inject
    @SuppressWarnings("unused")
    void load(
            XUnitReportProcessingService xUnitReportProcessingService,
            XUnitConversionService xUnitConversionService,
            XUnitValidationService xUnitValidationService,
            XUnitToolInfo xUnitToolInfo,
            XUnitLog xUnitLog) {
        this.xUnitReportProcessingService = xUnitReportProcessingService;
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
            if (!junitOuputDir.mkdirs()) {
                xUnitLog.warning("Can't create the path " + junitOuputDir + ". Maybe the directory already exists.");
            }

            String metricName = xUnitToolInfo.getToolName();

            //Gets all input files matching the user pattern
            List<String> resultFiles = xUnitReportProcessingService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getExpandedPattern());
            if (resultFiles.size() == 0) {
                xUnitLog.warning("No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'. Configuration error?.");
                return false;
            }

            //Checks the timestamp for each test file if the UI option is checked (true by default)
            if (!xUnitReportProcessingService.checkIfFindsFilesNewFiles(xUnitToolInfo, resultFiles, ws)) {
                return false;
            }

            for (String curFileName : resultFiles) {

                File curFile = xUnitReportProcessingService.getCurrentReport(ws, curFileName);
                

                if (!xUnitValidationService.checkFileIsNotEmpty(curFile)) {
                    //Ignore the empty result file (some reason)
                    String msg = "The result file '" + curFile.getPath() + "' for the metric '" + metricName + "' is empty. The result file has been skipped.";
                    xUnitLog.warning(msg);
                    return false;
                }

                //Validates Input file
                if (!xUnitValidationService.validateInputFile(xUnitToolInfo, curFile)) {
                    xUnitLog.warning("The result file '" + curFile + "' for the metric '" + metricName + "' is not valid. The result file has been skipped.");
                    return false;
                }

                //Convert the input file
                File junitTargetFile = xUnitConversionService.convert(xUnitToolInfo, curFile, ws, junitOuputDir);

                //Validates converted file
                if (!xUnitValidationService.validateOutputFile(xUnitToolInfo, curFile, junitTargetFile)) {
                    xUnitLog.warning("The converted file for the result file '" + curFile + "' (during conversion process for the metric '" + metricName + "') is not valid. The report file has been skipped.");
                    return false;
                }
            }

        }
        catch (Exception xe) {
            throw new IOException2("There are some problems during the conversion into JUnit reports: " + xe.getMessage(), xe);
        }

        return true;
    }

}
