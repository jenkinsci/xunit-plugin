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


import org.apache.commons.io.FileUtils;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputMetricFactory;
import org.jenkinsci.lib.dtkit.util.validator.ValidationError;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gregory Boissinot
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractTest {

    @TempDir
    protected File file;

    private static String resolveInput(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/input.xml", packageName, testNumber);
    }

    private static String resolveXSL(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/input.xsl", packageName, testNumber);
    }

    private static String resolveOutput(String packageName, int testNumber) {
        return MessageFormat.format("{0}/testcase{1}/result.xml", packageName, testNumber);
    }

    protected abstract Stream<Arguments> data();

    @ParameterizedTest(name = "{0}")
    @MethodSource("data")
    void verifyXSLT(String testName, Class<? extends InputMetric> metricClass, String packageName, int testNumber) throws Exception {
        String input = resolveInput(packageName, testNumber);
        String xslPath = resolveXSL(packageName, testNumber);
        String expectedResult = resolveOutput(packageName, testNumber);

        convertAndValidate(metricClass, input, xslPath, expectedResult);
    }

    private String readXmlAsString(File input) throws IOException {
        if (input == null) {
            throw new IOException("The input stream object is null.");
        }

        return FileUtils.readFileToString(input, StandardCharsets.UTF_8);
    }

    private void convertAndValidate(Class<? extends InputMetric> metricClass, String inputXMLPath,
                                    String inputXSLPath, String expectedResultPath) throws Exception {
        InputMetric inputMetric = InputMetricFactory.getInstance(metricClass);
        if (inputMetric instanceof CustomInputMetric) {
            ((CustomInputMetric) inputMetric).setCustomXSLFile(
                    new File(this.getClass().getResource(inputXSLPath).toURI()));
        }

        File outputXMLFile = File.createTempFile("junit", null, file);
        File inputXMLFile = new File(this.getClass().getResource(inputXMLPath).toURI());

        //The input file must be valid
        boolean inputResult = inputMetric.validateInputFile(inputXMLFile);
        for (ValidationError validatorError : inputMetric.getInputValidationErrors()) {
            System.out.println("[ERROR] " + validatorError.toString());
        }
        assertTrue(inputResult);

        inputMetric.convert(inputXMLFile, outputXMLFile);
        try {
            Diff myDiff = DiffBuilder.compare(Input.fromString(readXmlAsString(
                            new File(this.getClass().getResource(expectedResultPath).toURI())))) //
                    .withTest(Input.fromString(readXmlAsString(outputXMLFile))) //
                    .ignoreWhitespace() //
                    .ignoreComments() //
                    .normalizeWhitespace() //
                    .build();
            assertFalse(myDiff.hasDifferences(), myDiff.toString());
        } catch (Error | XMLUnitException e) {
            System.err.println(readXmlAsString(outputXMLFile));
            throw e;
        }

        //The generated output file must be valid
        boolean outputResult = inputMetric.validateOutputFile(outputXMLFile);
        for (ValidationError validatorError : inputMetric.getOutputValidationErrors()) {
            System.out.println(validatorError);
        }
        assertTrue(outputResult);
    }
}