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
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;

public class XUnitConversionService implements Serializable {
    private static final long serialVersionUID = 6019846684040298718L;

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
     * @throws ConversionException when conversion errors occurs.
     * @throws IOException when converted reports could not be created or written.
     */
    public File convert(XUnitToolInfo xUnitToolInfo, File inputFile, File junitOutputDirectory) throws IOException  {
        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        File parent = new File(junitOutputDirectory, inputMetric.getToolName());
        FileUtils.forceMkdir(parent);

        File junitTargetFile = getTargetFile(parent);
        try {
            if (inputMetric instanceof InputMetricXSL && xUnitToolInfo.getXSLFile() != null) {
                convertCustomMetric(xUnitToolInfo, inputFile, inputMetric, junitTargetFile);
            } else {
                inputMetric.convert(inputFile, junitTargetFile);
            }
            return junitTargetFile;

        } catch (ConversionException e) {
            Throwable cause = e.getCause();
            throw new TransformerException("Conversion error: " + (cause != null ? cause.getMessage() : e.getMessage()));
        }
    }

    private void convertCustomMetric(XUnitToolInfo xUnitToolInfo, File inputFile, InputMetric inputMetric, File junitTargetFile) {
        InputMetricXSL inputMetricXSL = (InputMetricXSL) inputMetric;
        try {
            inputMetricXSL.convert(inputFile, junitTargetFile, xUnitToolInfo.getXSLFile(), null);
        } catch (Exception xe) {
            xUnitLog.error("Error occurs on the use of the user stylesheet: " + xe.getMessage());
            xUnitLog.info("Fallback on the native embedded stylesheet.");

            inputMetric.convert(inputFile, junitTargetFile);
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

}