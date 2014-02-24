package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Extension;
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
