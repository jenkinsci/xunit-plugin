package com.thalesgroup.hudson.plugins.xunit.types;

public class GallioDescriptor extends TypeDescriptor{

    public static final GallioDescriptor DESCRIPTOR = new GallioDescriptor();

    public GallioDescriptor() {
        super("gallio", "Gallio",  "gallio-to-junit.xsl");
    }
}
