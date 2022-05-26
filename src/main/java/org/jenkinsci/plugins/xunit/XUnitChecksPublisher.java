/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022, JoC0de
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
package org.jenkinsci.plugins.xunit;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultSummary;
import io.jenkins.plugins.checks.api.ChecksConclusion;
import io.jenkins.plugins.checks.api.ChecksDetails;
import io.jenkins.plugins.checks.api.ChecksOutput;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.checks.api.ChecksStatus;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import java.util.List;

/**
 * Publishes test status to Checks API
 * Implementation is based on:
 * https://github.com/jenkinsci/junit-plugin/blob/dd65de93befc357d7160251536c903914dd257c6/src/main/java/io/jenkins/plugins/junit/checks/JUnitChecksPublisher.java
 */
class XUnitChecksPublisher {
    public static final String SEPARATOR = ", ";

    // cap to avoid hitting check API message limit
    private static final int MAX_MSG_SIZE_TO_CHECKS_API = 65535;

    private final Run<?, ?> run;

    @NonNull
    private final String checksName;

    @NonNull
    private final TestResult result;

    @NonNull
    private final TestResultSummary summary;

    @NonNull
    private final Result buildResult;

    public XUnitChecksPublisher(final Run<?, ?> run, @NonNull final String checksName, @NonNull final XUnitProcessorResult result, @NonNull final Result buildResult) {
        this.run = run;
        this.checksName = checksName;
        this.result = result.getTestResult();
        this.summary = result.getTestResultSummary();
        this.buildResult = buildResult;
    }

    public void publishChecks(TaskListener listener) {
        ChecksPublisher publisher = ChecksPublisherFactory.fromRun(run, listener);
        publisher.publish(extractChecksDetails());
    }

    @VisibleForTesting
    ChecksDetails extractChecksDetails() {
        String testsURL = DisplayURLProvider.get().getTestsURL(run);
        ChecksOutput output = new ChecksOutput.ChecksOutputBuilder()
                .withTitle(extractChecksTitle())
                .withSummary("<sub>Send us [feedback](https://github.com/jenkinsci/xunit-plugin/issues)")
                .withText(extractChecksText(testsURL))
                .build();

        return new ChecksDetails.ChecksDetailsBuilder()
                .withName(checksName)
                .withStatus(ChecksStatus.COMPLETED)
                .withConclusion(mapBuildResultToConclusion())
                .withDetailsURL(testsURL)
                .withOutput(output)
                .build();
    }

    private String extractChecksText(String testsURL) {
        StringBuilder builder = new StringBuilder();
        if (summary.getFailCount() > 0) {
            List<CaseResult> failedTests = result.getFailedTests();

            for (CaseResult failedTest: failedTests) {
                String testReport = mapFailedTestToTestReport(failedTest);
                int messageSize = testReport.length() + builder.toString().length();
                // to ensure text size is withing check API message limit
                if (messageSize > (MAX_MSG_SIZE_TO_CHECKS_API - 1024)){
                    builder.append("\n")
                            .append("more test results are not shown here, view them on [Jenkins](")
                            .append(testsURL).append(")");
                    break;
                }
                builder.append(testReport);
            }
        }

        return builder.toString();
    }

    private String mapFailedTestToTestReport(CaseResult failedTest) {
        StringBuilder builder = new StringBuilder();
        builder.append("## `").append(failedTest.getTransformedFullDisplayName().trim()).append("`")
                .append("\n");

        if (StringUtils.isNotBlank(failedTest.getErrorDetails())) {
            builder.append(codeTextFencedBlock(failedTest.getErrorDetails()))
                    .append("\n");
        }
        if (StringUtils.isNotBlank(failedTest.getErrorStackTrace())) {
            builder.append("<details><summary>Stack trace</summary>\n")
                    .append(codeTextFencedBlock(failedTest.getErrorStackTrace()))
                    .append("</details>\n");
        }

        if (StringUtils.isNotBlank(failedTest.getStderr())) {
            builder.append("<details><summary>Standard error</summary>\n")
                    .append(codeTextFencedBlock(failedTest.getStderr()))
                    .append("</details>\n");
        }

        if (StringUtils.isNotBlank(failedTest.getStdout())) {
            builder.append("<details><summary>Standard out</summary>\n")
                    .append(codeTextFencedBlock(failedTest.getStdout()))
                    .append("</details>\n");
        }
        builder.append("\n");
        return builder.toString();
    }

    private String codeTextFencedBlock(String body) {
        return "\n```text\n" + body.trim() + "\n```\n";
    }

    private String extractChecksTitle() {

        if (summary.getTotalCount() == 0) {
            return "No test results found";
        }

        StringBuilder builder = new StringBuilder();

        if (summary.getFailCount() == 1) {
            CaseResult failedTest = result.getFailedTests().get(0);
            builder.append(failedTest.getTransformedFullDisplayName()).append(" failed");
            return builder.toString();
        }

        if (summary.getFailCount() > 0) {
            builder.append("failed: ").append(summary.getFailCount());
            if (summary.getSkipCount() > 0 || summary.getPassCount() > 0) {
                builder.append(SEPARATOR);
            }
        }

        if (summary.getSkipCount() > 0) {
            builder.append("skipped: ").append(summary.getSkipCount());

            if (summary.getPassCount() > 0) {
                builder.append(SEPARATOR);
            }
        }

        if (summary.getPassCount() > 0) {
            builder.append("passed: ").append(summary.getPassCount());
        }


        return builder.toString();
    }

    private ChecksConclusion mapBuildResultToConclusion() {
        return buildResult == Result.SUCCESS ? ChecksConclusion.SUCCESS : ChecksConclusion.FAILURE;
    }
}