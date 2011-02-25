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

package com.thalesgroup.hudson.plugins.xunit.transformer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Stage;
import com.thalesgroup.hudson.plugins.xunit.service.*;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;


public class XUnitTransformerTest {

    @Mock
    @SuppressWarnings("unused")
    private BuildListener buildListenerMock;

    @Mock
    @SuppressWarnings("unused")
    private XUnitReportProcessingService xUnitReportProcessingServiceMock;

    @Mock
    @SuppressWarnings("unused")
    private XUnitConversionService xUnitConversionServiceMock;

    @Mock
    @SuppressWarnings("unused")
    private XUnitValidationService xUnitValidationServiceMock;

    @Rule
    public TempWorkspace tempWorkspace = new TempWorkspace();

    private XUnitTransformer xUnitTransformer;

    @Before
    public void beforeTest() throws IOException {

        MockitoAnnotations.initMocks(this);

        when(buildListenerMock.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));

        xUnitTransformer = Guice.createInjector(Stage.DEVELOPMENT, new AbstractModule() {
            @Override
            protected void configure() {
                bind(BuildListener.class).toInstance(buildListenerMock);
                bind(XUnitToolInfo.class).toInstance(mock(XUnitToolInfo.class));
                bind(XUnitConversionService.class).toInstance(xUnitConversionServiceMock);
                bind(XUnitValidationService.class).toInstance(xUnitValidationServiceMock);
                bind(XUnitReportProcessingService.class).toInstance(xUnitReportProcessingServiceMock);
            }
        }).getInstance(XUnitTransformer.class);
    }

    @Test
    public void emptyResultFiles() throws Exception {

        //Test result
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkFailedNewFiles() throws Exception {

        //Recording behaviour : testing not empty result files found and a false check
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(false);

        //Test result
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));

        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
    }

    @Test
    public void oneFileEmptyWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a empty file
        //Test the process continues and prints a message
        File myInputFile = new File(tempWorkspace.getDir(), "a.txt");
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());

        // Theses methods are never call
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void oneFileEmptyWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create an empty file
        //Test the process continues and prints a message
        File myInputFile = new File(tempWorkspace.getDir(), "a.txt");
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //The process exits on true
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());

        // Theses methods are never call
        verify(xUnitValidationServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "a.txt");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrderReport = inOrder(xUnitReportProcessingServiceMock);
        inOrderReport.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrderReport.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrderReport.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());

        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);
        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "a.txt");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrderReport = inOrder(xUnitReportProcessingServiceMock);
        inOrderReport.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrderReport.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrderReport.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());

        verify(xUnitValidationServiceMock).checkFileIsNotEmpty(any(File.class));
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyWithOneFileEmptyWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a non empty file
        File myInputFileNotEmpty = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFileNotEmpty);
        fos.write("bidon".getBytes());
        fos.close();
        File myInputFileEmpty = new File(tempWorkspace.getDir(), "b.txt");
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFileNotEmpty);
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFileEmpty);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessingServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void oneFileNotEmptyAndOneFileEmptyAndStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Wants to call the real method checkFileIsNotEmpty
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenCallRealMethod();

        //Create a non empty file
        File myInputFileNotEmpty = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFileNotEmpty);
        fos.write("bidon".getBytes());
        fos.close();
        File myInputFileEmpty = new File(tempWorkspace.getDir(), "b.txt");
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFileNotEmpty);
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFileEmpty);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessingServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void twoFilesNotEmptyWithStopActivated() throws Exception {
        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a dummy non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));

        verify(xUnitReportProcessingServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void twoFilesNotEmptyWithStopNotActivated() throws Exception {
        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a dummy non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));

        verify(xUnitReportProcessingServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Wrong input validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(false);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Wrong input validation
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(false);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkedFailedValidationInputWihOneFileFollowedByOneValidFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile1 = new File(tempWorkspace.getDir(), "a.txt");
        FileOutputStream fos1 = new FileOutputStream(myInputFile1);
        fos1.write("bidon1".getBytes());
        fos1.close();
        File myInputFile2 = new File(tempWorkspace.getDir(), "b.txt");
        FileOutputStream fos2 = new FileOutputStream(myInputFile2);
        fos2.write("bidon2".getBytes());
        fos2.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFile1);
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFile2);

        //Case: Wrong input validation on first file and right on second
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1))).thenReturn(false);
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile2))).thenReturn(true);

        //Right conversion and output validation
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);


        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        // The method for the second must never be called
        verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationInputWihOneFileFollowedByOneValidFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile1 = new File(tempWorkspace.getDir(), "a.txt");
        FileOutputStream fos1 = new FileOutputStream(myInputFile1);
        fos1.write("bidon1".getBytes());
        fos1.close();
        File myInputFile2 = new File(tempWorkspace.getDir(), "b.txt");
        FileOutputStream fos2 = new FileOutputStream(myInputFile2);
        fos2.write("bidon2".getBytes());
        fos2.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("a.txt"))).thenReturn(myInputFile1);
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), eq("b.txt"))).thenReturn(myInputFile2);

        //Case: Wrong input validation on first file and right on second
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1))).thenReturn(false);
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile2))).thenReturn(true);

        //Right conversion and output validation
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process must exit on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        // The method for the second must never be called
        verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitReportProcessingServiceMock, times(2)).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihOneFileWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihOneFileWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihTwoFilesWithStopActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        //Exit at the first validation output error
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihTwoFilesWithStopNotActivated() throws Exception {

        //One result file
        List<String> resultFiles = Arrays.asList("a.txt", "b.txt");
        when(xUnitReportProcessingServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitReportProcessingServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);
        when(xUnitValidationServiceMock.checkFileIsNotEmpty(any(File.class))).thenReturn(true);

        //Stop processing when there is an error
        when(xUnitReportProcessingServiceMock.isStopProcessingIfError(any(XUnitToolInfo.class))).thenReturn(false);

        //Create a non empty file
        File myInputFile = new File(tempWorkspace.getDir(), "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitReportProcessingServiceMock.getCurrentReport(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitValidationServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitConversionServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class))).thenReturn(new File(tempWorkspace.getDir(), "output"));

        //Wrong output validation
        when(xUnitValidationServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exits on false
        Assert.assertFalse(xUnitTransformer.invoke(tempWorkspace.getDir(), mock(VirtualChannel.class)));

        //Verifying mock interactions
        //Exit at the first validation output error
        InOrder inOrder = inOrder(xUnitReportProcessingServiceMock);
        inOrder.verify(xUnitReportProcessingServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitReportProcessingServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitReportProcessingServiceMock).getCurrentReport(any(File.class), anyString());
        verify(xUnitValidationServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitConversionServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class), any(File.class));
        verify(xUnitValidationServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

}
