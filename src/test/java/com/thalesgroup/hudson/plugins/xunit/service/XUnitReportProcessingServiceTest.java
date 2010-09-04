/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricType;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitToolInfo;
import hudson.Util;
import hudson.model.BuildListener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XUnitReportProcessingServiceTest {

    private static XUnitReportProcessingService xUnitReportProcessingService;


    public static class MyInputMetric extends InputMetricXSL {
        @Override
        public String getToolName() {
            return "testTool";
        }

        @Override
        public String getToolVersion() {
            return "testVersion";
        }

        @Override
        public InputMetricType getInputMetricType() {
            return InputMetricType.XSL;
        }

        @Override
        public InputType getToolType() {
            return InputType.TEST;
        }

        @Override
        public String getXslName() {
            return null;
        }

        @Override
        public String getInputXsd() {
            return null;
        }

        @Override
        public OutputMetric getOutputFormatType() {
            return JUnitModel.OUTPUT_JUNIT_1_0;
        }

    }

    public static class MyTestTypeDescriptor extends TestTypeDescriptor<MyTestType> {

        public MyTestTypeDescriptor() {
            super(MyTestType.class, MyInputMetric.class);
        }

        public String getId() {
            return MyInputMetric.class.toString();
        }
    }

    public static class MyTestType extends TestType {

        public MyTestType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
            super(pattern, faildedIfNotNew, deleteOutputFiles);
        }

        public TestTypeDescriptor<? extends TestType> getDescriptor() {
            return new MyTestTypeDescriptor();
        }
    }


    @BeforeClass
    public static void init() {
        final BuildListener listenerMock = mock(BuildListener.class);
        when(listenerMock.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        xUnitReportProcessingService = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(XUnitLog.class).in(Singleton.class);
                bind(BuildListener.class).toInstance(listenerMock);
            }
        }).getInstance(XUnitReportProcessingService.class);


    }

    @Test
    public void isEmptyPattern() {
        Assert.assertTrue(xUnitReportProcessingService.isEmptyPattern(null));
        Assert.assertTrue(xUnitReportProcessingService.isEmptyPattern(""));
        Assert.assertFalse(xUnitReportProcessingService.isEmptyPattern("abc"));
    }


    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void findReportsOneFile() throws IOException {
        File dir = Util.createTempDir();
        File f1 = new File(dir, "a.txt");
        try {
            f1.createNewFile();

            XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
            when(xUnitToolInfoMock.getTestType()).thenReturn(new MyTestType("", true, true));

            List<String> xUnitFiles = xUnitReportProcessingService.findReports(xUnitToolInfoMock, dir, "*.txt");
            Assert.assertFalse(xUnitFiles.isEmpty());
            Assert.assertEquals(1, xUnitFiles.size());
            Assert.assertEquals(f1.getName(), xUnitFiles.get(0));
        }
        finally {
            f1.delete();
            dir.delete();
        }
    }

}
