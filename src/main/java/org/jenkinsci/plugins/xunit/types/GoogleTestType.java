package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author David Hallas
 */
public class GoogleTestType extends TestType {

    @DataBoundConstructor
    public GoogleTestType(String pattern, boolean ignoreNoResultFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, ignoreNoResultFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class GoogleTestTypeDescriptor extends TestTypeDescriptor<GoogleTestType> {

        public GoogleTestTypeDescriptor() {
            super(GoogleTestType.class, GoogleTestInputMetric.class);
        }

    }

}
