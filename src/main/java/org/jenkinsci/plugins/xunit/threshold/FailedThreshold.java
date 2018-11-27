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

package org.jenkinsci.plugins.xunit.threshold;

import hudson.FilePath;
import hudson.tasks.junit.CaseResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author Gregory Boissinot
 */
public class FailedThreshold extends XUnitThreshold {

    @DataBoundConstructor
    public FailedThreshold() {
    }

    @Override
    public Result getResultThresholdNumber(XUnitLog log, Run<?, ?> build, TestResult testResultAction, TestResult previousTestResultAction, FilePath workspace) {

        int failedCount = testResultAction.getFailCount();

        int quarantined = getQuarantined(log, testResultAction, workspace);
        failedCount = failedCount - quarantined;

        int previousFailedCount = 0;
        if (previousTestResultAction != null) {
            previousFailedCount = previousTestResultAction.getFailCount();
        }
        int newFailedCount = failedCount - previousFailedCount;

        return getResultThresholdNumber(log, failedCount, newFailedCount);
    }

    @Override
    public Result getResultThresholdPercent(XUnitLog log, Run<?, ?> build, TestResult testResultAction, TestResult previousTestResultAction, FilePath workspace) {

        double count = testResultAction.getTotalCount();

        double failedCount = testResultAction.getFailCount();

        int quarantined = getQuarantined(log, testResultAction, workspace);
        failedCount = failedCount - quarantined;

        double percentFailed = (failedCount / count) * 100;

        double previousFailedCount = 0;
        if (previousTestResultAction != null) {
            previousFailedCount = previousTestResultAction.getFailCount();
        }
        double newFailedCount = failedCount - previousFailedCount;
        double percentNewFailed = (newFailedCount / count) * 100;

        return getResultThresholdPercent(log, percentFailed, percentNewFailed);
    }

    @Override
    public boolean isValidThreshold(double threshold, double value) {
        return value <= threshold;
    }

    private int getQuarantined(XUnitLog log, TestResult testResultAction, FilePath workspace) {

        if (workspace == null)
            return 0;

        String quarantinedTestsFileName = "quarantined-tests.json";
        log.info(String.format("Searching workspace `%s` for %s files." , workspace, quarantinedTestsFileName));

        List<TestSetting> listOfQuaratinedTests = new ArrayList<>();

        try
        {
            if(!workspace.isRemote())
            {
                Collection<File> files = listFileTree(new File(workspace.getRemote()));
                boolean userHeader = false;
                for ( File f: files) {
                    if (f.getName().equals(quarantinedTestsFileName))
                    {

                        InputStream is = new FileInputStream(f.getAbsoluteFile());
                        String jsonTxt = IOUtils.toString(is, "UTF-8");
                        JSONArray testArray = (JSONArray) JSONSerializer.toJSON(jsonTxt);
                        List<TestSetting> listOfTests = TestSetting.fillList(testArray);

                        for (TestSetting testSetting: listOfTests) {

                            if (!userHeader) {
                                log.info("------------------------------------------------------------------------");
                                log.info("QUARANTINED TESTS");
                                log.info("------------------------------------------------------------------------");
                                userHeader = true;
                            }

                            log.info(String.format("[%s] Reason: %s", testSetting.name, testSetting.reason));
                            listOfQuaratinedTests.add(testSetting);
                        }
                    }
                }
                if (userHeader) {
                    log.info("------------------------------------------------------------------------");
                }
            }}
        // catch and bury exceptions while loading the quarantined-tests.json
        catch (Exception ex)
        {
            log.error(String.format("EXCEPTION while loading the `quarantined-tests.json` files:%s", ex));
        }

        int quarantined = 0;
        for (CaseResult case_result: testResultAction.getFailedTests()) {
            // Java 8
            TestSetting matchingTest = listOfQuaratinedTests
                    .stream()
                    .filter((testResult) -> testResult.name.equals(case_result.getFullName()))
                    .findFirst()
                    .orElse(null);

            if (matchingTest != null)
            {
                log.warn(String.format("[Quarantine]: %s failed but it is quarantined.", case_result.getFullName()));
                quarantined++;
            }
        }
        return quarantined;
    }

    public static Collection<File> listFileTree(File dir) {
        Set<File> fileTree = new HashSet<File>();
        if(dir==null||dir.listFiles()==null){
            return fileTree;
        }
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File entry : files) {
                if (entry != null) {
                    if (entry.isFile()) fileTree.add(entry);
                    else fileTree.addAll(listFileTree(entry));
                }
            }
        }
        return fileTree;
    }


    // JSON file format
    //    [{
    //            "name": "FooServiceTests.testFooFooMethod",
    //                    "reason": "this test fails all the time."
    //        },
    //        {
    //            "name": "FooServiceTests.testFooIntermMethod",
    //                "reason": "this test fails intermitently."
    //        }
    //    ]
    public static class TestSetting {
        private String name;
        private String reason;

        public void setName(String name){
            this.name = name;
        }
        public String getName(){
            return this.name;
        }
        public void setReason(String reason){
            this.reason = reason;
        }
        public String getReason(){
            return this.reason;
        }

        public static TestSetting fill(JSONObject jo){
            TestSetting o = new TestSetting();
            if (jo.containsKey("name")) {
                o.setName(jo.getString("name"));
            }
            if (jo.containsKey("reason")) {
                o.setReason(jo.getString("reason"));
            }
            return o;
        }

        public static List<TestSetting> fillList(JSONArray ja) {
            if (ja == null || ja.size() == 0)
                return null;
            List<TestSetting> sqs = new ArrayList<TestSetting>();
            for (int i = 0; i < ja.size(); i++) {
                sqs.add(fill(ja.getJSONObject(i)));
            }
            return sqs;
        }
    }

}
