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
import org.jenkinsci.plugins.xunit.service.XUnitLog;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.junit.TestResult;

import java.io.IOException;
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

        if (failedCount > 0) {
            int quarantined = getQuarantinedFailedTestsCount(log, testResultAction, workspace);
            failedCount = failedCount - quarantined;
        }

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

        if (failedCount > 0) {
            int quarantined = getQuarantinedFailedTestsCount(log, testResultAction, workspace);
            failedCount = failedCount - quarantined;
        }

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

    /**
     * recursively search the source code for all the occurrences of the quarantined-tests.json files in the jenkins workspace
     * and the parsed tests (namespace+testname) are compared with the failed ones increasing the quarantined count.
     *
     * @param log              the log so the operation leaves a trail of its action.
     * @param testResultAction the test result from the run containing the failed tests.
     * @param workspace        the jenkins workspace FilePath.
     * @return the count of quarantined failed tests*
     */
    private int getQuarantinedFailedTestsCount(XUnitLog log, TestResult testResultAction, FilePath workspace) {

        if (workspace == null)
            return 0;

        final String QUARANTINED_TEST_FILE = "quarantined-tests.json";
        log.info(String.format("Searching %s workspace `%s` for %s files.", workspace.isRemote() ? "remote" : "local", workspace, QUARANTINED_TEST_FILE));

        List<TestSetting> listOfQuaratinedTests = new ArrayList<>();

        try {
            Collection<FilePath> collection = listFilePathTree(workspace, QUARANTINED_TEST_FILE);

            boolean userHeader = false;
            for (FilePath f : collection) {
                if (!f.isDirectory() && f.getName().equals(QUARANTINED_TEST_FILE)) {
                    // invoking Jenkins FilePath ability to read files from remote/local workspaces
                    String jsonTxt = f.readToString();
                    JSONArray testArray = (JSONArray) JSONSerializer.toJSON(jsonTxt);
                    List<TestSetting> listOfTests = TestSetting.fillList(testArray);

                    for (TestSetting testSetting : listOfTests) {

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
        }

        // catch and bury exceptions while loading the quarantined-tests.json
        catch (Exception ex) {
            log.error(String.format("EXCEPTION while loading the `%s` files:%s", QUARANTINED_TEST_FILE, ex));
        }

        int quarantined = 0;
        for (CaseResult case_result : testResultAction.getFailedTests()) {
            // Java 8
            TestSetting matchingTest = listOfQuaratinedTests
                    .stream()
                    .filter((testResult) -> testResult.name.equals(case_result.getFullName()))
                    .findFirst()
                    .orElse(null);

            if (matchingTest != null) {
                log.warn(String.format("[Quarantined]: %s failed but it is quarantined.", case_result.getFullName()));
                quarantined++;
            } else {
                log.error(String.format("[Un-Quarantined]: %s failed but it is NOT quarantined.", case_result.getFullName()));
            }
        }
        return quarantined;
    }


    /**
     * recursively search the input FilePath folder (remote or local) and returns all its files
     *
     * @param dir the root dir to search
     * @return returns the collection of the FilePath's found*
     */
    public static Collection<FilePath> listFilePathTree(FilePath dir, String fileName) {

        Set<FilePath> fileTree = new HashSet<FilePath>();

        try {
            if (dir == null || dir.list(new SpecificFilesFileFilter(fileName)) == null) {
                return fileTree;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            List<FilePath> files = dir.list(new SpecificFilesFileFilter(fileName));
            if (files != null) {
                for (FilePath entry : files) {
                    if (entry != null) {
                        if (entry.isDirectory()) {
                            fileTree.addAll(listFilePathTree(entry, fileName));
                        } else {
                            fileTree.add(entry);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fileTree;
    }

    /**
     * Poco class for decoding the following JSON file format
     * [{
     * "name": "FooServiceTests.testFooFooMethod",
     * "reason": "this test fails all the time."
     * },
     * {
     * "name": "FooServiceTests.testFooIntermMethod",
     * "reason": "this test fails intermitently."
     * }
     * ]
     */
    public static class TestSetting {
        private String name;
        private String reason;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return this.reason;
        }

        public static TestSetting fill(JSONObject jo) {
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
