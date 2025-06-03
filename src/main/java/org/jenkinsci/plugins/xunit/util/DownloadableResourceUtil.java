/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2010, Gregory Boissinot, Nikola Falco
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import hudson.ProxyConfiguration;

public class DownloadableResourceUtil {

    private DownloadableResourceUtil() {
    }

    public static boolean isURL(String url) {
        URL urlObj = null;
        try {
            urlObj = new URL(url);
        } catch(MalformedURLException e) {
            // it's not an URL
        }
        return urlObj != null;
    }

    public static String download(String url) throws IOException {
        URL archive = new URL(url);

        URLConnection con = ProxyConfiguration.open(archive);
        con.connect();

        if (con instanceof HttpURLConnection httpCon) {
            int responseCode = httpCon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED || responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Can not download resource " + archive.toExternalForm() + " due to server error: " + responseCode);
            }
        }

        try (InputStream in = archive.getProtocol().startsWith("http") ? ProxyConfiguration.getInputStream(archive) : con.getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

}
