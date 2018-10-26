/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018, Nikolas Falco
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

import hudson.Util;

public final class XUnitUtil {

    private XUnitUtil() {
    }

    /**
     * Parses the string argument as a signed decimal {@code long}. In case the
     * given value is empty or null, the default value is returned.
     * 
     * @param value
     *            to parse
     * @param defaultValue
     *            in case argument is not valid or empty or null
     * @return the {@code long} represented by the argument in decimal,
     *         otherwise default value if argument is null, empty or invalid.
     */
    public static long parsePositiveLong(String value, long defaultValue) {
        long result = defaultValue;
        if (Util.fixEmptyAndTrim(value) != null) {
            try {
                result = Math.abs(Long.parseLong(value));
            } catch (NumberFormatException e) {
                // leave result valued with default value
            }
        }
        return result;
    }
}
