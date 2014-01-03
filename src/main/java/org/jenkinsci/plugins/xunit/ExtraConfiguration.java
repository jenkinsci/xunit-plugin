package org.jenkinsci.plugins.xunit;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class ExtraConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long testTimeMargin;

    public ExtraConfiguration(long testTimeMargin) {
        this.testTimeMargin = testTimeMargin;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }
}
