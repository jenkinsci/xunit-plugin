<?xml version="1.0" encoding="UTF-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014, Jan De Bleser (jan at commsquare dot com)

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
    <xsl:output method="xml" indent="yes" encoding="UTF-8" cdata-section-elements="system-out system-err"/>
    <xsl:decimal-format decimal-separator="." grouping-separator=","/>

    <xsl:function name="xunit:junit-time" as="xs:string">
        <xsl:param name="value" as="xs:anyAtomicType?" />

        <xsl:variable name="time" as="xs:double">
            <xsl:choose>
                <xsl:when test="$value instance of xs:double">
                    <xsl:value-of select="$value" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="translate(string($value), ',', '.')" />
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

    <xsl:template match="/">
        <testsuites>
            <xsl:for-each select="TestResults[1]//TestListing[1]//TestSuite">
                <testsuite>
                    <xsl:attribute name="name">
                        <xsl:value-of select="@Name"/>
                    </xsl:attribute>
                    <xsl:attribute name="tests">
                        <xsl:value-of select="@NumberOfRunTests"/>
                    </xsl:attribute>
                    <xsl:attribute name="time">
                        <xsl:value-of select="xunit:junit-time(xunit:millis-from-time(@ElapsedTime))"/>
                    </xsl:attribute>
                    <xsl:attribute name="failures">
                        <xsl:value-of select="@NumberOfFailures"/>
                    </xsl:attribute>
                    <xsl:attribute name="errors">
                        <xsl:value-of select="@NumberOfErrors"/>
                    </xsl:attribute>
                    <xsl:attribute name="skipped">
                        <xsl:value-of select="@NumberOfIgnoredTests"/>
                    </xsl:attribute>
                    <xsl:for-each select="Test">
                        <testcase>
                            <xsl:attribute name="classname">
                                <xsl:value-of select="../@Name"/>
                            </xsl:attribute>
                            <xsl:attribute name="name">
                                <xsl:value-of select="@Name"/>
                            </xsl:attribute>
                            <xsl:attribute name="time">
                                <xsl:value-of select="xunit:junit-time(xunit:millis-from-time(@ElapsedTime))"/>
                            </xsl:attribute>
                            <xsl:if test="@Result='Failed'">
                                <failure>
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="Message"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="type">
                                        <xsl:value-of select="ExceptionClass"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="ExceptionMessage"/>
                                </failure>
                            </xsl:if>
                            <xsl:if test="@Result='Error'">
                                <failure>
                                    <xsl:attribute name="message">
                                        <xsl:value-of select="Message"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="type">
                                        <xsl:value-of select="ExceptionClass"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="SourceUnitName"/> (line <xsl:value-of select="LineNumber"/>):[<xsl:value-of
                                        select="FailedMethodName"/>]:<xsl:value-of select="ExceptionMessage"/>
                                </failure>
                            </xsl:if>
                        </testcase>
                    </xsl:for-each>
                </testsuite>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>