<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014, Gregory Boissinot, Falco Nikolas

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
        <testsuites>
            <xsl:variable name="buildName" select="//Site/@BuildName"/>
            <xsl:variable name="buildTime" select="(//Site/Testing/EndTestTime - //Site/Testing/StartTestTime)"/>
            <xsl:variable name="numberOfTests" select="count(//Site/Testing/Test)"/>
            <xsl:variable name="numberOfFailures" select="count(//Site/Testing/Test[@Status='failed'])"/>
            <xsl:variable name="numberOfSkipped" select="count(//Site/Testing/Test[@Status='notrun'])"/>
            <testsuite name="CTest"
                       tests="{$numberOfTests}"
                       time="{xunit:junit-time($buildTime)}"
                       failures="{$numberOfFailures}"
                       errors="0"
                       skipped="{$numberOfSkipped}">
                <xsl:for-each select="//Site/Testing/Test">
                    <xsl:variable name="testName" select="translate(Name, '-', '_')"/>
                    <xsl:variable name="duration" select="number(xunit:if-empty(Results/NamedMeasurement[@name='Execution Time']/Value, 0))"/>
                    <xsl:variable name="status" select="@Status"/>
                    <xsl:variable name="output" select="Results/Measurement/Value"/>
                    <xsl:variable name="className" select="translate(Path, '/.', '.')"/>
                    <testcase classname="projectroot{$className}"
                              name="{$testName}"
                              time="{xunit:junit-time($duration)}">
                        <xsl:choose>
                            <xsl:when test="@Status='passed'"/>
                            <xsl:when test="@Status='notrun'">
                                <skipped/>
                            </xsl:when>
                            <xsl:otherwise>
                                <failure>
                                    <xsl:value-of select="$output"/>
                                </failure>
                            </xsl:otherwise>
                        </xsl:choose>
                        <system-out>
                            <xsl:value-of select="$output"/>
                        </system-out>
                    </testcase>
                </xsl:for-each>
            </testsuite>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>