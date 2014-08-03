package org.jenkinsci.plugins.xunit.types;

import hudson.Extension;
import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class MbUnitType extends TestType {

    @DataBoundConstructor
    public MbUnitType(String pattern, boolean skipNoTestFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class MbUnitTypeDescriptor extends TestTypeDescriptor<MbUnitType> {

        public MbUnitTypeDescriptor() {
            super(MbUnitType.class, MbUnitInputMetric.class);
        }

    }
}

