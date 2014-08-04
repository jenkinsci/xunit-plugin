package org.jenkinsci.plugins.xunit.types;


import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.junit.Assert;
import org.junit.Before;

import java.io.*;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractTest {


    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
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
        InputMetric inputMetric = InputMetricFactory.getInstance(metricClass);

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

        CustomInputMetric customInputMetric = CustomInputMetric.class.newInstance();
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