<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2017, Alex Schwantes

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:template match="/test-run">
        <!--<xsl:variable name="hostname" select="./environment/@machine-name"/>-->
        <testsuites>
            <xsl:for-each select="//test-suite[@type='TestSuite']">
                <xsl:for-each select="test-suite[@type='TestFixture']">
                    <xsl:variable name="fixture" select="./@name" />
                    <xsl:variable name="classname" select="./@classname" />
                    <testsuite name="{$classname}"
                               tests="{@total}"
                               time="{@duration}"
                               failures="{@failed}"
                               errors="0"
                               skipped="{@skipped}">
                        <!-- skipped="{count(*/test-case[@executed='False'])}"> -->
                        <xsl:for-each select=".//test-case">
                            <xsl:variable name="testcaseName" select="./@name" />
                            <testcase classname="{$classname}" name="{$testcaseName}">
                                <xsl:if test="@duration!=''">
                                    <xsl:attribute name="time"><xsl:value-of select="@duration" /></xsl:attribute>
                                </xsl:if>

                                <xsl:variable name="generalfailure" select="./failure" />

                                <xsl:if test="./failure">
                                    <xsl:variable name="failstack"
                                                  select="count(./failure/stack-trace/*) + count(./failure/stack-trace/text())" />
                                    <failure>
                                        <xsl:choose>
                                            <xsl:when test="$failstack &gt; 0 or not($generalfailure)">
MESSAGE:
<xsl:value-of select="./failure/message" />
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="./failure/stack-trace" />
+++++++++++++++++++
<xsl:value-of select="./output" />
                                            </xsl:when>
                                            <xsl:otherwise>
MESSAGE:
<xsl:value-of select="$generalfailure/message" />
+++++++++++++++++++
STACK TRACE:
<xsl:value-of select="$generalfailure/stack-trace" />
+++++++++++++++++++
OUTPUT:
<xsl:value-of select="./output" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </failure>
                                </xsl:if>
                                <xsl:if test="@result='Skipped' or @result='Inconclusive'" >
                                    <skipped/>
                                    <system-out>
                                        <xsl:value-of select="./reason/message"/>
                                        <xsl:value-of select="./output" />
                                    </system-out>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                </xsl:for-each>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>
