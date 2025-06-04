/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Nikolas Falco
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

import org.jenkinsci.plugins.xunit.types.JUnitInputMetric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.jvnet.hudson.test.Issue;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XUnitConversionServiceTest {

    @TempDir
    private File folder;

    @Issue("JENKINS-48945")
    @Test
    void verify_that_report_file_name_does_clashes() throws Exception {
        File destFolder = newFolder(folder, "junit");
        File inputFile = newFile(folder, "com.acme.EKOM02XTest");

        XUnitToolInfo toolInfo = mock(XUnitToolInfo.class);
        when(toolInfo.getInputMetric()).thenReturn(new JUnitInputMetric());

        XUnitConversionService service = new XUnitConversionService(mock(XUnitLog.class));
        File reportFile = service.convert(toolInfo, inputFile, destFolder);
        File reportFile2 = service.convert(toolInfo, inputFile, destFolder);
        assertNotEquals(reportFile.getAbsolutePath(), reportFile2.getAbsolutePath());
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }

    private static File newFile(File parent, String child) throws IOException {
        File result = new File(parent, child);
        if (!result.createNewFile()) {
            throw new IOException("Couldn't create file " + result);
        }
        return result;
    }
}
