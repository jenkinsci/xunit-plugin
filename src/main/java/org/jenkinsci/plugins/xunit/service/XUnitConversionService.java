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
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;
import org.jenkinsci.plugins.xunit.exception.XUnitException;
import org.jenkinsci.plugins.xunit.types.CustomInputMetric;

import hudson.FilePath;

public class XUnitConversionService implements Serializable {
    private XUnitLog xUnitLog;

    @Inject
    public XUnitConversionService(XUnitLog xUnitLog) {
        this.xUnitLog = xUnitLog;
    }

    /**
     * Converts the inputFile into a JUnit output file
     *
     * @param xUnitToolInfo
     *            the xUnit info wrapper object
     * @param inputFile
     *            the input file to be converted
     * @param junitOutputDirectory
     *            the output parent directory that contains the JUnit output
     *            file
     * @return the converted file
     * @throws org.jenkinsci.plugins.xunit.exception.XUnitException
     *             in case of conversion error.
     */
    public File convert(XUnitToolInfo xUnitToolInfo, File inputFile, File junitOutputDirectory) throws XUnitException {
        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        File parent = new File(junitOutputDirectory, inputMetric.getToolName());
        if (!parent.exists() && !parent.mkdirs()) {
            throw new XUnitException("Can't create " + parent);
        }

        File junitTargetFile = getTargetFile(parent);
        try {
            if (inputMetric instanceof CustomInputMetric) {
                return convertCustomInputMetric(xUnitToolInfo, inputFile, inputMetric, junitTargetFile);
            }

            if (inputMetric instanceof InputMetricXSL) {
                return convertInputMetricXSL(xUnitToolInfo, inputFile, inputMetric, junitTargetFile);
            }

            inputMetric.convert(inputFile, junitTargetFile);
            return junitTargetFile;

        } catch (ConversionException | InterruptedException | IOException e) {
            throw new XUnitException("Conversion error " + e.getMessage(), e);
        }
    }

    /**
     * Provides a unique target file name given an input report.The same input
     * file is intended not be converted twice time.
     * <p>
     * By default java >= 6 provides a version 4 UUID that ensure be unique in
     * the same JVM instance. Since there are no more JVM that works with the
     * same workspace is enough. Version 1 UUID use MAC address but since
     * {@code XUnitProcessor} does not copy slave node report files back to
     * master is not necessary ensure file be globally unique.
     *
     * @param parent the parent folder under which create converted reports
     *
     * @return a node workspace unique file name
     */
    private File getTargetFile(File parent) {
        String uniqueTestName = UUID.randomUUID().toString();
        return new File(parent, XUnitDefaultValues.JUNIT_FILE_PREFIX + uniqueTestName + XUnitDefaultValues.JUNIT_FILE_EXTENSION);
    }

    private File convertCustomInputMetric(XUnitToolInfo xUnitToolInfo, File inputFile, InputMetric inputMetric, File junitTargetFile) {
        CustomInputMetric customInputMetric = (CustomInputMetric) inputMetric;
        customInputMetric.setCustomXSLFile(new File(xUnitToolInfo.getCusXSLFile().getRemote()));
        inputMetric.convert(inputFile, junitTargetFile);
        return junitTargetFile;
    }

    private File convertInputMetricXSL(XUnitToolInfo xUnitToolInfo, File inputFile, InputMetric inputMetric, File junitTargetFile) throws IOException, InterruptedException {
        InputMetricXSL inputMetricXSL = (InputMetricXSL) inputMetric;
        FilePath userXSLFilePath = xUnitToolInfo.getUserContentRoot().child(inputMetricXSL.getUserContentXSLDirRelativePath());

        if (userXSLFilePath.exists()) {
            xUnitLog.info("Using the native embedded stylesheet in JENKINS_HOME.");
            try {
                return convertInputMetricXSLWithUserXSL(inputFile, junitTargetFile, inputMetricXSL, userXSLFilePath);
            } catch (XUnitException xe) {
                xUnitLog.error("Error occurs on the use of the user stylesheet: " + xe.getMessage());
                xUnitLog.info("Trying to use the native embedded stylesheet.");
                inputMetric.convert(inputFile, junitTargetFile);
                return junitTargetFile;
            }
        }

        inputMetric.convert(inputFile, junitTargetFile);
        return junitTargetFile;
    }

    private File convertInputMetricXSLWithUserXSL(File inputFile, File junitTargetFile, InputMetricXSL inputMetricXSL, FilePath userXSLFilePath) throws XUnitException {
        try {
            List<FilePath> filePathList = userXSLFilePath.list();
            if (filePathList.isEmpty()) {
                throw new XUnitException(String.format("There are no XSLs in '%s'", userXSLFilePath.getRemote()));
            }

            for (FilePath file : userXSLFilePath.list()) {
                if (!file.isDirectory()) {
                    inputMetricXSL.convert(inputFile, junitTargetFile, file.readToString(), null);
                    return junitTargetFile;
                }
            }

            throw new XUnitException(String.format("There are no XSLs in '%s'", userXSLFilePath.getRemote()));

        } catch (IOException | InterruptedException e) {
            throw new XUnitException("Error in the use of the user stylesheet", e);
        }
    }

}
