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
