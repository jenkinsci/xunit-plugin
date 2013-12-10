package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.dtkit.metrics.model.InputMetric;
import com.thalesgroup.dtkit.metrics.model.InputMetricException;
import com.thalesgroup.dtkit.metrics.model.InputMetricFactory;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class CustomType extends TestType {

    private String customXSL;

    @DataBoundConstructor
    @SuppressWarnings("unused")
    public CustomType(String pattern, String customXSL, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
        this.customXSL = customXSL;
    }

    @SuppressWarnings("unused")
    public String getCustomXSL() {
        return customXSL;
    }

    @Extension
    public static class CustomInputMetricDescriptor extends TestTypeDescriptor<CustomType> {

        public CustomInputMetricDescriptor() {
            super(CustomType.class, null);
        }

        @Override
        public String getId() {
            return this.getClass().getName();
        }

        @Override
        public InputMetric getInputMetric() {
            try {
                return InputMetricFactory.getInstance(CustomInputMetric.class);
            } catch (InputMetricException e) {
                throw new RuntimeException("Can't create the inputMetric object for the class " + CustomInputMetric.class);
            }
        }

        public boolean isCustomType() {
            return true;
        }
    }
}
