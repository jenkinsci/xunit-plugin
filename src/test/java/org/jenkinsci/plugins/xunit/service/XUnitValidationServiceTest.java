package org.jenkinsci.plugins.xunit.service;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for XUnitValidationService class.
 *
 * @author Maciek Siemczyk
 */
public class XUnitValidationServiceTest {
    /**
     * Helper class for creating temporary workspace.
     */
    @Rule
    public TemporaryFolder folderRule = new TemporaryFolder();

    /**
     * System Under Test (SUT).
     */
    private XUnitValidationService xUnitValidationService;

    @Before
    public void setup() {
        xUnitValidationService = new XUnitValidationService(mock(XUnitLog.class));
    }

    @Test
    public void CheckFileIsNotEmpty_GivenEmptyFile_ReturnsFalse() throws Exception {
        File testFile = new File(folderRule.newFolder(), "empty.txt");
        boolean created = testFile.createNewFile();
        Assert.assertTrue(created);

        Assert.assertFalse("CheckFileIsNotEmpty returned true.", xUnitValidationService.checkFileIsNotEmpty(testFile));
    }

    @Test
    public void CheckFileIsNotEmpty_GivenNotEmptyFile_ReturnsTrue() throws Exception {
        File testFile = CreateNotEmtyFile();

        Assert.assertTrue("CheckFileIsNotEmpty returned false.", xUnitValidationService.checkFileIsNotEmpty(testFile));
    }

    /**
     * Helper method that will create a file with some text in it.
     *
     * @return Created file.
     * @throws Exception when there is a problem with writing to the file.
     */
    private File CreateNotEmtyFile() throws Exception {
        File testFile = new File(folderRule.newFolder(), "notempty.txt");

        FileOutputStream stream = new FileOutputStream(testFile);
        stream.write("This is just not empty test file!".getBytes());
        stream.close();

        return testFile;
    }
}
