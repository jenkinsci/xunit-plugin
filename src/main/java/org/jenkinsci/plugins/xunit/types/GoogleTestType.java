package org.jenkinsci.plugins.xunit.types;


import hudson.Extension;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author David Hallas
 */
public class GoogleTestType extends TestType {

    @DataBoundConstructor
    public GoogleTestType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class GoogleTestTypeDescriptor extends TestTypeDescriptor<GoogleTestType> {

        public GoogleTestTypeDescriptor() {
            super(GoogleTestType.class, GoogleTestInputMetric.class);
        }

    }

}
