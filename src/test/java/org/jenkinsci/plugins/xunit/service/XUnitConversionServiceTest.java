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

import static org.mockito.Mockito.*;

import java.io.File;

import org.jenkinsci.plugins.xunit.types.JUnitInputMetric;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.Issue;

public class XUnitConversionServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Issue("JENKINS-48945")
    @Test
    public void verify_that_report_file_name_does_clashes() throws Exception {
        File destFolder = folder.newFolder();
        File inputFile = folder.newFile("com.acme.EKOM02XTest");

        XUnitToolInfo toolInfo = mock(XUnitToolInfo.class);
        when(toolInfo.getInputMetric()).thenReturn(new JUnitInputMetric());

        XUnitConversionService service = new XUnitConversionService(mock(XUnitLog.class));
        File reportFile = service.convert(toolInfo, inputFile, destFolder);
        File reportFile2 = service.convert(toolInfo, inputFile, destFolder);
        Assert.assertNotEquals(reportFile.getAbsolutePath(), reportFile2.getAbsolutePath());
    }
}
