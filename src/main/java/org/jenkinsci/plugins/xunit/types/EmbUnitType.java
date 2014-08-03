package org.jenkinsci.plugins.xunit.types;


import hudson.Extension;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class EmbUnitType extends TestType {

    @DataBoundConstructor
    public EmbUnitType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class EmbUnitTypeDescriptor extends TestTypeDescriptor<EmbUnitType> {

        public EmbUnitTypeDescriptor() {
            super(EmbUnitType.class, EmbUnitInputMetric.class);
        }

    }
}
