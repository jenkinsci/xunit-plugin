/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.xunit.transformer;

import com.thalesgroup.hudson.plugins.xunit.types.GallioType;
import org.junit.Test;


public class GallioXSLTest extends AbstractXUnitXSLTest {

    public GallioXSLTest() {
        super(GallioType.class);
    }

    @Test
    public void testTransformation() throws Exception {
        processTransformation("gallio/Gallio-simple.xml", "gallio/JUnit-simple.xml");
    }

    @Test
    public void testTransformationFailure() throws Exception {
        processTransformation("gallio/Gallio-failure.xml", "gallio/JUnit-failure.xml");
    }

    @Test
    public void testTransformationMultiNamespace() throws Exception {
        processTransformation("gallio/Gallio-multinamespace.xml", "gallio/JUnit-multinamespace.xml");
    }

    @Test
    public void testTransformedIgnored() throws Exception {
        processTransformation("gallio/Gallio-ignored.xml", "gallio/JUnit-ignored.xml");
    }

    @Test
    public void testTransformedIssue1077() throws Exception {
        processTransformation("gallio/Gallio-issue1077.xml", "gallio/JUnit-issue1077.xml");
    }


}
