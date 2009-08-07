package com.thalesgroup.hudson.plugins.xunit.types;

public class NUnitDescriptor extends TypeDescriptor{

    public static final GallioDescriptor DESCRIPTOR = new GallioDescriptor();

    public NUnitDescriptor() {
        super("nunit", "NUnit",  "nunit-to-junit.xsl");
    }
}
