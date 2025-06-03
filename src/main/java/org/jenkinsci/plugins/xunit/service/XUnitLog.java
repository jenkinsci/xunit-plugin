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

import java.io.Serial;
import java.io.Serializable;

import com.google.inject.Inject;

import hudson.model.TaskListener;

public class XUnitLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private TaskListener buildListener;

    @Inject
    public XUnitLog(TaskListener buildListener) {
        this.buildListener = buildListener;
    }

    /**
     * Log an info output to the console logger.
     *
     * @param message The message to be outputted
     */
    public void info(String message) {
        buildListener.getLogger().println("INFO: " + message);
    }

    /**
     * Log an error output to the console logger.
     *
     * @param message The message to be outputted
     */
    public void error(String message) {
        buildListener.error(message);
    }

    /**
     * Log a warning output to the console logger.
     *
     * @param message The message to be outputted
     */
    public void warn(String message) {
        buildListener.getLogger().println("WARNING: " + message);
    }

}
