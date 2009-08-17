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

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thalesgroup.hudson.plugins.xunit.XUnitConfig;
import com.thalesgroup.hudson.plugins.xunit.model.TypeConfig;
import com.thalesgroup.hudson.plugins.xunit.util.Messages;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private static final String JUNIT_FILE_POSTFIX = ".xml";
    private static final String JUNIT_FILE_PREFIX = "TEST-";


    private BuildListener listener;
    private XUnitConfig config = new XUnitConfig();
    private FilePath junitOutputPath = null;

    public XUnitTransformer(BuildListener listener, XUnitConfig config, FilePath junitOutputPath) {
        this.listener = listener;
        this.config = config;
        this.junitOutputPath = junitOutputPath;
    }


    /**
     * Test if the field is empty
     * @param field
     * @return
     */
    private boolean isEmpty(String field) {
        if (field== null){
            return true;
        }

        if (field.trim().isEmpty()){
            return true;
        }

        return false;
    }

    /**
     * Determines if the current custom test tool entries are not empty or blank
     * @param testTool
     * @return
     */
    private boolean isNotCompleteCustomConfigEntry(TypeConfig testTool){
        boolean result = isEmpty(testTool.getPattern());
        result = result || isEmpty(testTool.getLabel());
        result = result || isEmpty(testTool.getStylesheet());
        return result;
    }

    /**
     * Valid the current custom test tool entries
     * @param moduleRoot
     * @param testTool
     * @return
     */
    private boolean isValidCustomConfigEntry(File moduleRoot, TypeConfig testTool){

        boolean result = !isNotCompleteCustomConfigEntry(testTool);

        File stylesheetFile = new File(moduleRoot, testTool.getStylesheet());
        if (result && !stylesheetFile.exists()){
           String msg = "The custom stylesheet '" + testTool.getStylesheet() +"' for the tool '"+ testTool.getLabel()+"' doesn't exist.";
           Messages.log(listener,msg);
           return  false;
        }

        return result;
    }


    /**
     * Invocation
     * @param moduleRoot
     * @param channel
     * @return
     * @throws IOException
     */
    public Boolean invoke(File moduleRoot, VirtualChannel channel) throws IOException {

       try{

    	boolean isInvoked=false;   
    	   
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlDocumentBuilder = factory.newDocumentBuilder();
        Transformer writerTransformer = transformerFactory.newTransformer();

        //Supported tools
        for (TypeConfig testTool:config.getTestTools()){
            if (!isEmpty(testTool.getPattern())){
                isInvoked=true;
            	boolean result=processTool(moduleRoot, transformerFactory, xmlDocumentBuilder, writerTransformer, testTool, new StreamSource(this.getClass().getResourceAsStream(config.TOOLS.get(testTool.getName()).getXslPath())));
                if (!result){
                	return false;
            	}
            }            
        }

        //Custom tools
        for (TypeConfig testTool:config.getCustomTools()){
           if (isValidCustomConfigEntry(moduleRoot, testTool)){
               isInvoked=true;
        	   boolean result=processTool(moduleRoot, transformerFactory, xmlDocumentBuilder, writerTransformer, testTool, new StreamSource(new File(moduleRoot, testTool.getStylesheet())));
	           if (!result){
	           		return false;
	       	   }	           
           }
           else if ( isNotCompleteCustomConfigEntry(testTool)){
               String msg = "[ERROR] - There is an invalid configuration for the following entries '"
                    + testTool.getLabel() + "':'"+ testTool.getPattern() + "':'"+ testTool.getStylesheet() + "' into the custom testing frameworks section.";
               Messages.log(listener,msg);
               return false;
           }
        }
        
        if (!isInvoked){
            String msg = "[ERROR] - No test report files were found. Configuration error?";
            Messages.log(listener,msg);
            return false;
        }
        
     }
     catch (Exception e){
    	throw new IOException2("Problem on converting into JUnit reports.", e);
     }

     return true;

    }

    /**
     * Processing the current test tool
     * @param moduleRoot
     * @param transformerFactory
     * @param xmlDocumentBuilder
     * @param writerTransformer
     * @param testTool
     * @param stylesheet
     * @throws TransformerException
     * @throws IOException
     * @throws InterruptedException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private boolean processTool(File moduleRoot, TransformerFactory transformerFactory, DocumentBuilder xmlDocumentBuilder, Transformer writerTransformer, TypeConfig testTool, StreamSource stylesheet)
            throws TransformerException, IOException, InterruptedException  {

        Transformer toolXMLTransformer = transformerFactory.newTransformer(stylesheet);

        String[] resultFiles = findtReports(moduleRoot, testTool.getPattern());
        if (resultFiles.length==0){
            String msg = "[ERROR] - No test report file(s) were found with the pattern '"
                + testTool.getPattern() + "' relative to '"+ moduleRoot + "' for the testing framework '"+ testTool.getLabel() + "'."
                + "  Did you enter a pattern relative to the correct directory?"
                + "  Did you generate the result report(s) for '"+ testTool.getLabel() + "'?";
            Messages.log(listener,msg);
            return false;            
        }

        Messages.log(listener,"["+ testTool.getLabel()+ "] - Processing "+resultFiles.length+ " files with the pattern '"  + testTool.getPattern() + "' relative to '"+ moduleRoot + "'.");
        for (String resultFile: resultFiles){
        
        	File resultFilePathFile  = new File(moduleRoot, resultFile);
        	if (resultFilePathFile.length()==0){
        		//Ignore the empty result file (some reason)
        		String msg = "[WARNING] - The file '"+resultFilePathFile.getPath()+"' is empty. This file has been ignored.";
                Messages.log(listener,msg);
        		continue;
        	}        	        	
        	
            FilePath junitTargetFile = new FilePath(junitOutputPath,  "file"+resultFilePathFile.hashCode() + ".xml");
        	try{
	            toolXMLTransformer.transform(new StreamSource(resultFilePathFile), new StreamResult(new File(junitTargetFile.toURI())));
	            processJUnitFile(xmlDocumentBuilder, writerTransformer, junitTargetFile, junitOutputPath);
        	}
        	catch (TransformerException te){
                String msg = "[ERROR] - Couldn't convert the file '"+resultFilePathFile.getPath()+"' into a JUnit file.";
        		Messages.log(listener,msg);
                return false;
        	}
        	catch (SAXException te){
        		String msg = "[ERROR] - Couldn't split JUnit testsuites for the file '"+resultFile+"' into JUnit files with one testsuite.";
        		Messages.log(listener,msg);
                return false;
        	}
       }
        
       return true;
    }


    /**
     * Return all report files
     *
     * @param parentPath parent
     * @param pattern pattern to seach files
     * @return an array of strings
     */
    private String[] findtReports(File parentPath, String pattern)  {
        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        String[] xunitFiles = ds.getIncludedFiles();
        return xunitFiles;
    }


    /**
     * Processing the current junit file
     * @param xmlDocumentBuilder
     * @param writerTransformer
     * @param junitFile
     * @param junitOutputPath
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     */
    private void processJUnitFile(DocumentBuilder xmlDocumentBuilder, Transformer writerTransformer, FilePath junitFile, FilePath junitOutputPath)
                throws SAXException, IOException, TransformerException, InterruptedException {

        Document document = xmlDocumentBuilder.parse(new File(junitFile.toURI()));
        NodeList testsuitesNodeList =  document.getElementsByTagName("testsuites");
        if (testsuitesNodeList == null || testsuitesNodeList.getLength()==0){
            junitFile.renameTo(new FilePath(junitFile.getParent(), JUNIT_FILE_PREFIX+junitFile.getName()+JUNIT_FILE_POSTFIX));
            return;
        }
        splitJunitFile(writerTransformer, testsuitesNodeList, junitOutputPath);
    }

    /**
     * Segragate the current junit file
     * @param writerTransformer
     * @param testsuitesNodeList
     * @param junitOutputPath
     * @throws IOException
     * @throws InterruptedException
     * @throws TransformerException
     */
    private void splitJunitFile(Transformer writerTransformer, NodeList testsuitesNodeList, FilePath junitOutputPath) throws IOException, InterruptedException, TransformerException {
        NodeList elementsByTagName = ((Element) testsuitesNodeList.item(0)).getElementsByTagName("testsuite");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            DOMSource source = new DOMSource(element);
            FilePath junitOutputFile = new FilePath(junitOutputPath, JUNIT_FILE_PREFIX + element.getAttribute("name") + JUNIT_FILE_POSTFIX);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(junitOutputFile.toURI()));
            try {
                StreamResult result = new StreamResult(fileOutputStream);
                writerTransformer.transform(source, result);
            } finally {
                fileOutputStream.close();
            }
        }
    }

}
