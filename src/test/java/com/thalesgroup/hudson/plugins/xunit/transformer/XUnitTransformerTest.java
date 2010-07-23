package com.thalesgroup.hudson.plugins.xunit.transformer;

import com.thalesgroup.hudson.plugins.xunit.service.XUnitService;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import org.junit.*;
import org.mockito.InOrder;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;


public class XUnitTransformerTest {

    private static BuildListener buildListenerMock;
    private static XUnitService xUnitServiceMock;
    private File workspace;


    @BeforeClass
    public static void beforeAllTests() {
        buildListenerMock = mock(BuildListener.class);
        xUnitServiceMock = mock(XUnitService.class);
    }


    @Before
    public void beforeTest() throws IOException {
        reset(buildListenerMock);
        reset(xUnitServiceMock);
        workspace = Util.createTempDir();
        when(buildListenerMock.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
    }

    @After
    public void afterTest() {
        workspace.delete();
    }


    @Test
    public void emptyResultFiles() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //Test result
        Assert.assertFalse(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkFailedNewFiles() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //Recording behaviour : testing not empty result files found and a false check
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(false);

        //Test result
        Assert.assertFalse(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying
        verify(xUnitServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));

        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
    }

    @Test
    public void oneFileEmpty() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a empty file
        //Test the process continues and prints a message
        File myInputFile = new File(workspace, "a.txt");
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //The process exits on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitServiceMock).getCurrentFile(any(File.class), anyString());

        // Theses methods are never call
        verify(xUnitServiceMock, never()).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void oneFileNotEmpty() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(workspace, "a.txt");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exists on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitServiceMock).getCurrentFile(any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        inOrder.verify(xUnitServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        inOrder.verify(xUnitServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void oneFileNotEmptyWithOneFileEmpty() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt", "b.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFileNotEmpty = new File(workspace, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFileNotEmpty);
        fos.write("bidon".getBytes());
        fos.close();
        File myInputFileEmpty = new File(workspace, "b.txt");
        when(xUnitServiceMock.getCurrentFile(any(File.class), eq("a.txt"))).thenReturn(myInputFileNotEmpty);
        when(xUnitServiceMock.getCurrentFile(any(File.class), eq("b.txt"))).thenReturn(myInputFileEmpty);

        //Case: Right input validation, conversion and right output validation
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exists on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitServiceMock, times(2)).getCurrentFile(any(File.class), anyString());
        verify(xUnitServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void twoFilesNotEmpty() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt", "b.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a dummy non empty file
        File myInputFile = new File(workspace, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation, conversion and right output validation
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);

        //The process exists on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));

        verify(xUnitServiceMock, times(2)).getCurrentFile(any(File.class), anyString());
        verify(xUnitServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock, times(2)).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock, times(2)).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkedFailedValidationInputWihOneFile() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(workspace, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(false);

        //The process exists on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitServiceMock).getCurrentFile(any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        inOrder.verify(xUnitServiceMock, never()).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        inOrder.verify(xUnitServiceMock, never()).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkedFailedValidationInputWihOneFileFollowedByOneValidFile() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt", "b.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile1 = new File(workspace, "a.txt");
        FileOutputStream fos1 = new FileOutputStream(myInputFile1);
        fos1.write("bidon1".getBytes());
        fos1.close();
        File myInputFile2 = new File(workspace, "b.txt");
        FileOutputStream fos2 = new FileOutputStream(myInputFile2);
        fos2.write("bidon2".getBytes());
        fos2.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), eq("a.txt"))).thenReturn(myInputFile1);
        when(xUnitServiceMock.getCurrentFile(any(File.class), eq("b.txt"))).thenReturn(myInputFile2);

        //Case: Wrong input validation on first file and right on second
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile1))).thenReturn(false);
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), eq(myInputFile2))).thenReturn(true);

        //Right conversion and output validation
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(true);


        //The process exists on true
        Assert.assertTrue(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        verify(xUnitServiceMock, times(2)).getCurrentFile(any(File.class), anyString());
        verify(xUnitServiceMock, times(2)).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        verify(xUnitServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        verify(xUnitServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

    @Test
    public void checkedFailedValidationOutputWihOneFile() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(workspace, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));

        //Wrong output validation
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exists on false
        Assert.assertFalse(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitServiceMock).getCurrentFile(any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        inOrder.verify(xUnitServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        inOrder.verify(xUnitServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }


    @Test
    public void checkedFailedValidationOutputWihTwoFiles() throws Exception {

        //Creating test object with mocks
        XUnitTransformer xUnitTransformer = new XUnitTransformer(xUnitServiceMock, buildListenerMock, mock(XUnitToolInfo.class));

        //One result file
        List<String> resultFiles = Arrays.asList(new String[]{"a.txt", "b.txt"});
        when(xUnitServiceMock.findReports(any(XUnitToolInfo.class), any(File.class), anyString())).thenReturn(resultFiles);

        //Check OK
        when(xUnitServiceMock.checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class))).thenReturn(true);

        //Create a non empty file
        File myInputFile = new File(workspace, "dummyFile");
        FileOutputStream fos = new FileOutputStream(myInputFile);
        fos.write("bidon".getBytes());
        fos.close();
        when(xUnitServiceMock.getCurrentFile(any(File.class), anyString())).thenReturn(myInputFile);

        //Case: Right input validation and conversion
        when(xUnitServiceMock.validateInputFile(any(XUnitToolInfo.class), any(File.class))).thenReturn(true);
        when(xUnitServiceMock.convert(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(new File(workspace, "output"));

        //Wrong output validation
        when(xUnitServiceMock.validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class))).thenReturn(false);

        //The process exists on false
        Assert.assertFalse(xUnitTransformer.invoke(workspace, mock(VirtualChannel.class)));

        //Verifying mock interactions
        //Exit at the first validation output error        
        InOrder inOrder = inOrder(xUnitServiceMock);
        inOrder.verify(xUnitServiceMock).findReports(any(XUnitToolInfo.class), any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).checkIfFindsFilesNewFiles(any(XUnitToolInfo.class), eq(resultFiles), any(File.class));
        inOrder.verify(xUnitServiceMock).getCurrentFile(any(File.class), anyString());
        inOrder.verify(xUnitServiceMock).validateInputFile(any(XUnitToolInfo.class), any(File.class));
        inOrder.verify(xUnitServiceMock).convert(any(XUnitToolInfo.class), any(File.class), any(File.class));
        inOrder.verify(xUnitServiceMock).validateOutputFile(any(XUnitToolInfo.class), any(File.class), any(File.class));
    }

}
