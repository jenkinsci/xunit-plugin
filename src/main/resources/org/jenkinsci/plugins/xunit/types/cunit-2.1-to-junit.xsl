<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014 Shawn Liang
Last modification: 02/18/2016 by Schneider Electric

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

https://github.com/shawnliang/cunit-to-junit
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <testsuites>
            <xsl:for-each select="//CUNIT_RUN_SUITE_SUCCESS">
                <xsl:variable name="suiteName" select="normalize-space(SUITE_NAME/text())"/>
                <xsl:variable name="numberOfTests" select="count(CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_SUCCESS)"/>
                <xsl:variable name="numberOfFailures" select="count(CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_FAILURE)"/>
                <testsuite name="{$suiteName}" tests="{$numberOfTests}" time="0" failures="{$numberOfFailures}" errors="0" skipped="0">
                    <xsl:for-each select="CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_SUCCESS">
                        <xsl:variable name="testname" select="normalize-space(TEST_NAME/text())"/>
                        <testcase classname="{$suiteName}" name="{$testname}" time="0.0"/>
                    </xsl:for-each>
                    <xsl:for-each select="CUNIT_RUN_TEST_RECORD/CUNIT_RUN_TEST_FAILURE">
                        <xsl:variable name="testname" select="normalize-space(TEST_NAME/text())"/>
                        <testcase classname="{$suiteName}" name="{$testname}" time="0.0">
                            <failure>
Test failed at line <xsl:value-of select="LINE_NUMBER"/> in file <xsl:value-of select="FILE_NAME"/>:
<xsl:value-of select="CONDITION"/>
                            </failure>
                        </testcase>
                    </xsl:for-each>
                </testsuite>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>
