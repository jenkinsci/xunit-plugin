<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2009, Gregory Boissinot

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

    <xsl:template match="/">
        <testsuite>
            <xsl:attribute name="errors">
                <xsl:value-of select="unittest-results/@failedtests"/>
            </xsl:attribute>

            <xsl:attribute name="failures">
                <xsl:value-of select="unittest-results/@failures"/>
            </xsl:attribute>

            <xsl:attribute name="tests">
                <xsl:value-of select="unittest-results/@tests"/>
            </xsl:attribute>

            <xsl:attribute name="name">unittest</xsl:attribute>

            <xsl:variable name="testSuiteTime">
                <xsl:value-of select="xunit:junit-time(xunit:if-empty(unittest-results/@time, 0))"/>
            </xsl:variable>

            <xsl:attribute name="timestamp">
                <xsl:value-of select="$testSuiteTime"/>
            </xsl:attribute>
            
            <xsl:apply-templates select="/unittest-results/test">
                <xsl:with-param name="testSuiteTime" select="$testSuiteTime" />
            </xsl:apply-templates>
        </testsuite>
    </xsl:template>

    <xsl:template match="test">
        <xsl:param name="testSuiteTime"/>

        <testcase>
            <xsl:attribute name="classname">
                <xsl:value-of select="@suite"/>
            </xsl:attribute>

            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>

            <xsl:attribute name="time">
                <xsl:value-of select="xunit:junit-time(xunit:if-empty(@time, $testSuiteTime))"/>
            </xsl:attribute>

            <xsl:if test="failure">
                <xsl:variable name="failureMessage">
                    <xsl:value-of select="string-join(failure/@message, '&#10;')" />
                </xsl:variable>
                <failure message="{$failureMessage}" />
            </xsl:if>
        </testcase>
    </xsl:template>

</xsl:stylesheet>