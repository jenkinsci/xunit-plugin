<?xml version="1.0" encoding="utf-8"?>
<!--
The MIT License (MIT)

Copyright (c) 2012, Bradley Grainger, Falco Nikolas

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
            <xsl:for-each select=".//assembly">
                <xsl:variable name="package" select="@name"/>
                <!-- Gestion Assembly Set-Up -->
                <xsl:for-each select="set-up">
                    <xsl:variable name="errorsCount">
                        <xsl:choose>
                            <xsl:when test="@result = 'failure'">1</xsl:when>
                            <xsl:otherwise>0</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="errorsCountTearDown">
                        <xsl:choose>
                            <xsl:when test="../tear-down/@result = 'failure'">1</xsl:when>
                            <xsl:otherwise>0</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="tearDownCount">
                        <xsl:choose>
                            <xsl:when test="../tear-down/@result != ''">1</xsl:when>
                            <xsl:otherwise>0</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <testsuite skipped="0" failures="{$errorsCount + $errorsCountTearDown}"
                               errors="{$errorsCount + $errorsCountTearDown}" time="{xunit:junit-time(@duration)}"
                               tests="{1+$tearDownCount}" name="{$package}.AssemblyInitialize">
                        <testcase classname="{$package}.AssemblyInitialize" name="AssemblyInitialize.SetUp"
                                  time="{xunit:junit-time(@duration)}">
                            <xsl:if test="@result = 'failure'">
                                <failure type="{exception/@type}" message="{exception/message}">
                                    <xsl:value-of select="exception/stack-trace"/>
                                </failure>
                                <system-out>
                                    <xsl:value-of select="console-out"/>
                                </system-out>
                                <system-err>
                                    <xsl:value-of select="console-error"/>
                                </system-err>
                            </xsl:if>
                        </testcase>
                        <xsl:for-each select="../tear-down">
                            <xsl:variable name="className" select="@name"/>
                            <!--<xsl:variable name="testNameDown" select="substring-after(@name, concat($package , '.'))" />-->
                            <testcase classname="{$package}.AssemblyInitialize" name="AssemblyInitialize.TearDown"
                                      time="{xunit:junit-time(@duration)}">
                                <xsl:if test="@result = 'failure'">
                                    <failure type="{exception/@type}" message="{exception/message}">
                                        <xsl:value-of select="exception/stack-trace"/>
                                    </failure>
                                    <system-out>
                                        <xsl:value-of select="console-out"/>
                                    </system-out>
                                    <system-err>
                                        <xsl:value-of select="console-error"/>
                                    </system-err>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                </xsl:for-each>
                <!-- Gestion des TU -->
                <xsl:for-each select=".//fixture">
                    <xsl:variable name="className" select="@name"/>
                    <testsuite skipped="{counter/@skip-count + counter/@ignore-count}"
                               failures="{counter/@failure-count}" errors="{counter/@failure-count}"
                               time="{counter/xunit:junit-time(@duration)}" tests="{counter/@run-count}" name="{@type}">
                        <!-- Setup -->
                        <xsl:for-each select=".//set-up">
                            <testcase classname="{$package}.{$className}" name="{@name}" time="{xunit:junit-time(@duration)}">
                                <xsl:if test="@result = 'failure'">
                                    <failure type="{exception/@type}" message="{exception/message}">
                                        <xsl:value-of select="exception/stack-trace"/>
                                    </failure>
                                    <system-out>
                                        <xsl:value-of select="console-out"/>
                                    </system-out>
                                    <system-err>
                                        <xsl:value-of select="console-error"/>
                                    </system-err>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                        <!-- Liste des tests -->
                        <!--<xsl:for-each select="runs/run[@result != 'ignore']">-->
                        <xsl:for-each select="runs/run">
                            <!-- testsuite "package" attribute is actually ignore, package really comes from classname, if it is qualified-->
                            <testcase classname="{$package}.{$className}" name="{@name}" time="{xunit:junit-time(@duration)}">
                                <xsl:if test="@result = 'failure'">
                                    <failure type="{exception/@type}" message="{exception/message}">
                                        <xsl:value-of select="exception/stack-trace"/>
                                    </failure>
                                    <system-out>
                                        <xsl:value-of select="console-out"/>
                                    </system-out>
                                    <system-err>
                                        <xsl:value-of select="console-error"/>
                                    </system-err>
                                </xsl:if>
                                <xsl:if test="@result = 'ignore'">
                                    <skipped/>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                        <!-- Tear Down -->
                        <xsl:for-each select=".//tear-down">
                            <testcase classname="{$package}.{$className}" name="{@name}" time="{xunit:junit-time(@duration)}">
                                <xsl:if test="@result = 'failure'">
                                    <failure type="{exception/@type}" message="{exception/message}">
                                        <xsl:value-of select="exception/stack-trace"/>
                                    </failure>
                                    <system-out>
                                        <xsl:value-of select="console-out"/>
                                    </system-out>
                                    <system-err>
                                        <xsl:value-of select="console-error"/>
                                    </system-err>
                                </xsl:if>
                            </testcase>
                        </xsl:for-each>
                    </testsuite>
                </xsl:for-each>
            </xsl:for-each>
        </testsuites>
    </xsl:template>
</xsl:stylesheet>
