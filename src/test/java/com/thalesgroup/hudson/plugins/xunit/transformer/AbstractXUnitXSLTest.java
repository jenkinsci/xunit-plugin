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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thalesgroup.hudson.plugins.xunit.XUnitConfig;
import com.thalesgroup.hudson.plugins.xunit.types.TypeDescriptor;

public class AbstractXUnitXSLTest {

	private TypeDescriptor descriptor;
	
	protected AbstractXUnitXSLTest(TypeDescriptor descriptor){
		this.descriptor=descriptor;
	}
	
	@Before
	public void setUp() {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalizeWhitespace(true);
		XMLUnit.setIgnoreComments(true);
	}

	protected InputSource getInputSource(TypeDescriptor descriptor) {
		return new InputSource(this.getClass().getResourceAsStream(
				XUnitConfig.TOOLS.get(descriptor.getName()).getXslPath()));
	}

	protected void processTransformation(String source, String target)
			throws IOException, TransformerException, SAXException {
		
		Transform myTransform = new Transform(new InputSource(this.getClass()
				.getResourceAsStream(source)),getInputSource(descriptor));
		Diff myDiff = new Diff(XUnitXSLUtil.readXmlAsString(target), myTransform);
		assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
	}



}
