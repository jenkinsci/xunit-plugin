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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thalesgroup.hudson.plugins.xunit.AbstractWorkspaceTest;
import com.thalesgroup.hudson.plugins.xunit.XUnitConfig;
import com.thalesgroup.hudson.plugins.xunit.model.TypeConfig;
import com.thalesgroup.hudson.plugins.xunit.types.AUnitDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.BoostTestDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.CppUnitDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.GallioDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.MSTestDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.NUnitDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.TypeDescriptor;
import com.thalesgroup.hudson.plugins.xunit.types.UnitTestDescriptor;

public class XUnitTransformerTest extends AbstractWorkspaceTest{

	private XUnitTransformer xUnitTransformer;
	private BuildListener listener;
	private FilePath junitOutputPath;
	private VirtualChannel channel;
	
	@Before
	public void initialize() throws Exception{
		listener = mock(BuildListener.class);
        when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream())); 
		junitOutputPath = new FilePath(new File(parentFile,"junitOutput"));
        if (junitOutputPath.exists()) {
        	junitOutputPath.deleteRecursive();
        }
        junitOutputPath.mkdirs();		
		channel=mock(VirtualChannel.class);
		super.createWorkspace();
	}
	
	@After
	public void tearDown() throws Exception{
		super.deleteWorkspace();
	}
	
	@Test
	public void testEmptyConfig() throws Exception{		
		XUnitConfig xUnitConfig= new XUnitConfig();		
		xUnitTransformer = new XUnitTransformer(listener, xUnitConfig, junitOutputPath);
		Boolean result = xUnitTransformer.invoke(new File(workspace.toURI()), channel);
		Assert.assertFalse("With an empty configuration, there is an error.", result);
	}
	

	private boolean processTransformer(XUnitConfig xUnitConfig) throws Exception{
		xUnitTransformer = new XUnitTransformer(listener, xUnitConfig, junitOutputPath);
		return xUnitTransformer.invoke(new File(workspace.toURI()), channel);
	}

	private TypeConfig getTypeConfig(XUnitConfig xUnitConfig, TypeDescriptor descriptor){
		for (TypeConfig config: xUnitConfig.getTestTools()){
			if (config.getName()==CppUnitDescriptor.DESCRIPTOR.getName()){
				return config;
			}
		}
		return null;
	}
	
	private XUnitConfig setupXUnitConfiWithPattern(TypeDescriptor descriptor, String pattern){
		XUnitConfig xUnitConfig= new XUnitConfig();
		TypeConfig config = getTypeConfig(xUnitConfig, descriptor);
        config.setPattern(pattern);
        return xUnitConfig;
	}

	
	private void wrongPattern(TypeDescriptor descriptor) throws Exception{				
		XUnitConfig xUnitConfig = setupXUnitConfiWithPattern(descriptor, "*.txt");        
		workspace.createTextTempFile("cppunit-report", ".xml", "content");    			
		Assert.assertFalse("With a wrong pattern, it have to be false",processTransformer(xUnitConfig));
	}	

	private void oneMatchWithWrongContent(TypeDescriptor descriptor) throws Exception{				
		XUnitConfig xUnitConfig = setupXUnitConfiWithPattern(descriptor, "*.xml");        
		workspace.createTextTempFile("report", ".xml", "content");    			
		try{
			processTransformer(xUnitConfig);
			Assert.assertFalse("With a wrong content, there is an exception",false);
		}
		catch (IOException2 ioe){
			Assert.assertTrue(true);
		}
	}	
	
	private void oneMatchWithValidContent(TypeDescriptor descriptor) throws Exception{				
		XUnitConfig xUnitConfig = setupXUnitConfiWithPattern(descriptor, "*.xml");        
		
		String content = XUnitXSLUtil.readXmlAsString("cppunit/source-cppunit.xml");
	
		
		File reportFile = new File(new File(workspace.toURI()),"report.xml");
		FileWriter fw = new FileWriter(reportFile);		
		fw.write(content);
		fw.close();
		
		processTransformer(xUnitConfig);
		  			
		Assert.assertTrue(true);
	}	
	
	@Test
	public void testCppUnitTool()throws Exception{
		wrongPattern(CppUnitDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(CppUnitDescriptor.DESCRIPTOR);
		oneMatchWithValidContent(CppUnitDescriptor.DESCRIPTOR);
	}
	@Test
	public void testBoostTestTool()throws Exception{
		wrongPattern(BoostTestDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(BoostTestDescriptor.DESCRIPTOR);
	}
	@Test
	public void testUnitTestTool()throws Exception{
		wrongPattern(UnitTestDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(UnitTestDescriptor.DESCRIPTOR);
	}
	@Test
	public void testMSTestTool()throws Exception{
		wrongPattern(MSTestDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(MSTestDescriptor.DESCRIPTOR);
	}
	@Test
	public void testGallioTool()throws Exception{
		wrongPattern(GallioDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(GallioDescriptor.DESCRIPTOR);
	}		
	@Test
	public void testNUnitTool()throws Exception{
		wrongPattern(NUnitDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(NUnitDescriptor.DESCRIPTOR);
	}	
	@Test
	public void testAUnitTool()throws Exception{
		wrongPattern(AUnitDescriptor.DESCRIPTOR);
		oneMatchWithWrongContent(AUnitDescriptor.DESCRIPTOR);
	}		
	
}
