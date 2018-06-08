<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2011, Gregory Boissinot, Aravindan Mahendran

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xunit="http://www.xunit.org">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" cdata-section-elements="system-out system-err failure"/>
    <xsl:decimal-format decimal-separator="." grouping-separator=","/>

    <xsl:function name="xunit:junit-time" as="xs:string">
        <xsl:param name="value" as="xs:anyAtomicType?" />

        <xsl:variable name="time" as="xs:double">
            <xsl:choose>
                <xsl:when test="$value instance of xs:double">
                    <xsl:value-of select="$value" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="translate(string(xunit:if-empty($value, 0)), ',', '.')" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:value-of select="format-number($time, '0.000')" />
    </xsl:function>

    <xsl:function name="xunit:if-empty" as="xs:string">
        <xsl:param name="value" as="xs:anyAtomicType?" />
        <xsl:param name="default" as="xs:anyAtomicType" />
        <xsl:value-of select="if (string($value) != '') then string($value) else $default" />
    </xsl:function>

    <xsl:function name="xunit:is-empty" as="xs:boolean">
        <xsl:param name="value" as="xs:string?" />
        <xsl:value-of select="string($value) != ''" />
    </xsl:function>

    <xsl:function name="xunit:millis-from-time" as="xs:double">
        <xsl:param name="value" as="xs:string?" />

        <xsl:variable name="formattedTime" select="xunit:if-empty(string($value), '00:00:00')" />
        <xsl:variable name="formattedTime" select="replace(translate($formattedTime,',','.'), '^(\d:.+)', '0$1')" />
        <xsl:variable name="time" select="xs:time($formattedTime)" />
        <xsl:value-of select="hours-from-time($time)*3600 + minutes-from-time($time)*60 + seconds-from-time($time)" />
    </xsl:function>

    <xsl:key name="testCaseId" match="/ResultsSession/Exec/ExecViols/ExecViol" use="@testCaseId" />

    <xsl:template match="/">
        <xsl:variable name="testCount" select="ResultsSession/Exec/Summary/Projects/Project/@testCases" />
        <xsl:variable name="failureCount" select="ResultsSession/Exec/Summary/Projects/Project/@fail" />
        <xsl:variable name="suiteName" select="ResultsSession/Exec/Summary/Projects/Project/@name" />
        <xsl:variable name="totalTime" select="xunit:junit-time(xunit:millis-from-time(ResultsSession/Exec/@time))" />

        <xsl:choose>
            <xsl:when test="ResultsSession/Exec/TestingProcessProblems">
                <!-- No tests was run -->
                <testsuite name="{ResultsSession/TestConfig/@name}"
                            time="{$totalTime}"
                            tests="0"
                            failures="0"
                            errors="0">
                    <xsl:element name="system-err">
                        <xsl:value-of select="ResultsSession/Exec/TestingProcessProblems/CppAnalysisProblem/ErrorList/Error/@val"/>
                    </xsl:element>
                </testsuite>
            </xsl:when>
            <xsl:when test="ResultsSession/ExecutedTestsDetails">
                <testsuites name="{$suiteName}"
                            time="{$totalTime}"
                            tests="{$testCount}"
                            failures="{$failureCount}"
                            errors="0">
                    <xsl:apply-templates select="ResultsSession/ExecutedTestsDetails/Total/Project/TestSuite">
                        <xsl:with-param name="suiteName" select="$suiteName" />
                    </xsl:apply-templates>
                </testsuites>
            </xsl:when>
            <xsl:otherwise>
                <!-- CppTest 7.x no CLI options-->
                <testsuite name="{$suiteName}"
                            time="{$totalTime}"
                            tests="{$testCount}"
                            failures="{$failureCount}"
                            errors="0">
                    <xsl:call-template name="TestCase_7x">
                        <xsl:with-param name="violations" select="ResultsSession/Exec/ExecViols"/>
                    </xsl:call-template>
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="TestCase_7x">
        <xsl:param name="violations" />

        <xsl:for-each select="$violations/ExecViol[generate-id() = generate-id(key('testCaseId', @testCaseId)[1])]">
            <xsl:variable name="suiteName" select="substring-before(@testName, '::')" />
            <xsl:variable name="testName" select="substring-after(@testName, '::')" />

            <testcase classname="{$suiteName}" name="{$testName}" time="{xunit:junit-time(0)}">
                <xsl:if test="@cat != 6">
                    <xsl:apply-templates select="Thr" />
                </xsl:if>
            </testcase>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="TestSuite">
        <xsl:param name="suiteName" as="xs:string?" />

        <xsl:variable name="suiteName" select="concat($suiteName, '.', @name)" />
        <xsl:choose>
            <xsl:when test="count(TestSuite) > 0">
                <xsl:apply-templates select="TestSuite">
                    <xsl:with-param name="suiteName" select="$suiteName" />
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="suiteTime">
                    <xsl:for-each select=".//Test/@time">
                        <xsl:value-of select="xunit:millis-from-time(.)" />
                    </xsl:for-each>
                </xsl:variable>

                <testsuite name="{$suiteName}"
                           time="{xunit:junit-time($suiteTime)}"
                           tests="{@pass + @fail}"
                           failures="{@fail}"
                           errors="0">
                    <xsl:apply-templates select="Test">
                        <xsl:with-param name="fullSuiteName" select="$suiteName" />
                        <xsl:with-param name="suiteName" select="@name" />
                    </xsl:apply-templates>
                </testsuite>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="Test">
        <xsl:param name="suiteName" as="xs:string?" />
        <xsl:param name="fullSuiteName" as="xs:string?" />

        <xsl:variable name="testName" select="if (starts-with(@name, $suiteName)) then substring(@name, string-length($suiteName) + 3) else @name" />
        <xsl:variable name="testId" select="@id" />
        <testcase name="{$testName}"
                  classname="{xunit:if-empty($fullSuiteName, $suiteName)}"
                  time="{xunit:junit-time(xunit:millis-from-time(@time))}">
            <xsl:if test="@pass != 1">
                <xsl:apply-templates select="//Exec/ExecViols/ExecViol[@testId = $testId]" />
            </xsl:if>
        </testcase>
    </xsl:template>

    <xsl:template match="ExecViol">
        <xsl:apply-templates select="Thr"/>
    </xsl:template>

    <xsl:template match="Thr">
        <xsl:apply-templates select="ThrPart"/>
    </xsl:template>

    <xsl:template match="ThrPart">
        <failure type="{@clName}" message="{@detMsg}">
            <xsl:apply-templates select="Trace"/>
        </failure>
    </xsl:template>

    <xsl:template match="Trace">
	at <xsl:value-of select="@fileName"/>:<xsl:value-of select="@ln"/>
    </xsl:template>

</xsl:stylesheet>