package org.jenkinsci.plugins.xunit.threshold;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import java.io.File;
import java.io.Serializable;


// the filter class needs to be serializable to work for remote workspace searches
public class QuarantinedFilesFileFilter extends DirectoryFileFilter implements Serializable {
    public static final IOFileFilter DIRECTORY = new QuarantinedFilesFileFilter();
    public static final IOFileFilter INSTANCE;
    static final String QUARANTINED_TEST_FILE = "quarantined-tests.json";

    protected QuarantinedFilesFileFilter() {
    }

    public boolean accept(File file) {
        return file.isDirectory() || file.getName().contains(QUARANTINED_TEST_FILE);
    }

    static {
        INSTANCE = DIRECTORY;
    }
}