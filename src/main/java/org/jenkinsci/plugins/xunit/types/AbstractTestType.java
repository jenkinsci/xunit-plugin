/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020, Falco Nikolas
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
package org.jenkinsci.plugins.xunit.types;

import org.jenkinsci.lib.dtkit.type.TestType;
import org.kohsuke.stapler.DataBoundSetter;

@SuppressWarnings("serial")
abstract class AbstractTestType extends TestType {

    AbstractTestType(String pattern) {
        super(pattern);
    }

    AbstractTestType(String pattern,
                     boolean skipNoTestFiles,
                     boolean failIfNotNew,
                     boolean deleteOutputFiles,
                     boolean stopProcessingIfError) {
        super(pattern, skipNoTestFiles, failIfNotNew, deleteOutputFiles, stopProcessingIfError);
    }

    @DataBoundSetter
    @Override
    public void setDeleteOutputFiles(boolean deleteOutputFiles) {
        super.setDeleteOutputFiles(deleteOutputFiles);
    }

    @DataBoundSetter
    @Override
    public void setFailIfNotNew(boolean failIfNotNew) {
        super.setFailIfNotNew(failIfNotNew);
    }

    @DataBoundSetter
    @Override
    public void setExcludesPattern(String excludesPattern) {
        super.setExcludesPattern(excludesPattern);
    }

    @DataBoundSetter
    @Override
    public void setSkipNoTestFiles(boolean skipNoTestFiles) {
        super.setSkipNoTestFiles(skipNoTestFiles);
    }

    @DataBoundSetter
    @Override
    public void setStopProcessingIfError(boolean stopProcessingIfError) {
        super.setStopProcessingIfError(stopProcessingIfError);
    }
}
