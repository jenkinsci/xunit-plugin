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

package org.jenkinsci.plugins.xunit.service;

import com.google.inject.Inject;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.xunit.ExtraConfiguration;
import org.jenkinsci.plugins.xunit.XUnitDefaultValues;

import java.io.Serializable;

public class XUnitLog implements Serializable {

    public enum Level {
        INFO, WARN, ERROR;

        public boolean isActive(Level testedLevel) {
            return testedLevel.ordinal() >= ordinal();
        }

        public static Level fromString(String value) {
            if (value != null) {
                return Enum.valueOf(Level.class, value);
            }
            return INFO;
        }
    }

    private TaskListener buildListener;
    private ExtraConfiguration extraConfiguration;

    @Inject
    @SuppressWarnings("unused")
    void set(TaskListener buildListener) {
        this.buildListener = buildListener;
    }

    @Inject
    @SuppressWarnings("unused")
    void set(ExtraConfiguration extraConfiguration) {
        this.extraConfiguration = extraConfiguration;
    }

    private XUnitLog.Level currentLevel() {
        return extraConfiguration == null ? XUnitDefaultValues.LOGGING_LEVEL : extraConfiguration.getLogLevel();
    }

    private boolean logError() {
        return currentLevel().isActive(Level.ERROR);
    }

    private boolean logWarn() {
        return currentLevel().isActive(Level.WARN);
    }

    private boolean logInfo() {
        return currentLevel().isActive(Level.INFO);
    }

    /**
     * Log an info output to the console logger
     *
     * @param message The message to be outputted
     */
    public void infoConsoleLogger(String message) {
        if (logInfo()) buildListener.getLogger().println("[xUnit] [INFO] - " + message);
    }

    /**
     * Log an error output to the console logger
     *
     * @param message The message to be outputted
     */
    public void errorConsoleLogger(String message) {
        if (logError()) buildListener.getLogger().println("[xUnit] [ERROR] - " + message);
    }

    /**
     * Log a warning output to the console logger
     *
     * @param message The message to be outputted
     */
    public void warningConsoleLogger(String message) {
        if (logWarn()) buildListener.getLogger().println("[xUnit] [WARNING] - " + message);
    }

}
