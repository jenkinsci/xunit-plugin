package org.jenkinsci.plugins.xunit;

import com.thalesgroup.dtkit.metrics.hudson.api.descriptor.TestTypeDescriptor;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import hudson.DescriptorExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Hudson;
import hudson.model.Items;

import java.util.Iterator;

/**
 * @author Gregory Boissinot
 */
public class AliasInitializer {

    @Initializer(before = InitMilestone.PLUGINS_STARTED)
    @SuppressWarnings("unused")
    public static void addAliases() {
        Items.XSTREAM.alias("xunit", com.thalesgroup.hudson.plugins.xunit.XUnitPublisher.class);
        DescriptorExtensionList<TestType, TestTypeDescriptor<TestType>> extensionList = Hudson.getInstance().getDescriptorList(TestType.class);
        for (Iterator<TestTypeDescriptor<TestType>> it = extensionList.iterator(); it.hasNext();) {
            Class<? extends TestType> classType = it.next().clazz;
            String className = getClassName(classType);
            Items.XSTREAM.alias(className, classType);
        }
    }

    private static String getClassName(Class<? extends TestType> classType) {
        String name = classType.getName();
        String packageSep = ".";
        if (name.contains(packageSep)) {
            return name.substring(name.lastIndexOf(packageSep) + 1);
        }
        return name;
    }
}
