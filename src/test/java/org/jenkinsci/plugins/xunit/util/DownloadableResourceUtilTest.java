package org.jenkinsci.plugins.xunit.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DownloadableResourceUtilTest {

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    @Test
    public void test_if_is_url() throws Exception {
        Assert.assertFalse(DownloadableResourceUtil.isURL("file"));
        Assert.assertFalse(DownloadableResourceUtil.isURL("/foo.xml"));
        Assert.assertTrue(DownloadableResourceUtil.isURL("file:///foo.xml"));
        Assert.assertTrue(DownloadableResourceUtil.isURL("http://www.acme.com/foo.xml"));
        Assert.assertTrue(DownloadableResourceUtil.isURL("ftp://ftp.acme.com/foo.xml"));
    }

    @Test
    public void test_download() throws Exception {
        File file = fileRule.newFile();
        FileUtils.writeStringToFile(file, "test");
        String fileURL = file.toURI().toURL().toExternalForm();
        Assert.assertEquals("test", DownloadableResourceUtil.download(fileURL));
    }


}