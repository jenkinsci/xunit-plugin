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

import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.api.InputMetricType;
import com.thalesgroup.dtkit.metrics.api.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.api.InputType;
import com.thalesgroup.dtkit.metrics.api.OutputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.transformer.XUnitToolInfo;
import hudson.Util;
import hudson.model.BuildListener;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.mockito.Mockito.*;

public class XUnitServicelTest {

    private static XUnitService xUnitService;


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
            super(MyTestType.class, new MyInputMetric().getClass());
        }

        public String getId() {
            return new MyInputMetric().getClass().toString();
        }
    }

    public static class MyTestType extends TestType {

        public MyTestType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
            super(pattern, faildedIfNotNew, deleteOutputFiles);
        }

        public TestTypeDescriptor<?> getDescriptor() {
            return new MyTestTypeDescriptor();
        }
    }



    @BeforeClass
    public static void init() {
        BuildListener listener = mock(BuildListener.class);
        when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        xUnitService = new XUnitService(listener);
    }

    @Test
    public void isEmptyPattern() {
        Assert.assertTrue(xUnitService.isEmptyPattern(null));
        Assert.assertTrue(xUnitService.isEmptyPattern(""));
        Assert.assertFalse(xUnitService.isEmptyPattern("abc"));
    }


    @Test
    public void findReportsOneFile() throws IOException {
        File dir = Util.createTempDir();
        File f1 = new File(dir, "a.txt");
        try {
            f1.createNewFile();

            XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
            when(xUnitToolInfoMock.getTestType()).thenReturn(new MyTestType("", true, true));

            List<String> xUnitFiles = xUnitService.findReports(xUnitToolInfoMock, dir, "*.txt");
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
