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

package org.jenkinsci.plugins.xunit.types;

import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.model.InputMetricOther;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.lib.dtkit.util.validator.ErrorType;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.jenkinsci.lib.dtkit.util.validator.ValidationException;
import org.jenkinsci.plugins.xunit.types.model.JUnit10;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Gregory Boissinot
 */
public class JUnitInputMetric extends InputMetricOther {

    @Override
    public String getToolName() {
        return "JUnit";
    }

    @Override
    public void convert(File inputFile, File outFile, Map<String, Object> params) throws ConversionException {
        try {
            FileUtils.copyFile(inputFile, outFile);
            new FilePath(outFile).touch(System.currentTimeMillis());
        } catch (IOException ioe) {
            throw new ConversionException(ioe);
        } catch (InterruptedException ie) {
            throw new ConversionException(ie);
        }
    }

    /**
     * Consider the file as invalid only if there is a fatal error, i.e. if is
     * not possible for the application to process the document through to the end.
     *
     * @param inputXMLFile
     * @return a boolean
     * @throws ValidationException
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    @Override
    public boolean validateInputFile(File inputXMLFile) throws ValidationException {
        final JUnit10 jUnit = new JUnit10();
        List<ValidationError> errors = jUnit.validate(inputXMLFile);
        boolean isValid = true;
        for (ValidationError error : errors) {
            System.out.println(error + " type: " + error.getType());
            if (error.getType() == ErrorType.FATAL_ERROR) {
                isValid = false;
            }
        }
        return isValid;
    }

    @Override
    public boolean validateOutputFile(File inputXMLFile) throws ValidationException {
        return true;
    }
}
