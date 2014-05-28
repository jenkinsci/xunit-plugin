package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.model.InputMetricOther;
import com.thalesgroup.dtkit.util.converter.ConversionException;
import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.dtkit.util.validator.ValidationException;
import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.xunit.types.model.JUnit10;

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

    @Override
    public boolean validateInputFile(File inputXMLFile) throws ValidationException {
        final JUnit10 jUnit = new JUnit10();
        List<ValidationError> errors = jUnit.validate(inputXMLFile);
        setInputValidationErrors(errors);
        return errors.isEmpty();
    }

    @Override
    public boolean validateOutputFile(File inputXMLFile) throws ValidationException {
        return true;
    }
}
