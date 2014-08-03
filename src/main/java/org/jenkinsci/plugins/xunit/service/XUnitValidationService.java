package org.jenkinsci.plugins.xunit.service;


import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.jenkinsci.lib.dtkit.util.validator.ValidationException;
import org.jenkinsci.plugins.xunit.exception.XUnitException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class XUnitValidationService extends XUnitService implements Serializable {

    /**
     * Checks if the current input file is not empty
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
     * Validates an input file
     *
     * @param xUnitToolInfo the xUnit tool info wrapper
     * @param inputFile     the current input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation exceptions
     */
    public boolean validateInputFile(XUnitToolInfo xUnitToolInfo, File inputFile) throws XUnitException {

        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        //Validates the input file (not empty)
        try {
            if (!inputMetric.validateInputFile(inputFile)) {

                //Ignores invalid files
                warningSystemLogger("The file '" + inputFile + "' is an invalid file.");
                for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
                    warningSystemLogger(validatorError.toString());
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
     * @param xUnitToolInfo   the xUnit info wrapper object
     * @param inputFile       the input metric from the conversion
     * @param junitTargetFile the converted input file
     * @return true if the validation is success, false otherwise
     * @throws XUnitException an XUnitException when there are validation exceptions
     */
    public boolean validateOutputFile(XUnitToolInfo xUnitToolInfo, File inputFile, File junitTargetFile) throws XUnitException {
        InputMetric inputMetric = xUnitToolInfo.getInputMetric();

        try {
            //Validates the output
            boolean validateOutput = inputMetric.validateOutputFile(junitTargetFile);
            if (!validateOutput) {
                warningSystemLogger("The converted file for the input file '" + inputFile + "' doesn't match the JUnit format");
                for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
                    warningSystemLogger(validatorError.toString());
                }
                return false;
            }

        } catch (ValidationException ve) {
            throw new XUnitException("Validation error on output", ve);
        }

        return true;
    }
}
