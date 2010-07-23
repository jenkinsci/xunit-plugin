/*******************************************************************************
 * Copyright (c) 2010 Thales Corporate Services SAS                             *
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

import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.hudson.api.type.TestType;
import com.thalesgroup.hudson.plugins.xunit.exception.XUnitException;
import com.thalesgroup.hudson.plugins.xunit.service.XUnitService;
import com.thalesgroup.hudson.plugins.xunit.util.XUnitLog;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class XUnitTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private transient XUnitService xUnitService;

    private final BuildListener listener;

    private final XUnitToolInfo xUnitToolInfo;

    public XUnitTransformer(XUnitService xUnitService, BuildListener listener, XUnitToolInfo xUnitToolInfo) {
        this.xUnitService = xUnitService;
        this.listener = listener;
        this.xUnitToolInfo= xUnitToolInfo;
    }


    /**
     * Invocation
     *
     * @param ws      the Hudson workspace
     * @param channel the Hudson chanel
     * @return true or false if the convertion fails
     * @throws IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        try {

            //Gets all input files matching the user pattern
            List<String> resultFiles = xUnitService.findReports(xUnitToolInfo, ws, xUnitToolInfo.getExpandedPattern());
            if (resultFiles.size() == 0) {
                return false;
            }

            //Checks the timestamp for each test file if the UI option is checked (true by default)
            if (!xUnitService.checkIfFindsFilesNewFiles(xUnitToolInfo, resultFiles, ws)) {
                return false;
            }

            for (String curFileName : resultFiles) {

                File curFile = xUnitService.getCurrentFile(ws, curFileName);

                if (curFile.length() == 0) {
                    //Ignore the empty result file (some reason)
                    String msg = "[WARNING] - The file '" + curFile.getPath() + "' is empty. This file has been ignored.";
                    XUnitLog.log(listener, msg);
                    continue;
                }

                //Validates Input file
                if (!xUnitService.validateInputFile(xUnitToolInfo , curFile)) {
                    XUnitLog.log(listener, "[WARNING] - The file '" + curFile + "' has been ignored.");
                    continue;
                }

                //Convert the input file
                File junitTargetFile = xUnitService.convert(xUnitToolInfo, curFile, xUnitToolInfo.getJunitOutputDir());


                //Validates converted file
                boolean result = xUnitService.validateOutputFile(xUnitToolInfo, curFile, junitTargetFile);
                if (!result) {
                    return false;
                }
            }

        }
        catch (XUnitException xe) {
            throw new IOException2("Problem on converting into JUnit reports.", xe);
        }


        return true;
    }

}
