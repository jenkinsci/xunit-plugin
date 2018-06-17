<?xml version="1.0" encoding="utf-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2018, Gregory Boissinot, Falco Nikolas

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
<!-- for details interpreting unit test results http://qt-project.org/wiki/Writing_Unit_Tests -->
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

    <!-- misc variables -->
    <xsl:variable name="classname" select="/TestCase/@name"/>
    <xsl:variable name="total-tests" select="count(/TestCase/TestFunction)"/>
    <xsl:variable name="total-skipped" select="count(/TestCase/TestFunction/Message[@type = 'skip'])"/>
    <xsl:variable name="total-failures" select="count(/TestCase/TestFunction/Incident[@type = 'fail' and position() = 1])"/>
    <xsl:variable name="total-errors" select="0"/>

    <!-- main template call -->
    <xsl:template match="/">
        <xsl:apply-templates select="TestCase"/>
    </xsl:template>

    <xsl:template match="TestCase">
        <xsl:variable name="msecsTest">
            <xsl:choose>
                <xsl:when test="Duration">
                    <xsl:value-of select="translate(Duration/@msecs,',','.')"/>
                </xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <testsuite name="{$classname}"
                   tests="{$total-tests}"
                   failures="{$total-failures}"
                   errors="{$total-errors}"
                   skipped="{$total-skipped}"
                   time="{xunit:junit-time($msecsTest div 1000)}">
            <xsl:apply-templates select="Environment"/>
            <xsl:apply-templates select="TestFunction"/>
        </testsuite>
    </xsl:template>

    <xsl:template match="Environment">
        <properties>
            <xsl:for-each select="*">
                <property name="{name()}" value="{text()}"/>
            </xsl:for-each>
        </properties>
    </xsl:template>

    <xsl:template match="TestFunction">
        <xsl:variable name="msecsFunction">
            <xsl:choose>
                <xsl:when test="Duration">
                    <xsl:value-of select="translate(Duration/@msecs,',','.')"/>
                </xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="testName" select="@name" />

        <testcase classname="{$classname}" name="{$testName}" time="{xunit:junit-time($msecsFunction div 1000)}">
            <!-- we need to use choose here, because jenkins cannot not handle fail and afterwards skip -->
            <xsl:choose>
                <!-- handle fail -->
                <xsl:when test="Incident[@type = 'fail']">
                    <xsl:variable name="description">
                        <xsl:value-of select="Incident[@type='fail']/Description"/>
                    </xsl:variable>
                    <!-- display a reasonable error message -->
                    <xsl:element name="failure">
                        <xsl:attribute name="message">
                            <xsl:for-each select="Incident[@type = 'fail']">
                                <xsl:apply-templates>
                                    <xsl:with-param name="testName" select="$testName" />
                                </xsl:apply-templates>
                            </xsl:for-each>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
                <!-- handle skip -->
                <xsl:when test="Message[@type = 'skip']">
                    <xsl:variable name="description">
                        <xsl:value-of select="Message[@type='skip']/Description"/>
                    </xsl:variable>
                    <xsl:element name="skipped">
                        <xsl:attribute name="message">
                            <xsl:value-of select="$description"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="display-system-out">
                <xsl:with-param name="testName" select="@name" />
            </xsl:call-template>
        </testcase>

    </xsl:template>

    <xsl:template name="log">
        <xsl:param name="testName" />

        <!-- will be used to generate "nice" error message -->
        <xsl:variable name="file" select="@file"/>
        <xsl:variable name="line" select="@line"/>
        <xsl:variable name="description">
            <xsl:value-of select="Description"/>
        </xsl:variable>
        <xsl:variable name="datatag">
            <xsl:value-of select="DataTag"/>
        </xsl:variable>
        <xsl:text>&#10;</xsl:text>
        <xsl:value-of select="concat('[', upper-case(@type), '] ', $testName)"/>
        <xsl:if test="xunit:is-empty($datatag)"><xsl:value-of select="concat('(', $datatag, ') ')"/></xsl:if>
        <xsl:value-of select="$description"/>
        <xsl:if test="xunit:is-empty($file)"><xsl:value-of select="concat(' at ', $file, '(', $line, ')')"/></xsl:if>
    </xsl:template>

    <xsl:template name="display-system-out">
        <xsl:param name="testName" />

        <xsl:if test="count(Incident[@type!='fail' or @type!='skip']/DataTag | Incident[@type!='fail' or @type!='skip']/Description | Message) > 0">
            <system-out>
                <xsl:for-each select="Incident[@type!='fail' or @type!='skip'] | Message">
                    <xsl:apply-templates select=".">
                        <xsl:with-param name="testName" select="$testName" />
                    </xsl:apply-templates>
                </xsl:for-each>
            </system-out>
        </xsl:if>
    </xsl:template>

    <xsl:template name="display-system-err">
        <system-err/>
    </xsl:template>

    <xsl:template match="Incident">
        <xsl:param name="testName" />

        <xsl:if test="DataTag | Description">
            <xsl:call-template name="log">
                <xsl:with-param name="testName" select="concat($classname, '::', $testName)" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Message">
        <xsl:if test="Description">
            <xsl:call-template name="log" />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>