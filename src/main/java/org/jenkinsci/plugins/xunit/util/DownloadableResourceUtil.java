package org.jenkinsci.plugins.xunit.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
        URL archive = new URL(url);;

        URLConnection con = ProxyConfiguration.open(archive);
        con.connect();

        if (con instanceof HttpURLConnection) {
            HttpURLConnection httpCon = (HttpURLConnection) con;
            int responseCode = httpCon.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED || responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Impossible to download resource " + archive.toExternalForm() + " due to server error: " + responseCode);
            }
        }

        try (InputStream in = archive.getProtocol().startsWith("http") ? ProxyConfiguration.getInputStream(archive) : con.getInputStream()) {
            return IOUtils.toString(in);
        }
    }
    
}
