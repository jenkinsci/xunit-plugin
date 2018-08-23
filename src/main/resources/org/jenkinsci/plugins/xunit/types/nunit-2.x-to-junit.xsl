<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2018, Falco Nikolas

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


    <xsl:template match="/test-results">
        <testsuites>
            <xsl:for-each select="//test-suite">
                <xsl:choose>
                    <xsl:when test="results/test-case">
                        <xsl:apply-templates select="current()"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
        </testsuites>
    </xsl:template>

    <xsl:template match="test-suite">
        <xsl:variable name="nunitVersion">
            <xsl:choose>
                <xsl:when test="/test-results/environment">
                    <xsl:analyze-string regex="^[^0-9]*(\d+\.\d+)" select="/test-results/environment/@nunit-version">
                        <xsl:matching-substring>
                            <xsl:value-of select="regex-group(1)" />
                        </xsl:matching-substring>
                    </xsl:analyze-string>
                </xsl:when>
                <xsl:otherwise>
                    <!-- default to 2.4 because environment element miss only in the XSD but in 2.4 the behaviour of suite name is the same of version >= 2.5 -->
                    <xsl:value-of select="2.4" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- get the name of all ancestor test-suite expect the first one that is the assembly name or parametric suites) -->
        <xsl:variable name="suiteName">
            <xsl:choose>
                <xsl:when test="$nunitVersion > 2.2">
                    <xsl:variable name="suiteNames" select="(./ancestor::test-suite[. != /test-results/test-suite and (not(@type) or (@type!='GenericFixture' and @type!='ParameterizedFixture' and @type!='Assembly'))]/@name, @name)" />
                    <xsl:value-of select="string-join($suiteNames, '.')" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@name" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="testCount">
            <xsl:value-of select="count(*/test-case)" />
        </xsl:variable>

        <xsl:variable name="skippedCount">
            <xsl:choose>
                <xsl:when test="not(@type)">
                    <xsl:value-of select="count(*/test-case[@executed='False']/reason)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="count(*/test-case[@result='Ignored'])" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="failureCount">
            <xsl:choose>
                <xsl:when test="not(@type)">
                    <xsl:value-of select="count(*/test-case[@executed='True' and @success='False'])" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="count(*/test-case[@result='Failure'])" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="errorCount">
            <xsl:value-of select="count(*/test-case[@result='NotRunnable']) + count(*/test-case[@result='Error'])" />
        </xsl:variable>

        <testsuite name="{$suiteName}"
                   tests="{$testCount}"
                   failures="{$failureCount}"
                   errors="{$errorCount}"
                   skipped="{$skippedCount}">

            <xsl:if test="@time">
                <xsl:attribute name="time">
                    <xsl:value-of select="xunit:junit-time(@time)"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="categories">
                <xsl:attribute name="group">
                    <xsl:value-of select="categories/category/@name" />
                </xsl:attribute>
            </xsl:if>

            <xsl:for-each select="*/test-case">
                <xsl:variable name="testName">
                    <xsl:choose>
                        <xsl:when test="starts-with(@name, concat($suiteName, '.'))">
                            <xsl:value-of select="substring(@name, string-length($suiteName) + 2)"/>
                        </xsl:when>
                        <xsl:when test="starts-with(@name, $suiteName)">
                            <xsl:value-of select="substring(@name, string-length($suiteName) + 1)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@name"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <testcase classname="{$suiteName}" name="{$testName}">
                    <xsl:if test="@time!=''">
                        <xsl:attribute name="time">
                            <xsl:value-of select="xunit:junit-time(@time)"/>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="categories">
                        <xsl:attribute name="group">
                            <xsl:value-of select="categories/category/@name" />
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:choose>
                        <xsl:when test="@result='Failure' or (not(@result) and failure)">
                            <failure message="{failure/message}">
                                <xsl:value-of select="failure/stack-trace"/>
                            </failure>
                        </xsl:when>
                        <xsl:when test="@result='NotRunnable' or @result='Error'">
                            <xsl:element name="error">
                                <xsl:if test="reason and reason/message/text()">
                                    <xsl:attribute name="message" select="reason/message" />
                                </xsl:if>
                                <xsl:if test="failure">
                                    <xsl:if test="failure/message and failure/message/text()">
                                        <xsl:attribute name="message" select="failure/message"/>
                                    </xsl:if>
                                    <xsl:value-of select="failure/stack-trace"/>
                                </xsl:if>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="@result='Ignored' or (not(@result) and reason)">
                            <xsl:element name="skipped">
                                <xsl:if test="reason and reason/message/text()">
                                    <xsl:attribute name="message" select="reason/message" />
                                </xsl:if>
                            </xsl:element>
                        </xsl:when>
                    </xsl:choose>
                </testcase>
            </xsl:for-each>
        </testsuite>
    </xsl:template>
</xsl:stylesheet>