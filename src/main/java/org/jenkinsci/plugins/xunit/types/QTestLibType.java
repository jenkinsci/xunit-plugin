package org.jenkinsci.plugins.xunit.types;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class QTestLibType extends TestType {

    @DataBoundConstructor
    public QTestLibType(String pattern, boolean ignoreNoResultFiles, boolean failIfNotNew, boolean deleteOutputFiles, boolean stopProcessingIfError) {
        super(pattern, ignoreNoResultFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @Extension
    public static class QTestLibTypeDescriptor extends TestTypeDescriptor<QTestLibType> {

        public QTestLibTypeDescriptor() {
            super(QTestLibType.class, QTestLibInputMetric.class);
        }

    }

}

