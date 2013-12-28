package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class CustomType extends TestType {

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomType(String pattern, String customXSL, boolean ignoreNoResultFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, ignoreNoResultFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
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
