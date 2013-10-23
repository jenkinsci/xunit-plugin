/*******************************************************************************
 * Copyright (c) 2013 Thales Corporate Services SAS                             *
 * Author : Maciek Siemczyk                                                     *
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

import java.io.*;
//import java.nio.file.*;
//import java.nio.file.FileSystem;
import org.junit.*;
import com.thalesgroup.hudson.plugins.xunit.transformer.TempWorkspace;

/**
 * Unit tests for XUnitValidationService class.
 *
 * @author Maciek Siemczyk
 * 
 * @note Two of the unit tests were disabled as they require Java 7 API.
 */
public class XUnitValidationServiceTest
{
    /**
     * Helper class for creating temporary workspace. 
     */
    @Rule
    public TempWorkspace tempWorkspace = new TempWorkspace();
    
    /**
     * System Under Test (SUT).
     */
    private XUnitValidationService xUnitValidationService = new XUnitValidationService();

    @Test
    public void CheckFileIsNotEmpty_GivenEmptyFile_ReturnsFalse() throws Exception
    {
        File testFile = new File(tempWorkspace.getDir(), "empty.txt");
        testFile.createNewFile();
        
        Assert.assertFalse(
            "CheckFileIsNotEmpty returned true.",
            xUnitValidationService.checkFileIsNotEmpty(testFile));
    }
    
    @Test
    public void CheckFileIsNotEmpty_GivenNotEmptyFile_ReturnsTrue() throws Exception
    {
        File testFile = CreateNotEmtyFile();
        
        Assert.assertTrue(
            "CheckFileIsNotEmpty returned false.",
            xUnitValidationService.checkFileIsNotEmpty(testFile));
    }
    
    @Test
    @Ignore
    public void CheckFileIsNotEmpty_GivenLinkToEmptyFile_ReturnsFalse() throws Exception
    {
        /**
         * This unit test is disabled as it required Java 7 API.
         
        FileSystem filesystem = FileSystems.getDefault();
        
        File targetFile = new File(tempWorkspace.getDir(), "empty.txt");;
        targetFile.createNewFile();
        
        Path targetPath = filesystem.getPath(targetFile.getPath());
        Path linkPath = filesystem.getPath(tempWorkspace.getDir().getPath(), "link.txt");
        
        Files.createSymbolicLink(linkPath, targetPath);
        
        Assert.assertFalse(
            "CheckFileIsNotEmpty returned true.",
            xUnitValidationService.checkFileIsNotEmpty(linkPath.toFile()));
        */
    }
    
    @Test
    @Ignore
    public void CheckFileIsNotEmpty_GivenLinkToNotEmptyFile_ReturnsTrue() throws Exception
    {
        /**
         * This unit test is disabled as it required Java 7 API.

        FileSystem filesystem = FileSystems.getDefault();
        
        File targetFile = CreateNotEmtyFile();
        Path targetPath = filesystem.getPath(targetFile.getPath());
        Path linkPath = filesystem.getPath(tempWorkspace.getDir().getPath(), "link.txt");
        
        Files.createSymbolicLink(linkPath, targetPath);
        
        Assert.assertTrue(
            "CheckFileIsNotEmpty returned false.",
            xUnitValidationService.checkFileIsNotEmpty(linkPath.toFile()));
        */
    }
    
    /**
     * Helper method that will create a file with some text in it.
     * 
     * @return Created file.
     * 
     * @throws Exception when there is a problem with writing to the file.
     */
    private File CreateNotEmtyFile() throws Exception
    {
        File testFile = new File(tempWorkspace.getDir(), "notempty.txt");
        
        FileOutputStream stream = new FileOutputStream(testFile);
        stream.write("This is just not empty test file!".getBytes());
        stream.close();
        
        return testFile;
    }
}
