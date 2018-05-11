<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2012, Gregory Boissinot, Falco Nikolas

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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xunit="http://www.xunit.org"
    xmlns:a="http://microsoft.com/schemas/VisualStudio/TeamTest/2006" xmlns:b="http://microsoft.com/schemas/VisualStudio/TeamTest/2010">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:decimal-format decimal-separator="." grouping-separator=","/>

    <xsl:function name="xunit:junit-time" as="xs:string">
        <xsl:param name="value" as="xs:double?" />

        <xsl:variable name="time" as="xs:double">
            <xsl:value-of select="translate(string($value),',','.')" />
        </xsl:variable>
        <xsl:value-of select="format-number($time, '0.000')" />
    </xsl:function>

    <xsl:function name="xunit:is-empty" as="xs:boolean">
        <xsl:param name="value" as="xs:string?" />
        <xsl:value-of select="string($value) != ''" />
    </xsl:function>

    <xsl:template match="/">
        <testsuites>
            <xsl:variable name="buildName" select="//a:TestRun/@name"/>
            <xsl:variable name="numberOfTests"
                          select="count(//a:UnitTestResult/@outcome) + count(//b:UnitTestResult/@outcome)"/>
            <xsl:variable name="numberOfFailures"
                          select="count(//a:UnitTestResult/@outcome[.='Failed']) + count(//b:UnitTestResult/@outcome[.='Failed'])"/>
            <xsl:variable name="numberSkipped"
                          select="count(//a:UnitTestResult/@outcome[.='Inconclusive']) + count(//b:UnitTestResult/@outcome[.='Inconclusive'])"/>
            <testsuite name="MSTestSuite"
                       tests="{$numberOfTests}" time="0"
                       failures="{$numberOfFailures}" errors="0"
                       skipped="{$numberSkipped}">

                <xsl:for-each select="//a:UnitTestResult">
                    <xsl:variable name="testName" select="@testName"/>
                    <xsl:variable name="executionId" select="@executionId"/>
                    <xsl:variable name="duration_seconds" select="number(substring(@duration, 7))"/>
                    <xsl:variable name="duration_minutes" select="number(substring(@duration, 4,2 ))"/>
                    <xsl:variable name="duration_hours" select="number(substring(@duration, 1, 2))"/>
                    <xsl:variable name="outcome" select="@outcome"/>
                    <xsl:variable name="message" select="a:Output/a:ErrorInfo/a:Message"/>
                    <xsl:variable name="stacktrace" select="a:Output/a:ErrorInfo/a:StackTrace"/>
                    <xsl:for-each select="//a:UnitTest">
                        <xsl:variable name="currentExecutionId" select="a:Execution/@id"/>
                        <xsl:if test="$currentExecutionId = $executionId">
                            <xsl:variable name="className" select="substring-before(a:TestMethod/@className, ',')"/>
                            <xsl:variable name="time" select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
                            <testcase classname="{$className}"
                                      name="{$testName}"
                                      time="{xunit:junit-time($time)}">

                                <xsl:if test="contains($outcome, 'Failed')">
                                    <failure>
                                        MESSAGE:
                                        <xsl:value-of select="$message"/>
                                        +++++++++++++++++++
                                        STACK TRACE:
                                        <xsl:value-of select="$stacktrace"/>
                                    </failure>
                                </xsl:if>
                            </testcase>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="//b:UnitTestResult">
                    <xsl:variable name="testName" select="@testName"/>
                    <xsl:variable name="executionId" select="@executionId"/>
                    <xsl:variable name="duration_seconds" select="number(substring(@duration, 7))"/>
                    <xsl:variable name="duration_minutes" select="number(substring(@duration, 4,2 ))"/>
                    <xsl:variable name="duration_hours" select="number(substring(@duration, 1, 2))"/>
                    <xsl:variable name="outcome" select="@outcome"/>
                    <xsl:variable name="message" select="b:Output/b:ErrorInfo/b:Message"/>
                    <xsl:variable name="stacktrace" select="b:Output/b:ErrorInfo/b:StackTrace"/>
                    <xsl:for-each select="//b:UnitTest">
                        <xsl:variable name="currentExecutionId" select="b:Execution/@id"/>
                        <xsl:if test="$currentExecutionId = $executionId">
                            <xsl:variable name="className" select="substring-before(b:TestMethod/@className, ',')"/>
                            <xsl:variable name="time" select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
                            <testcase classname="{$className}"
                                      name="{$testName}"
                                      time="{xunit:junit-time($time)}">

                                <xsl:if test="$outcome = 'Inconclusive'">
                                    <skipped />
                                    <system-out>
MESSAGE:
<xsl:value-of select="$message"/>
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="$stacktrace"/>
                                    </system-out>
                                </xsl:if>
                                <xsl:if test="contains($outcome, 'Failed')">
                                    <failure>
MESSAGE:
<xsl:value-of select="$message"/>
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="$stacktrace"/>
                                    </failure>
                                </xsl:if>
                            </testcase>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:for-each>

            </testsuite>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>
