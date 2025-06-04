/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Thales Corporate Services SAS Gregory Boissinot
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


import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidInputMetricXSLTest {

    private static final List<Class<? extends InputMetricXSL>> LIST_INPUT_METRIC = List.of(
            AUnit.class, BoostTest.class, CppTest.class, CppUnit.class, FPCUnit.class, MSTest.class,
            NUnit.class, PHPUnit.class, UnitTest.class);

    @Test
    void testAllTypes() throws Exception {
        for (Class<? extends InputMetricXSL> inputMetricXSLClass : LIST_INPUT_METRIC) {
            InputMetricXSL inputMetricXSL = inputMetricXSLClass.getDeclaredConstructor()
                    .newInstance();

            //The following elements must be set
            assertNotNull(inputMetricXSL.getOutputFormatType());
            assertNotNull(inputMetricXSL.getXslName());
            assertNotNull(inputMetricXSL.getToolName());
            assertNotNull(inputMetricXSL.getToolVersion());
            assertNotNull(inputMetricXSL.getToolType());

            //The xsl must exist
            assertDoesNotThrow(() -> new File(
                            inputMetricXSL.getClass().getResource(inputMetricXSL.getXslName()).toURI()),
                    inputMetricXSL.getXslName() + " doesn't exist.");

            //The xsd must exist if it sets
            if (inputMetricXSL.getInputXsdNameList() != null) {
                for (int i = 0; i < inputMetricXSL.getInputXsdNameList().length; i++) {
                    String name = inputMetricXSL.getInputXsdNameList()[i];
                    assertDoesNotThrow(
                            () -> new File(inputMetricXSL.getClass().getResource(name).toURI()),
                            name + " doesn't exist.");
                }
            }
        }
    }
}
