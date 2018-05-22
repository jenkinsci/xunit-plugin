package org.jenkinsci.plugins.xunit.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.jenkinsci.lib.dtkit.model.InputMetricType;
import org.jenkinsci.lib.dtkit.model.InputMetricXSL;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.plugins.xunit.NoFoundTestException;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;

import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

@RunWith(MockitoJUnitRunner.class)
public class XUnitTransformerTest {

    @Mock
    private TaskListener buildListenerMock;

    @Mock
    private XUnitReportProcessorService xUnitReportProcessorServiceMock;

    @Mock
    private XUnitConversionService xUnitConversionServiceMock;

    @Mock
    private XUnitValidationService xUnitValidationServiceMock;

    @Mock
    private XUnitToolInfo xUnitToolInfoMock;

    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    private XUnitTransformer xUnitTransformer;

    @Before
    public void beforeTest() throws IOException {
        when(xUnitToolInfoMock.getInputMetric()).thenReturn(new MyInputMetric());

        xUnitTransformer = Guice.createInjector(Stage.DEVELOPMENT, new AbstractModule() {
            @Override
            protected void configure() {
                bind(TaskListener.class).toInstance(buildListenerMock);
                bind(XUnitToolInfo.class).toInstance(xUnitToolInfoMock);
                bind(XUnitConversionService.class).toInstance(xUnitConversionServiceMock);
                bind(XUnitValidationService.class).toInstance(xUnitValidationServiceMock);
                bind(XUnitReportProcessorService.class).toInstance(xUnitReportProcessorServiceMock);
            }
        }).getInstance(XUnitTransformer.class);
        xUnitTransformer.setProcessorId("testProcessor");
    }

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

    @Test(expected = NoFoundTestException.class)
    public void emptyResultFiles() throws Exception {

        //Test result
        File ws = folderRule.newFolder();
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkFailedNewFiles() throws Exception {

        //Recording behaviour : testing not empty result files found and a false check
        String fileName = "a.txt";
        File fileReport = new File(fileName);
        List<String> resultFiles = Arrays.asList(fileName);
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(fileReport);

        //Test result
        File ws = folderRule.newFolder();
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));

        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
    }

    @Test
    public void oneFileEmptyWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a empty file
        //Test the process continues and prints a message
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "a.txt");
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), (String) any())).thenReturn(myInputFile);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));

        // Theses methods are never call
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileEmptyWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create an empty file
        //Test the process continues and prints a message
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "a.txt");
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //The process exits on true
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));

        // Theses methods are never call
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(folderRule.newFolder(), "a.txt");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrderReport = inOrder(xUnitReportProcessorServiceMock);
        inOrderReport.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrderReport.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrderReport.verify(xUnitReportProcessorServiceMock).getCurrentReport(any(File.class), anyString());

        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(folderRule.newFolder(), "a.txt");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrderReport = inOrder(xUnitReportProcessorServiceMock);
        inOrderReport.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrderReport.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrderReport.verify(xUnitReportProcessorServiceMock).getCurrentReport(any(File.class), anyString());

        verify(xUnitValidationServiceMock).checkFileIsNotEmpty(any(File.class));
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyWithOneFileEmptyWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a non empty file
        File myInputFileNotEmpty = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFileNotEmpty);
        fos.write("bidon".getBytes());
        fos.close();
        File myInputFileEmpty = new File(folderRule.newFolder(), "b.txt");
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFileNotEmpty);
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFileEmpty);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessorServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void oneFileNotEmptyAndOneFileEmptyAndStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a non empty file
        File myInputFileNotEmpty = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFileNotEmpty);
        fos.write("bidon".getBytes());
        fos.close();
        File myInputFileEmpty = new File(folderRule.newFolder(), "b.txt");
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFileNotEmpty);
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFileEmpty);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessorServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void twoFilesNotEmptyWithStopActivated() throws Exception {
        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a dummy non empty file
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        File targetFolder = new File(ws, "output");
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(targetFolder);
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));

        verify(xUnitReportProcessorServiceMock, times(2)).getCurrentReport(eq(ws), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), eq(targetFolder));
    }


    @Test
    public void twoFilesNotEmptyWithStopNotActivated() throws Exception {
        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a dummy non empty file
        File myInputFile = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));

        verify(xUnitReportProcessorServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Wrong input validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(false);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Wrong input validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(false);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkedFailedValidationInputWihOneFileFollowedByOneValidFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File ws = folderRule.newFolder();
        File myInputFile1 = new File(ws, "a.txt");
        FileOutputStream fos1 = new FileOutputStream(myInputFile1);
        fos1.write("bidon1".getBytes());
        fos1.close();
        File myInputFile2 = new File(ws, "b.txt");
        FileOutputStream fos2 = new FileOutputStream(myInputFile2);
        fos2.write("bidon2".getBytes());
        fos2.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFile1);

        //Case: Wrong input validation on first file and right on second
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1))).thenReturn(false);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        // The method for the second must never be called
        verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileFollowedByOneValidFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile1 = new File(folderRule.newFolder(), "a.txt");
        FileOutputStream fos1 = new FileOutputStream(myInputFile1);
        fos1.write("bidon1".getBytes());
        fos1.close();
        File myInputFile2 = new File(folderRule.newFolder(), "b.txt");
        FileOutputStream fos2 = new FileOutputStream(myInputFile2);
        fos2.write("bidon2".getBytes());
        fos2.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFile1);
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFile2);

        //Case: Wrong input validation on first file and right on second
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1))).thenReturn(false);
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile2))).thenReturn(true);

        //Right conversion and output validation
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        // The method for the second must never be called
        verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessorServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihOneFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        File targetFolder = new File(ws, "output");
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(targetFolder);

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), eq(myInputFile));
        ArgumentCaptor<File> arguments = ArgumentCaptor.forClass(File.class);
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), eq(myInputFile), arguments.capture());
        File outputFolder = arguments.getValue();
        Assert.assertThat(outputFolder.getName(), CoreMatchers.equalTo(xUnitTransformer.getProcessorId()));
        Assert.assertThat(outputFolder.getParent(), CoreMatchers.endsWith(XUnitDefaultValues.GENERATED_JUNIT_DIR));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), eq(myInputFile), eq(targetFolder));
    }

    @Test
    public void checkedFailedValidationOutputWihOneFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        File targetFolder = new File(ws, "output");
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(targetFolder);

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));

        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), eq(myInputFile));
        ArgumentCaptor<File> arguments = ArgumentCaptor.forClass(File.class);
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), eq(myInputFile), arguments.capture());
        File outputFolder = arguments.getValue();
        Assert.assertThat(outputFolder.getName(), CoreMatchers.equalTo(xUnitTransformer.getProcessorId()));
        Assert.assertThat(outputFolder.getParent(), CoreMatchers.endsWith(XUnitDefaultValues.GENERATED_JUNIT_DIR));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), eq(myInputFile), eq(targetFolder));
    }

    @Test
    public void checkedFailedValidationOutputWihTwoFilesWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File ws = folderRule.newFolder();
        File myInputFile = new File(ws, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        File targetFolder = new File(ws, "output");
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(targetFolder);

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(ws, mock(VirtualChannel.class)));

        //Verifying mock interactions
        //Exit at the first validation output error
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), eq(ws), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), eq(ws));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(ws, resultFiles.get(0));

        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), eq(myInputFile));
        ArgumentCaptor<File> arguments = ArgumentCaptor.forClass(File.class);
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), eq(myInputFile), arguments.capture());
        File outputFolder = arguments.getValue();
        Assert.assertThat(outputFolder.getName(), CoreMatchers.equalTo(xUnitTransformer.getProcessorId()));
        Assert.assertThat(outputFolder.getParent(), CoreMatchers.endsWith(XUnitDefaultValues.GENERATED_JUNIT_DIR));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), eq(myInputFile), eq(targetFolder));
    }

    @Test
    public void checkedFailedValidationOutputWihTwoFilesWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessorServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), (String) any())).thenReturn(resultFiles);

        //Check OK
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessorServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(folderRule.newFolder(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessorServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(folderRule.newFolder(), "output"));

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(folderRule.newFolder(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        //Exit at the first validation output error
        InOrder inOrder = inOrder(xUnitReportProcessorServiceMock);
        inOrder.verify(xUnitReportProcessorServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), (String) isNull());
        inOrder.verify(xUnitReportProcessorServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessorServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

}
