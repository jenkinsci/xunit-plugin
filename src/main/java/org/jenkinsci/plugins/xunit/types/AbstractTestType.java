package org.jenkinsci.plugins.xunit.types;

import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundSetter;

@SuppressWarnings("serial")
abstract class AbstractTestType extends TestType {

    AbstractTestType(String pattern) {
        super(pattern);
    }

    AbstractTestType(String pattern,
                     boolean skipNoTestFiles,
                     boolean failIfNotNew,
                     boolean deleteOutputFiles,
                     boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @DataBoundSetter
    @Override
    public void setDeleteOutputFiles(boolean deleteOutputFiles) {
        super.setDeleteOutputFiles(deleteOutputFiles);
    }

    @DataBoundSetter
    @Override
    public void setFailIfNotNew(boolean failIfNotNew) {
        super.setFailIfNotNew(failIfNotNew);
    }

    @DataBoundSetter
    @Override
    public void setExcludesPattern(String excludesPattern) {
        super.setExcludesPattern(excludesPattern);
    }

    @DataBoundSetter
    @Override
    public void setSkipNoTestFiles(boolean skipNoTestFiles) {
        super.setSkipNoTestFiles(skipNoTestFiles);
    }

    @DataBoundSetter
    @Override
    public void setStopProcessingIfError(boolean stopProcessingIfError) {
        super.setStopProcessingIfError(stopProcessingIfError);
    }
}
