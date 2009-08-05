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

package com.thalesgroup.hudson.plugins.xunit;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

import com.thalesgroup.hudson.plugins.convert2Junit.util.Messages;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private static final String JUNIT_FILE_POSTFIX = ".xml";
    private static final String JUNIT_FILE_PREFIX = "TEST-";


    private BuildListener listener;
    private Convert2JunitConfig config = new Convert2JunitConfig();
    private  FilePath junitOutputPath = null;

    public Convert2JunitTransformer(BuildListener listener, Convert2JunitConfig config, FilePath junitOutputPath) {
        this.listener = listener;
        this.config = config;
        this.junitOutputPath = junitOutputPath;
    }

    public Boolean invoke(File moduleRoot, VirtualChannel channel) throws IOException {

        for (TypeConfig testTool:config.getTestTools()){
            try{

                if (testTool.isFill()){

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer toolXMLTransformer = transformerFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(config.TOOLS.get(testTool.getName()).getXslPath())));

                    String[] resultFiles = findtReports(moduleRoot, testTool.getPattern());
                    if (resultFiles.length==0){
                            String msg = "No test report file(s) were found with the pattern '"
                                + testTool.getPattern() + "' relative to '"+ moduleRoot + "' for the tool '"+ testTool.getName() + "'."
                                + "  Did you enter a pattern relative to the correct directory?"
                                + "  Did you generate the XML report(s) for CppUnit?";
                            Messages.log(listener,msg);
                            return false;
                    }

                    Messages.log(listener,"Processing "+resultFiles.length+ " files with the pattern '"  + testTool.getPattern() + "' relative to '"+ moduleRoot + "' for the tool '"+ testTool.getName() + "'.");
                    for (String resultFile: resultFiles){
                        FilePath resultFilePath =  new FilePath(new File(moduleRoot, resultFile));
                        performTransformation(toolXMLTransformer, junitOutputPath,  resultFilePath ) ;
                   }
                }
            }
            catch (Exception e){
               throw new IOException2("Could not convert the Junit report.", e);
            }
        }
        return true;
    }

    private void performTransformation(Transformer toolXMLTransformer, FilePath junitOutputPath, FilePath curToolTestFile)
    throws TransformerException, InterruptedException, IOException{
        FilePath junitTargetFile = new FilePath(junitOutputPath, JUNIT_FILE_PREFIX + curToolTestFile.hashCode() + JUNIT_FILE_POSTFIX);
        toolXMLTransformer.transform(new StreamSource(new File(curToolTestFile.toURI())), new StreamResult(new File(junitTargetFile.toURI())));
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
        String[] cppunitFiles = ds.getIncludedFiles();
        return cppunitFiles;
    }


}
