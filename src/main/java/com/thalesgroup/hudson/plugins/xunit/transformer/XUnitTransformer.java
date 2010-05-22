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

import com.thalesgroup.hudson.library.tusarconversion.ConversionUtil;
import com.thalesgroup.hudson.library.tusarconversion.exception.ConversionException;
import com.thalesgroup.hudson.plugins.xunit.types.PHPUnitType;
import com.thalesgroup.hudson.plugins.xunit.types.XUnitType;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private static final String JUNIT_FILE_POSTFIX = ".xml";
    private static final String JUNIT_FILE_PREFIX = "TEST-";


    private BuildListener listener;
    private long buildTime;
    private EnvVars env;
    private XUnitType[] types;
    private FilePath junitOutputPath = null;

    public XUnitTransformer(BuildListener listener, long buildTime, EnvVars env, XUnitType[] types, FilePath junitOutputPath) {
        this.listener = listener;
        this.buildTime = buildTime;
        this.env = env;
        this.types = types;
        this.junitOutputPath = junitOutputPath;
    }


    /**
     * Test if the field is empty
     *
     * @param field
     * @return
     */
    private boolean isEmpty(String field) {
        if (field == null) {
            return true;
        }

        if (field.trim().length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Invocation
     *
     * @param ws
     * @param channel
     * @return the Result
     * @throws IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {

        try {

            boolean isInvoked = false;
            for (XUnitType tool : types) {
                if (!isEmpty(tool.getPattern())) {
                    isInvoked = true;

                    InputStream is = null;
                    File f = new File(ws, tool.getXsl());
                    if (!f.exists()) {
                        XUnitLog.log(listener, "[" + tool.getDescriptor().getDisplayName() + "] - Use the embedded style sheet.");
                        is = ConversionUtil.class.getResourceAsStream(tool.getXsl());
                    } else {
                        XUnitLog.log(listener, "[" + tool.getDescriptor().getDisplayName() + "] - Use the style sheet found into the workspace.");
                        is = new FileInputStream(f);
                    }

                    if (is == null) {
                        XUnitLog.log(listener, "The style sheet '" + tool.getXsl() + "' is not found for the xUnit tool '" + tool.getDescriptor().getDisplayName() + "'");
                        return false;
                    }

                    boolean result = processTool(ws, tool, new StreamSource(is));
                    is.close();

                    if (!result) {
                        return result;
                    }
                }
            }

            if (!isInvoked) {
                String msg = "[ERROR] - No test report files were found. Configuration error?";
                XUnitLog.log(listener, msg);
                return false;
            }


        }
        catch (Exception e) {
            throw new IOException2("Problem on converting into JUnit reports.", e);
        }

        return true;

    }


    private boolean validateXUnitResultFile(File fileXUnitReportFile)
            throws FactoryConfigurationError {
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            parser.parse(fileXUnitReportFile);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    /**
     * /**
     * Collect reports from the given parentpath and the pattern, while
     * filtering out all files that were created before the given time.
     *
     * @param buildTime       the build time
     * @param parentPath      parent
     * @param pattern         pattern to seach files
     * @param faildedIfNotNew indicated if the tests time need to be checked
     * @return an array of strings
     */
    private List<String> findtReports(XUnitType testTool, long buildTime, File parentPath, String pattern, boolean faildedIfNotNew) throws AbortException {

        FileSet fs = Util.createFileSet(parentPath, pattern);
        DirectoryScanner ds = fs.getDirectoryScanner();
        File baseDir = ds.getBasedir();
        String[] xunitFiles = ds.getIncludedFiles();

        if (xunitFiles.length == 0) {
            String msg = "[ERROR] - No test report file(s) were found with the pattern '"
                    + pattern + "' relative to '" + parentPath + "' for the testing framework '" + testTool.getDescriptor().getDisplayName() + "'."
                    + "  Did you enter a pattern relative to the correct directory?"
                    + "  Did you generate the result report(s) for '" + testTool.getDescriptor().getDisplayName() + "'?";
            XUnitLog.log(listener, msg);
            return null;
        }


        //Check the timestamp for each test file if the UI option is checked (true by default)
        if (faildedIfNotNew) {
            ArrayList<File> oldResults = new ArrayList<File>();
            for (String value : xunitFiles) {
                File reportFile = new File(baseDir, value);
                // if the file was not updated this build, that is a problem
                if (buildTime - 3000 > reportFile.lastModified()) {
                    oldResults.add(reportFile);
                }
            }

            if (!oldResults.isEmpty()) {
                long localTime = System.currentTimeMillis();
                if (localTime < buildTime - 1000) {
                    // build time is in the the future. clock on this slave must be running behind
                    String msg = "[ERROR] - Clock on this slave is out of sync with the master, and therefore \n" +
                            "I can't figure out what test results are new and what are old.\n" +
                            "Please keep the slave clock in sync with the master.";
                    XUnitLog.log(listener, msg);
                    return null;
                }

                String msg = "[ERROR] Test reports were found but not all of them are new. Did all the tests run?\n";
                for (File f : oldResults) {
                    msg += String.format("  * %s is %s old\n", f, Util.getTimeSpanString(buildTime - f.lastModified()));
                }
                XUnitLog.log(listener, msg);
                return null;
            }
        }

        return Arrays.asList(xunitFiles);
    }

    /**
     * Processing the current test tool
     *
     * @param moduleRoot
     * @param testTool
     * @param stylesheet
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean processTool(File moduleRoot, XUnitType testTool, StreamSource stylesheet)
            throws IOException, InterruptedException {

        String curPattern = testTool.getPattern();
        curPattern = curPattern.replaceAll("[\t\r\n]+", " ");
        curPattern = Util.replaceMacro(curPattern, this.env);

        List<String> resultFiles = findtReports(testTool, this.buildTime, moduleRoot, curPattern, testTool.isFaildedIfNotNew());


        if (resultFiles == null) {
            return false;
        }

        XUnitLog.log(listener, "[" + testTool.getDescriptor().getDisplayName() + "] - Processing " + resultFiles.size() + " files with the pattern '" + testTool.getPattern() + "' relative to '" + moduleRoot + "'.");

        boolean hasInvalidateFiles = false;
        for (String resultFile : resultFiles) {

            File resultFilePathFile = new File(moduleRoot, resultFile);

            if (resultFilePathFile.length() == 0) {
                //Ignore the empty result file (some reason)
                String msg = "[WARNING] - The file '" + resultFilePathFile.getPath() + "' is empty. This file has been ignored.";
                XUnitLog.log(listener, msg);
                continue;
            }


            if (!validateXUnitResultFile(resultFilePathFile)) {

                //register there are unvalid files
                hasInvalidateFiles = true;

                //Ignore unvalid files
                XUnitLog.log(listener, "[WARNING] - The file '" + resultFilePathFile + "' is an invalid file. It has been ignored.");
                continue;
            }


            FilePath currentOutputDir = new FilePath(junitOutputPath, testTool.getDescriptor().getShortName());
            FilePath junitTargetFile = new FilePath(currentOutputDir, JUNIT_FILE_PREFIX + resultFilePathFile.hashCode() + JUNIT_FILE_POSTFIX);
            try {
                processJUnitFile(testTool, resultFilePathFile, junitTargetFile, currentOutputDir);

            }
            catch (Exception se) {
                String msg = "[ERROR] - Couldn't convert the file '" + resultFilePathFile.getPath() + "' into a JUnit file.";
                XUnitLog.log(listener, msg + se.toString());
                return false;
            }
        }
        return true;
    }


    /**
     * Processing the current junit file
     *
     * @param testTool
     * @param inputFile
     * @param junitOutputPath
     * @throws SAXException
     * @throws IOException
     * @throws InterruptedException
     * @throws ParserConfigurationException
     */
    private void processJUnitFile(XUnitType testTool, File inputFile, FilePath junitTargetFilePath, FilePath junitOutputPath)
            throws IOException, ConversionException, ParserConfigurationException, SAXException, InterruptedException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlDocumentBuilder = factory.newDocumentBuilder();
        Document document = xmlDocumentBuilder.parse(inputFile);
        NodeList testsuitesNodeList = document.getElementsByTagName("testsuites");

        if ((testTool.getClass() == PHPUnitType.class || testsuitesNodeList == null) || testsuitesNodeList.getLength() == 0) {
            File fTargetFile = new File(junitTargetFilePath.toURI());
            fTargetFile.createNewFile();
            ConversionUtil.convert(testTool.getXsl(), new FileInputStream(inputFile), new FileOutputStream(fTargetFile));
        } else {
            splitJunitFile(testTool, testsuitesNodeList, junitOutputPath);
        }

    }

    /**
     * Segragate the current junit file
     *
     * @param testTool
     * @param testsuitesNodeList
     * @param junitOutputPath
     * @throws IOException
     * @throws InterruptedException
     * @throws SaxonApiException
     */
    private void splitJunitFile(XUnitType testTool, NodeList testsuitesNodeList, FilePath junitOutputPath)
            throws IOException, InterruptedException, ConversionException {
        NodeList elementsByTagName = ((Element) testsuitesNodeList.item(0)).getElementsByTagName("testsuite");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);

            DOMSource source = new DOMSource(element);

            String suiteName = element.getAttribute("name");
            FilePath junitOutputFile = new FilePath(junitOutputPath, JUNIT_FILE_PREFIX + suiteName.hashCode() + JUNIT_FILE_POSTFIX);
            FileOutputStream fileOutputStream = new FileOutputStream(new File(junitOutputFile.toURI()));
            try {
                ConversionUtil.convert(testTool.getXsl(), source, fileOutputStream);

            } finally {
                fileOutputStream.close();
            }
        }
    }

}
