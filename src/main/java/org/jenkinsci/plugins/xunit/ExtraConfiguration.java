/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.xunit;

import static org.jenkinsci.plugins.xunit.XUnitDefaultValues.FOLLOW_SYMLINK;

import java.io.Serializable;

/**
 * Lists all advanced configuration for XUnit.
 * <p>
 * This class is used only by UI.
 *
 * @author Gregory Boissinot
 * @author Nikolas Falco
 */
public class ExtraConfiguration implements Serializable {
    
    static class ExtraConfigurationBuilder {
        private ExtraConfiguration configuration;

        public ExtraConfigurationBuilder(ExtraConfiguration configuration) {
            this.configuration = new ExtraConfiguration(configuration.testTimeMargin, configuration.reduceLog, configuration.sleepTime, configuration.followSymlink);
        }

        public ExtraConfigurationBuilder testTimeMargin(long testTimeMargin) {
            configuration.testTimeMargin = testTimeMargin;
            return this;
        }

        public ExtraConfigurationBuilder sleepTime(long sleepTime) {
            configuration.sleepTime = sleepTime;
            return this;
        }

        public ExtraConfigurationBuilder reduceLog(boolean reduceLog) {
            configuration.reduceLog = reduceLog;
            return this;
        }

        public ExtraConfigurationBuilder followSymlink(boolean followSymlink) {
            configuration.followSymlink = followSymlink;
            return this;
        }

        public ExtraConfiguration build() {
            return configuration;
        }
    }

    private static final long serialVersionUID = 1L;

    private long testTimeMargin;
    private long sleepTime;
    private boolean reduceLog;
    /*
     * Boolean to be backward compatible when unmarshall by XStream so we can
     * understand when value is missing in XML file
     */
    private Boolean followSymlink;

    static ExtraConfigurationBuilder withConfiguration(ExtraConfiguration configuration) {
        return new ExtraConfigurationBuilder(configuration);
    }

    public ExtraConfiguration(long testTimeMargin, boolean reduceLog, long sleepTime, boolean followSymlink) {
        this.testTimeMargin = testTimeMargin;
        this.sleepTime = sleepTime;
        this.reduceLog = reduceLog;
        this.followSymlink = followSymlink;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public boolean isReduceLog() {
        return reduceLog;
    }

    public boolean isFollowSymlink() {
        return followSymlink;
    }

    /**
     * Migrate old data
     *
     * @see <a href=
     *      "https://wiki.jenkins-ci.org/display/JENKINS/Hint+on+retaining+backward+compatibility">
     *      Jenkins wiki entry on the subject</a>
     *
     * @return must be always 'this'
     */
    private Object readResolve() {
        if (followSymlink == null) {
            followSymlink = FOLLOW_SYMLINK;
        }
        return this;
    }
}
