<?xml version="1.0" encoding="UTF-8"?>
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


    <xsl:template match="testsuite">

        <xsl:if test="testcase">

            <xsl:element name="testsuite">

                <xsl:attribute name="name">
                    <xsl:value-of select="@name"/>
                </xsl:attribute>

                <xsl:attribute name="tests">
                    <xsl:value-of select="@tests"/>
                </xsl:attribute>

                <xsl:attribute name="failures">
                    <xsl:value-of select="@failures"/>
                </xsl:attribute>

                <xsl:attribute name="errors">
                    <xsl:value-of select="@errors"/>
                </xsl:attribute>

                <xsl:attribute name="skipped">
                    <xsl:value-of select="xunit:if-empty(@skipped, xunit:if-empty(@skip, '0'))" />
                </xsl:attribute>

                <xsl:attribute name="time">
                    <xsl:value-of select="xunit:junit-time(@time)"/>
                </xsl:attribute>

                <xsl:if test="string(@timestamp) != ''">
                    <xsl:attribute name="timestamp">
                        <xsl:value-of select="@timestamp" />
                    </xsl:attribute>
                </xsl:if>

                <xsl:if test="properties">
                    <xsl:element name="properties">
                        <xsl:for-each select="properties/property">
                            <xsl:element name="property">

                                <xsl:attribute name="name">
                                    <xsl:value-of select="@name"/>
                                </xsl:attribute>

                                <xsl:attribute name="value">
                                    <xsl:value-of select="@value"/>
                                </xsl:attribute>

                            </xsl:element>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:if>

                <xsl:for-each select="testcase">
                    <xsl:element name="testcase">

                        <xsl:if test="@class or @classname">
                            <xsl:attribute name="classname">
                                <xsl:value-of select="xunit:if-empty(@class, xunit:if-empty(@classname, '0'))"/>
                            </xsl:attribute>
                        </xsl:if>

                        <xsl:attribute name="name">
                            <xsl:value-of select="@name"/>
                        </xsl:attribute>

                        <xsl:attribute name="time">
                            <xsl:value-of select="xunit:junit-time(@time)"/>
                        </xsl:attribute>

                        <xsl:if test="error">
                            <xsl:element name="error">
                                <xsl:attribute name="message">
                                    <xsl:value-of select="error/@message"/>
                                </xsl:attribute>

                                <xsl:attribute name="type">
                                    <xsl:value-of select="error/@type"/>
                                </xsl:attribute>

                                <xsl:value-of select="error"/>
                            </xsl:element>
                        </xsl:if>

                        <xsl:if test="warning">
                            <xsl:element name="system-err">
                                <xsl:value-of select="warning"/>
                            </xsl:element>
                        </xsl:if>

                        <xsl:if test="skipped">
                            <xsl:element name="skipped" />
                        </xsl:if>

                        <xsl:if test="failure">
                            <xsl:element name="failure">
                                <xsl:attribute name="message">
                                    <xsl:value-of select="failure/@message"/>
                                </xsl:attribute>

                                <xsl:attribute name="type">
                                    <xsl:value-of select="failure/@type"/>
                                </xsl:attribute>

                                <xsl:value-of select="failure"/>
                            </xsl:element>
                        </xsl:if>

                        <xsl:if test="system-out">
                            <xsl:element name="system-out">
                                <xsl:value-of select="system-out"/>
                            </xsl:element>
                        </xsl:if>

                        <xsl:if test="system-err">
                            <xsl:element name="system-err">
                                <xsl:value-of select="system-err"/>
                            </xsl:element>
                        </xsl:if>

                    </xsl:element>
                </xsl:for-each>

                <xsl:if test="system-out">
                    <xsl:element name="system-out">
                        <xsl:value-of select="system-out"/>
                    </xsl:element>
                </xsl:if>

                <xsl:if test="system-err">
                    <xsl:element name="system-err">
                        <xsl:value-of select="system-err"/>
                    </xsl:element>
                </xsl:if>

            </xsl:element>

        </xsl:if>

        <xsl:apply-templates select="testsuite"/>


    </xsl:template>


    <xsl:template match="testsuites">
        <xsl:element name="testsuites">
            <xsl:apply-templates select="testsuite"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
