package org.jenkinsci.plugins.xunit;

/**
 * @author Gregory Boissinot
 */
public class ExtraConfiguration {

    private final long testTimeMargin;

    public ExtraConfiguration(long testTimeMargin) {
        this.testTimeMargin = testTimeMargin;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }
}
