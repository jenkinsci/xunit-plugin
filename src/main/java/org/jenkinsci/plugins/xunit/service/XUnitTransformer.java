package org.jenkinsci.plugins.xunit.service;

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import org.jenkinsci.plugins.xunit.NoFoundTestException;
import org.jenkinsci.plugins.xunit.OldTestReportException;
import org.jenkinsci.plugins.xunit.SkipTestException;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;

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
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
        try {

            File junitOutputDir = new File(ws, XUnitDefaultValues.GENERATED_JUNIT_DIR);
            if (!junitOutputDir.exists() && !junitOutputDir.mkdirs()) {
                String msg = "Can't create the path " + junitOutputDir + ". Maybe the directory already exists.";
                xUnitLog.warningConsoleLogger(msg);
                warningSystemLogger(msg);
            }

            String metricName = xUnitToolInfo.getInputMetric().getToolName();

            //Gets all input files matching the user pattern
            List<String> resultFiles = xUnitReportProcessorService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getExpandedPattern());
            int nbTestFiles = resultFiles.size();
            if (nbTestFiles == 0 && xUnitToolInfo.isSkipNoTestFiles()) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'.";
                xUnitLog.warningConsoleLogger(msg);
                throw new SkipTestException();
            }

            if (nbTestFiles == 0) {
                String msg = "No test reports found for the metric '" + metricName + "' with the resolved pattern '" + xUnitToolInfo.getExpandedPattern() + "'. Configuration error?.";
                xUnitLog.errorConsoleLogger(msg);
                errorSystemLogger(msg);
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
                File junitTargetFile = xUnitConversionService.convert(xUnitToolInfo, curFile, ws, junitOutputDir);

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
                    }
                }
            }

            if (atLeastOneWarningOrError) {
                String msg = "There is at least one problem. Check the Jenkins system log for more information. (if you don't have configured yet the system log before, you have to rebuild).";
                xUnitLog.errorConsoleLogger(msg);
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
                xUnitLog.errorConsoleLogger(msg);
            }
            xe.printStackTrace();
            throw new IOException2("There are some problems during the conversion into JUnit reports: ", xe);
        }

        return true;
    }

}
