package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class CTestType extends TestType {

    @DataBoundConstructor
    public CTestType(String pattern, boolean ignoreNoResultFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, ignoreNoResultFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class CTestTypeDescriptor extends TestTypeDescriptor<CTestType> {

        public CTestTypeDescriptor() {
            super(CTestType.class, CTestInputMetric.class);
        }

    }

}

