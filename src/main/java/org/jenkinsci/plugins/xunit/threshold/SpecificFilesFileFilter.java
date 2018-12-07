package org.jenkinsci.plugins.xunit.threshold;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import java.io.File;
import java.io.Serializable;


/*
  Filter class for recursively search a specific file in a directory tree.
  The class needs to be serializable to work for remote workspace searches.
 */
public class SpecificFilesFileFilter extends DirectoryFileFilter implements Serializable {
    public static final IOFileFilter DIRECTORY = new SpecificFilesFileFilter();
    public static final IOFileFilter INSTANCE;
    private String testFileName;

    protected SpecificFilesFileFilter() {
    }

    public SpecificFilesFileFilter(String testFileName)
    {
        this.testFileName = testFileName;
    }

    public boolean accept(File file) {
        return file.isDirectory() || file.getName().contains(this.testFileName);
    }

    static {
        INSTANCE = DIRECTORY;
    }
}