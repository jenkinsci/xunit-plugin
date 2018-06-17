package org.jenkinsci.plugins.xunit.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

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

import hudson.Util;
import hudson.model.TaskListener;

public class XUnitReportProcessorServiceTest {

    private XUnitReportProcessorService xUnitReportProcessorService;
    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

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
            super(pattern, faildedIfNotNew, deleteOutputFiles);
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

        List<String> xUnitFiles = xUnitReportProcessorService.findReports(xUnitToolInfoMock, f1.getParentFile(), "*.txt");
        Assert.assertFalse(xUnitFiles.isEmpty());
        Assert.assertEquals(1, xUnitFiles.size());
        Assert.assertEquals(f1.getName(), xUnitFiles.get(0));
    }

    @Test(expected = NoTestFoundException.class)
    public void verify_processor_throws_exception_if_no_reports_was_found() throws Exception {
        XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
        when(xUnitToolInfoMock.getInputMetric()).thenReturn(new MyInputMetric());

        xUnitReportProcessorService.findReports(xUnitToolInfoMock, folderRule.newFolder(), "*.xml");
    }

}
