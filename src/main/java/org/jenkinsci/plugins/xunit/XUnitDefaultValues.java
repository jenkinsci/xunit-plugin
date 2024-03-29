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

/**
 * @author Gregory Boissinot
 */
public class XUnitDefaultValues {

    public static final String GENERATED_JUNIT_DIR = "generatedJUnitFiles";

    public static final int MODE_PERCENT = 2;

    public static final int TEST_REPORT_TIME_MARGING = 3000; // default to 3000

    public static final int PROCESSING_SLEEP_TIME = 10;
    
    public static final boolean FOLLOW_SYMLINK = true;

    public static final boolean JUNIT_FILE_REDUCE_LOG = true;

    public static final String JUNIT_FILE_PREFIX = "TEST-";

    public static final String JUNIT_FILE_EXTENSION = ".xml";    

    public static final String DEFAULT_CHECKS_NAME = "Tests";

    public static final boolean SKIP_PUBLISHING_CHECKS = true;

}