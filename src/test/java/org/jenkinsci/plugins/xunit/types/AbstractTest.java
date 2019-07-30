/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016, Nikolas Falco
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractTest {

    @Rule
    public TemporaryFolder file = new TemporaryFolder();

    public static String resolveInput(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/input.xml", packageName, testNumber);
    }

    public static String resolveXSL(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/input.xsl", packageName, testNumber);
    }

    public static String resolveOutput(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/result.xml", packageName, testNumber);
    }

    private final String input;
    private final String expectedResult;
    private final Class<? extends InputMetric> metricClass;
    private final String xslPath;

    public AbstractTest() {
        // TODO remove when all test will be adapted
        input = null;
        expectedResult = null;
        metricClass = null;
        xslPath = null;
    }

    protected AbstractTest(Class<? extends InputMetric> metricClass, String input, String expectedResult) {
        this(metricClass, input, null, expectedResult);
    }

    protected AbstractTest(Class<? extends InputMetric> metricClass, String input, String xslPath, String expectedResult) {
        this.input = input;
        this.expectedResult = expectedResult;
        this.metricClass = metricClass;
        this.xslPath = xslPath;
    }

    @Test
    public void verifyXSLT() throws Exception {
        convertAndValidate(metricClass, input, xslPath, expectedResult);
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

    protected void convertAndValidate(Class<? extends InputMetric> metricClass, String inputXMLPath, String inputXSLPath, String expectedResultPath) throws Exception {
        InputMetric inputMetric = InputMetricFactory.getInstance(metricClass);
        if (inputMetric instanceof CustomInputMetric) {
            ((CustomInputMetric) inputMetric).setCustomXSLFile(new File(this.getClass().getResource(inputXSLPath).toURI()));
        }
        
        File outputXMLFile = file.newFile();
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());
        
        //The input file must be valid
        boolean inputResult = inputMetric.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        Assert.assertTrue(inputResult);
        
        inputMetric.convert(inputXMLFile, outputXMLFile);
        Diff myDiff = DiffBuilder.compare(Input.fromString(readXmlAsString(outputXMLFile))) //
                .withTest(Input.fromString(readXmlAsString(new File(this.getClass().getResource(expectedResultPath).toURI())))) //
                .ignoreWhitespace() //
                .ignoreComments() //
                .normalizeWhitespace() //
                .build();
        try {
            Assert.assertFalse(myDiff.toString(), myDiff.hasDifferences());
        } catch (Error e) {
            System.err.println(readXmlAsString(outputXMLFile));
            throw e;
        }
        
        //The generated output file must be valid
        boolean outputResult = inputMetric.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        Assert.assertTrue(outputResult);
    }
}