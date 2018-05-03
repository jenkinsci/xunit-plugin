<?xml version="1.0" encoding="utf-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2014, Gregory Boissinot

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- for details interpreting unit test results http://qt-project.org/wiki/Writing_Unit_Tests -->
    <xsl:output method="xml" indent="yes"/>
    <xsl:decimal-format decimal-separator="." grouping-separator=","/>

    <!-- misc variables -->
    <xsl:variable name="classname" select="/TestCase/@name"/>
    <xsl:variable name="total-tests" select="count(/TestCase/TestFunction)"/>
    <xsl:variable name="total-failures"
                  select="count(/TestCase/TestFunction/Incident[@type='fail'])+count(/TestCase/TestFunction/Incident[@type='xpass'])"/>

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
        <testsuite name="{$classname}" tests="{$total-tests}" failures="{$total-failures}" errors="0"
                   time="{format-number($msecsTest div 1000,'0.000')}">
            <xsl:apply-templates select="Environment"/>
            <xsl:apply-templates select="TestFunction"/>
            <xsl:call-template name="display-system-out"/>
            <xsl:call-template name="display-system-err"/>
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
        <testcase classname="{$classname}" name="{@name}" time="{format-number($msecsFunction div 1000,'0.000')}">
            <!-- we need to use choose here, because jenkins cannot not handle fail and afterwards skip -->
            <xsl:choose>
                <!-- handle fail -->
                <xsl:when test="Incident/@type = 'fail'">
                    <!-- will be used to generate "nice" error message -->
                    <xsl:variable name="file" select="Incident[@type='fail']/@file"/>
                    <xsl:variable name="line" select="Incident[@type='fail']/@line"/>
                    <xsl:variable name="description">
                        <xsl:value-of select="Incident[@type='fail']/Description"/>
                    </xsl:variable>
                    <xsl:variable name="datatag">
                        <xsl:value-of select="Incident[@type='fail']/DataTag"/>
                    </xsl:variable>
                    <!-- display a reasonable error message -->
                    <xsl:element name="failure">
                        <xsl:attribute name="type">failure</xsl:attribute>
                        <xsl:attribute name="message">
                            <xsl:value-of select="concat($file,':',$line,' :: [',$datatag,'] ',$description)"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
                <!-- handle skip -->
                <xsl:when test="Message/@type = 'skip'">
                    <!-- will be used to generate "nice" error message -->
                    <xsl:variable name="file" select="Message[@type='skip']/@file"/>
                    <xsl:variable name="line" select="Message[@type='skip']/@line"/>
                    <xsl:variable name="description">
                        <xsl:value-of select="Message[@type='skip']/Description"/>
                    </xsl:variable>
                    <xsl:variable name="datatag">
                        <xsl:value-of select="Message[@type='skip']/DataTag"/>
                    </xsl:variable>
                    <!-- display a reasonable skipped message -->
                    <xsl:element name="skipped">
                        <xsl:attribute name="message">
                            <xsl:value-of select="concat($file,':',$line,' :: [',$datatag,'] ',$description)"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>

            <!-- handle xfail -->
            <xsl:if test="Incident/@type = 'xfail'">
                <system-out>
                    <xsl:for-each select="Incident[@type='xfail']">
                        <!-- will be used to generate "nice" error message -->
                        <xsl:variable name="file" select="@file"/>
                        <xsl:variable name="line" select="@line"/>
                        <xsl:variable name="description">
                            <xsl:value-of select="Description"/>
                        </xsl:variable>
                        <xsl:variable name="datatag">
                            <xsl:value-of select="DataTag"/>
                        </xsl:variable>

                        <!-- display a reasonable error message -->
                        <xsl:text>&#10;</xsl:text>
                        <xsl:text disable-output-escaping="yes">&lt;![CDATA[XFAIL : </xsl:text>
                        <xsl:value-of select="concat($file,':',$line,' :: ',$description)"
                                      disable-output-escaping="yes"/>
                        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                    </xsl:for-each>
                </system-out>
            </xsl:if>

            <!-- handle xpass -->
            <xsl:if test="Incident/@type = 'xpass'">
                <system-out>
                    <xsl:for-each select="Incident[@type='xpass']">
                        <!-- will be used to generate "nice" error message -->
                        <xsl:variable name="file" select="@file"/>
                        <xsl:variable name="line" select="@line"/>
                        <xsl:variable name="description">
                            <xsl:value-of select="Description"/>
                        </xsl:variable>

                        <!-- display a reasonable error message -->
                        <xsl:text>&#10;</xsl:text>
                        <xsl:text disable-output-escaping="yes">&lt;![CDATA[XPASS : </xsl:text>
                        <xsl:value-of select="concat($file,':',$line,' :: ',$description)"
                                      disable-output-escaping="yes"/>
                        <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                    </xsl:for-each>
                </system-out>
            </xsl:if>

            <!-- handle pass -->
            <xsl:if test="Incident/@type = 'pass'">
                <xsl:if test="Message[@type='qdebug'] | Message[@type='qwarn'] | Message[@type='warn']">
                    <system-err>
                        <xsl:for-each select="Message[@type='qdebug'] | Message[@type='qwarn'] | Message[@type='warn']">
                            <xsl:choose>
                                <xsl:when test="@type='qdebug'">
                                    <xsl:text>&#10;</xsl:text>
                                    <xsl:text disable-output-escaping="yes">&lt;![CDATA[QDEBUG : </xsl:text>
                                    <xsl:value-of select="Description" disable-output-escaping="yes"/>
                                    <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                                </xsl:when>
                                <xsl:when test="@type='qwarn'">
                                    <xsl:text>&#10;</xsl:text>
                                    <xsl:text disable-output-escaping="yes">&lt;![CDATA[QWARN : </xsl:text>
                                    <xsl:value-of select="Description" disable-output-escaping="yes"/>
                                    <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                                </xsl:when>
                                <xsl:when test="@type='warn'">
                                    <xsl:text>&#10;</xsl:text>
                                    <xsl:text disable-output-escaping="yes">&lt;![CDATA[WARNING : </xsl:text>
                                    <xsl:value-of select="Description" disable-output-escaping="yes"/>
                                    <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
                                </xsl:when>
                            </xsl:choose>
                        </xsl:for-each>
                    </system-err>
                </xsl:if>
            </xsl:if>
        </testcase>

    </xsl:template>

    <xsl:template name="display-system-out">
        <system-out/>
    </xsl:template>

    <xsl:template name="display-system-err">
        <system-err/>
    </xsl:template>

</xsl:stylesheet>