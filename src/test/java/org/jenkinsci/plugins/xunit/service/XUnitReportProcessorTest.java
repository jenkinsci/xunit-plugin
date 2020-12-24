/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Gregory Boissinot, Nikolas Falco
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
package org.jenkinsci.plugins.xunit.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.assertj.core.api.Assertions;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.model.InputMetricType;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;

import hudson.model.TaskListener;

public class XUnitReportProcessorTest {

    private XUnitReportProcessorService xUnitReportProcessorService;
    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    @SuppressWarnings("serial")
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
        public String[] getInputXsdNameList() {
            return null;
        }

        @Override
        public OutputMetric getOutputFormatType() {
            return JUnitModel.LATEST;
        }
    }

    public static class MyTestTypeDescriptor extends TestTypeDescriptor<MyTestType> {

        public MyTestTypeDescriptor() {
            super(MyTestType.class, MyInputMetric.class);
        }

        @Override
        public String getId() {
            return MyInputMetric.class.toString();
        }
    }

    public static class MyTestType extends TestType {

        public MyTestType(String pattern, boolean faildedIfNotNew, boolean deleteOutputFiles) {
            super(pattern);
            setFailIfNotNew(faildedIfNotNew);
            setDeleteOutputFiles(deleteOutputFiles);
        }

        @Override
        public TestTypeDescriptor<? extends TestType> getDescriptor() {
            return new MyTestTypeDescriptor();
        }
    }


    @Before
    public void setup() {
        final TaskListener listenerMock = mock(TaskListener.class);
        when(listenerMock.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        xUnitReportProcessorService = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(XUnitLog.class).in(Singleton.class);
                bind(TaskListener.class).toInstance(listenerMock);
            }
        }).getInstance(XUnitReportProcessorService.class);

    }

    @Test
    public void isEmptyPattern() {
        Assert.assertTrue(xUnitReportProcessorService.isEmptyPattern(null));
        Assert.assertTrue(xUnitReportProcessorService.isEmptyPattern(""));
        Assert.assertFalse(xUnitReportProcessorService.isEmptyPattern("abc"));
    }

    @Test
    public void findReportsOneFile() throws Exception {
        File f1 = folderRule.newFile("a.txt");
        XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
        when(xUnitToolInfoMock.getInputMetric()).thenReturn(new MyInputMetric());
        when(xUnitToolInfoMock.getPattern()).thenReturn("*.txt");

        String[] xUnitFiles = xUnitReportProcessorService.findReports(f1.getParentFile(), xUnitToolInfoMock);
        Assertions.assertThat(xUnitFiles).isNotEmpty().hasSize(1).contains(f1.getName());
    }

    @Test(expected = NoTestFoundException.class)
    public void verify_processor_throws_exception_if_no_reports_was_found() throws Exception {
        XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
        when(xUnitToolInfoMock.getInputMetric()).thenReturn(new MyInputMetric());
        when(xUnitToolInfoMock.getPattern()).thenReturn("*.xml");

        xUnitReportProcessorService.findReports(folderRule.newFolder(), xUnitToolInfoMock);
    }

}
