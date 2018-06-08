<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2017, Alex Schwantes, Nikolas Falco

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


    <xsl:template match="/test-run">
        <!--<xsl:variable name="hostname" select="./environment/@machine-name"/>-->
        <testsuites>
            <xsl:for-each select="//test-suite[@type='TestSuite']">
                <xsl:for-each select="test-suite[@type='TestFixture']">
                    <xsl:variable name="fixture" select="./@name" />
                    <xsl:variable name="classname" select="./@classname" />
                    <testsuite name="{$classname}"
                               tests="{@total}"
                               time="{xunit:junit-time(@duration)}"
                               failures="{@failed}"
                               errors="0"
                               skipped="{@skipped}">
                        <xsl:for-each select=".//test-case">
                            <xsl:variable name="testcaseName" select="./@name" />
                            <testcase classname="{$classname}" name="{$testcaseName}">
                                <xsl:if test="@duration!=''">
                                    <xsl:attribute name="time">
                                        <xsl:value-of select="xunit:junit-time(@duration)" />
                                    </xsl:attribute>
                                </xsl:if>

                                <xsl:if test="@result='Skipped' or @result='Inconclusive'" >
                                    <skipped>
                                        <xsl:if test="xunit:is-empty(./reason/message)">
                                            <xsl:attribute name="message">
                                                <xsl:value-of select="./reason/message"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                    </skipped>
                                    <system-out>
                                        <xsl:value-of select="./output" />
                                    </system-out>
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
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                </xsl:for-each>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>
