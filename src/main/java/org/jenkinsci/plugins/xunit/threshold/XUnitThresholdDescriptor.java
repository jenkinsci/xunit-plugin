package org.jenkinsci.plugins.xunit.threshold;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * @author Gregory Boissinot
 */
public abstract class XUnitThresholdDescriptor<T extends XUnitThreshold> extends Descriptor<XUnitThreshold> {

    public XUnitThresholdDescriptor(Class<? extends XUnitThreshold> clazz) {
        super(clazz);
    }

    @SuppressWarnings("unused")
    public static DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> all() {
        return Hudson.getInstance().getDescriptorList(XUnitThreshold.class);
    }

    @SuppressWarnings("unused")
    public abstract String getUnstableThresholdImgTitle();

    @SuppressWarnings("unused")
    public abstract String getUnstableNewThresholdImgTitle();

    @SuppressWarnings("unused")
    public abstract String getFailureThresholdImgTitle();

    @SuppressWarnings("unused")
    public abstract String getFailureNewThresholdImgTitle();

    @SuppressWarnings("unused")
    public abstract String getThresholdHelpMessage();

}
