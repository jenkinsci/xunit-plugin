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