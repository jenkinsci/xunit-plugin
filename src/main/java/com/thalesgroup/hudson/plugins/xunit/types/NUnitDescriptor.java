package com.thalesgroup.hudson.plugins.xunit.types;

public class NUnitDescriptor extends TypeDescriptor{

    public static final NUnitDescriptor DESCRIPTOR = new NUnitDescriptor();

    public NUnitDescriptor() {
        super("nunit", "NUnit",  "nunit-to-junit.xsl");
    }
}
