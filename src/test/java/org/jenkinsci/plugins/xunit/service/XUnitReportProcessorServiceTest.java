package org.jenkinsci.plugins.xunit.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import hudson.Util;
import hudson.model.BuildListener;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.model.InputMetricType;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;
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

public class XUnitReportProcessorServiceTest {

    private static XUnitReportProcessorService xUnitReportProcessorService;


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
        xUnitReportProcessorService = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(XUnitLog.class).in(Singleton.class);
                bind(BuildListener.class).toInstance(listenerMock);
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void findReportsOneFile() throws IOException {
        File dir = Util.createTempDir();
        File f1 = new File(dir, "a.txt");
        try {
            f1.createNewFile();

            XUnitToolInfo xUnitToolInfoMock = mock(XUnitToolInfo.class);
            when(xUnitToolInfoMock.getInputMetric()).thenReturn(new MyInputMetric());

            List<String> xUnitFiles = xUnitReportProcessorService.findReports(xUnitToolInfoMock, dir, "*.txt");
            Assert.assertFalse(xUnitFiles.isEmpty());
            Assert.assertEquals(1, xUnitFiles.size());
            Assert.assertEquals(f1.getName(), xUnitFiles.get(0));
        } finally {
            f1.delete();
            dir.delete();
        }
    }

}
