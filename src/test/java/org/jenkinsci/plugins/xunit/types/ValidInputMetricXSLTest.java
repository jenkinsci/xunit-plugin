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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidInputMetricXSLTest {

    private static List<Class<? extends InputMetricXSL>> listInputMetric = new ArrayList<Class<? extends InputMetricXSL>>();

    @BeforeClass
    public static void loadList() {
        listInputMetric.add(AUnit.class);
        listInputMetric.add(BoostTest.class);
        listInputMetric.add(CppTest.class);
        listInputMetric.add(CppUnit.class);
        listInputMetric.add(FPCUnit.class);
        listInputMetric.add(MSTest.class);
        listInputMetric.add(NUnit.class);
        listInputMetric.add(PHPUnit.class);
        listInputMetric.add(UnitTest.class);
    }

    @Test
    public void testAllTypes() throws Exception {
        for (Class<? extends InputMetricXSL> inputMetricXSLClass : listInputMetric) {
            InputMetricXSL inputMetricXSL = inputMetricXSLClass.newInstance();

            //The following elements must be set
            Assert.assertNotNull(inputMetricXSL.getOutputFormatType());
            Assert.assertNotNull(inputMetricXSL.getXslName());
            Assert.assertNotNull(inputMetricXSL.getToolName());
            Assert.assertNotNull(inputMetricXSL.getToolVersion());
            Assert.assertNotNull(inputMetricXSL.getToolType());

            //The xsl must exist
            try {
                new File(inputMetricXSL.getClass().getResource(inputMetricXSL.getXslName()).toURI());
                Assert.assertTrue(true);
            } catch (NullPointerException npe) {
                Assert.assertTrue(inputMetricXSL.getXslName() + " doesn't exist.", false);
            }

            //The xsd must exist if it sets
            if (inputMetricXSL.getInputXsdNameList() != null) {
                try {
                    for (int i = 0; i < inputMetricXSL.getInputXsdNameList().length; i++) {
                        new File(inputMetricXSL.getClass().getResource(inputMetricXSL.getInputXsdNameList()[i]).toURI());
                    }

                    Assert.assertTrue(true);
                } catch (NullPointerException npe) {
                    Assert.assertTrue("one of" + Arrays.toString(inputMetricXSL.getInputXsdNameList()) + " doesn't exist.", false);
                }
            }


        }
    }
}
