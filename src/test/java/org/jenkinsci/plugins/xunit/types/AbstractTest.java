package org.jenkinsci.plugins.xunit.types;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.util.converter.ConversionService;
import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.dtkit.util.validator.ValidationService;
import com.thalesgroup.hudson.plugins.xunit.types.CustomInputMetric;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.*;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractTest {

    private static Injector injector;

    @BeforeClass
    public static void initInjector() {
        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                //Optional binding, provided by default in Guice)
                bind(ValidationService.class).in(Singleton.class);
                bind(ConversionService.class).in(Singleton.class);
            }
        });
    }

    private String readXmlAsString(File input)
            throws IOException {
        String xmlString = "";

        if (input == null) {
            throw new IOException("The input stream object is null.");
        }

        FileInputStream fileInputStream = new FileInputStream(input);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            xmlString += line + "\n";
            line = bufferedReader.readLine();
        }
        fileInputStream.close();
        fileInputStream.close();
        bufferedReader.close();

        return xmlString;
    }

    protected void convertAndValidate(Class<? extends InputMetric> metricClass, String inputXMLPath, String expectedResultPath) throws Exception {
        InputMetric inputMetric = injector.getInstance(metricClass);

        File outputXMLFile = File.createTempFile("result", "xml");
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());

        //The input file must be valid
        boolean inputResult = inputMetric.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        Assert.assertTrue(inputResult);

        inputMetric.convert(inputXMLFile, outputXMLFile);
        XMLUnit.setIgnoreWhitespace(true);
        Diff myDiff = new Diff(readXmlAsString(new File(this.getClass().getResource(expectedResultPath).toURI())), readXmlAsString(outputXMLFile));
        Assert.assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());

        //The generated output file must be valid
        boolean outputResult = inputMetric.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        Assert.assertTrue(outputResult);

        outputXMLFile.deleteOnExit();

    }

    protected void convertAndValidate(String inputXMLPath, String inputXSLPath, String expectedResultPath) throws Exception {

        CustomInputMetric customInputMetric = injector.getInstance(CustomInputMetric.class);
        customInputMetric.setCustomXSLFile(new File(this.getClass().getResource(inputXSLPath).toURI()));

        File outputXMLFile = File.createTempFile("result", "xml");
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());

        //The input file must be valid
        boolean inputResult = customInputMetric.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : customInputMetric.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        Assert.assertTrue(inputResult);

        customInputMetric.convert(inputXMLFile, outputXMLFile);
        Diff myDiff = new Diff(readXmlAsString(new File(this.getClass().getResource(expectedResultPath).toURI())), readXmlAsString(outputXMLFile));
        Assert.assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());

        //The generated output file must be valid
        boolean outputResult = customInputMetric.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : customInputMetric.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        Assert.assertTrue(outputResult);

        outputXMLFile.deleteOnExit();
    }


}
