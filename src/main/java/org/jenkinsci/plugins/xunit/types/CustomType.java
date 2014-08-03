package org.jenkinsci.plugins.xunit.types;

import hudson.Extension;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class CustomType extends TestType {

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomType(String pattern, String customXSL, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
        this.customXSL = customXSL;
    }

    @SuppressWarnings("unused")
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class CustomInputMetricDescriptor extends TestTypeDescriptor<CustomType> {

        public CustomInputMetricDescriptor() {
            super(CustomType.class, CustomInputMetric.class);
        }

        public boolean isCustomType() {
            return true;
        }
    }

}
