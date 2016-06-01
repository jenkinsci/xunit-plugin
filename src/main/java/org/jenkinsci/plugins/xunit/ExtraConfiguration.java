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

import java.io.Serializable;

import com.google.inject.Inject;
import org.jenkinsci.plugins.xunit.service.XUnitLog;

/**
 * @author Gregory Boissinot
 */
public class ExtraConfiguration implements Serializable {

    private static final long serialVersionUID = 2L;

    private final long testTimeMargin;
    private final XUnitLog.Level logLevel;

    public ExtraConfiguration(long testTimeMargin, XUnitLog.Level logLevel) {
        this.testTimeMargin = testTimeMargin;
        this.logLevel = logLevel;
    }

    public XUnitLog.Level getLogLevel() {
        return logLevel;
    }

    public long getTestTimeMargin() {
        return testTimeMargin;
    }
}
