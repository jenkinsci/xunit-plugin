package org.jenkinsci.plugins.xunit.service;

import hudson.Util;
import org.junit.rules.ExternalResource;

import java.io.File;


public class TempWorkspace extends ExternalResource {

    private File dir;

    @Override
    protected void before() throws Throwable {
        dir = Util.createTempDir();
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void after() {
        dir.delete();
    }

    public File getDir() {
        return dir;
    }
}
