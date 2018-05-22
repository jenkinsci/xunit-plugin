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

import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.jenkinsci.lib.dtkit.util.validator.ValidationException;
import org.jenkinsci.plugins.xunit.exception.XUnitException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;

public class XUnitValidationService implements Serializable {

    private XUnitLog xUnitLog;

    @Inject
    public XUnitValidationService(XUnitLog xUnitLog) {
        this.xUnitLog = xUnitLog;
    }

    /**
     * Checks if the current input file is not empty.
     *
     * @param inputFile the input file
     * @return true if not empty, false otherwise
     */
    public boolean checkFileIsNotEmpty(File inputFile) {
        try {
            return inputFile.getCanonicalFile().length() != 0;
        } catch (IOException ex) {
            return inputFile.length() != 0;
        }
    }

    /**
     * Validates an input file.
     *
     * @param xUnitToolInfo the xUnit tool info wrapper
     * @param inputFile the current input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation
     *         exceptions
     */
    public boolean validateInputFile(XUnitToolInfo xUnitToolInfo, File inputFile) throws XUnitException {

        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        // Validates the input file (not empty)
        try {
            if (!inputMetric.validateInputFile(inputFile)) {
                // ignores invalid files
                xUnitLog.warn("The file '" + inputFile + "' is an invalid file.");
                for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
                    xUnitLog.warn(validatorError.toString());
                }

                return false;
            }
        } catch (ValidationException ve) {
            throw new XUnitException("Validation error on input", ve);
        }
        return true;
    }

    /**
     * Validates the converted file against a JUnit format
     *
     * @param xUnitToolInfo the xUnit info wrapper object
     * @param inputFile the input metric from the conversion
     * @param junitTargetFile the converted input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation
     *         exceptions
     */
    public boolean validateOutputFile(XUnitToolInfo xUnitToolInfo, File inputFile, File junitTargetFile) throws XUnitException {
        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        try {
            // Validates the output
            boolean validateOutput = inputMetric.validateOutputFile(junitTargetFile);
            if (!validateOutput) {
                xUnitLog.warn("The converted file for the input file '" + inputFile + "' doesn't match the JUnit format");
                for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
                    xUnitLog.warn(validatorError.toString());
                }
                return false;
            }

        } catch (ValidationException ve) {
            throw new XUnitException("Validation error on output", ve);
        }

        return true;
    }
}
