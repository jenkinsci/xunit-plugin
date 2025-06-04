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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for XUnitValidationService class.
 *
 * @author Maciek Siemczyk
 */
class XUnitValidationServiceTest {

    /**
     * Helper class for creating temporary workspace.
     */
    @TempDir
    private File folderRule;

    /**
     * System Under Test (SUT).
     */
    private XUnitValidationService xUnitValidationService;

    @BeforeEach
    void setUp() {
        xUnitValidationService = new XUnitValidationService(mock(XUnitLog.class));
    }

    @Test
    void CheckFileIsNotEmpty_GivenEmptyFile_ReturnsFalse() throws Exception {
        File testFile = new File(newFolder(folderRule, "junit"), "empty.txt");
        boolean created = testFile.createNewFile();
        assertTrue(created);

        assertFalse(xUnitValidationService.checkFileIsNotEmpty(testFile),
                "CheckFileIsNotEmpty returned true.");
    }

    @Test
    void CheckFileIsNotEmpty_GivenNotEmptyFile_ReturnsTrue() throws Exception {
        File testFile = createNotEmtyFile();

        assertTrue(xUnitValidationService.checkFileIsNotEmpty(testFile),
                "CheckFileIsNotEmpty returned false.");
    }

    /**
     * Helper method that will create a file with some text in it.
     *
     * @return Created file.
     * @throws Exception when there is a problem with writing to the file.
     */
    private File createNotEmtyFile() throws Exception {
        File testFile = new File(newFolder(folderRule, "junit"), "notempty.txt");

        FileOutputStream stream = new FileOutputStream(testFile);
        stream.write("This is just not empty test file!".getBytes());
        stream.close();

        return testFile;
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
