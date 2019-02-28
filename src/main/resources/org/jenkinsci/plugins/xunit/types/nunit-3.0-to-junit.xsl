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
    <xsl:output method="xml" indent="yes" encoding="UTF-8" cdata-section-elements="system-out system-err failure error"/>
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
        <testsuites>
            <xsl:for-each select="//test-suite">
                <xsl:apply-templates select="current()" />
            </xsl:for-each>
        </testsuites>
    </xsl:template>

    <xsl:template match="test-suite[@type = 'ParameterizedMethod']" />

    <xsl:template match="test-suite[@type != 'ParameterizedMethod']">
        <xsl:if test="test-case or count(test-suite[@type = 'ParameterizedMethod']/test-case) > 0">
            <xsl:variable name="suiteName" select="@fullname" />
            <testsuite name="{$suiteName}"
                       tests="{@total}"
                       failures="{@failed}"
                       errors="{@inconclusive}"
                       skipped="{@skipped}">

                <xsl:if test="@duration">
                    <xsl:attribute name="time">
                        <xsl:value-of select="xunit:junit-time(@duration)"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="properties/property[@name='Category']">
                    <xsl:attribute name="group">
                        <xsl:value-of select="properties/property[@name='Category']/@value" />
                    </xsl:attribute>
                </xsl:if>

                <xsl:for-each select="test-case">
                    <xsl:call-template name="test-case">
                        <xsl:with-param name="suiteName" select="$suiteName" />
                    </xsl:call-template>
                </xsl:for-each>

                <xsl:for-each select="test-suite[@type = 'ParameterizedMethod']/test-case">
                    <xsl:call-template name="test-case">
                        <xsl:with-param name="suiteName" select="$suiteName" />
                    </xsl:call-template>
                </xsl:for-each>
            </testsuite>
        </xsl:if>
    </xsl:template>

    <xsl:template name="test-case">
        <xsl:param name="suiteName" />

        <xsl:variable name="testName">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <testcase classname="{$suiteName}" name="{$testName}">
            <xsl:if test="@duration!=''">
                <xsl:attribute name="time">
                    <xsl:value-of select="xunit:junit-time(@duration)" />
                </xsl:attribute>
            </xsl:if>

            <xsl:if test="properties/property[@name='Category']">
                <xsl:attribute name="group">
                    <xsl:value-of select="properties/property[@name='Category']/@value" />
                </xsl:attribute>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="@result='Failed'">
                    <failure message="{failure/message}">
                        <xsl:value-of select="failure/stack-trace"/>
                    </failure>
                    <xsl:if test="output/text()">
                        <system-out>
                            <xsl:value-of select="output/text()"/>
                        </system-out>
                    </xsl:if>
                </xsl:when>
                <xsl:when test="@result='Inconclusive' or @result='Error'">
                    <xsl:element name="error">
                        <xsl:choose>
                            <xsl:when test="reason and reason/message/text()">
                                <xsl:attribute name="message" select="reason/message" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="message">Inconclusive test</xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:element>
                </xsl:when>
                <xsl:when test="@result='Skipped'">
                    <xsl:element name="skipped">
                        <xsl:if test="reason and reason/message/text()">
                            <xsl:attribute name="message" select="reason/message" />
                        </xsl:if>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
        </testcase>
    </xsl:template>
</xsl:stylesheet>
