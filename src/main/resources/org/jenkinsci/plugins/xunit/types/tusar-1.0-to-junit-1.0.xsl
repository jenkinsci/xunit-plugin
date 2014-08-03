<?xml version="1.0" encoding="UTF-8"?>
<!--
/*******************************************************************************
* Copyright (c) 2009 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot                                                   *
*                                                                              *
* Permission is hereby granted, free of charge, to any person obtaining a copy *
* of this software and associated documentation files (the "Software"), to deal*
* in the Software without restriction, including without limitation the rights *
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
* copies of the Software, and to permit persons to whom the Software is        *
* furnished to do so, subject to the following conditions:                     *
*                                                                              *
* The above copyright notice and this permission notice shall be included in   *
* all copies or substantial portions of the Software.                          *
*                                                                              *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
* THE SOFTWARE.                                                                *
*******************************************************************************/
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        >
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">

        <xsl:element name="testsuites">

            <xsl:for-each select="tusar/tests/testsuite">

                <xsl:element name="testsuite">

                    <xsl:attribute name="name">
                        <xsl:value-of select="@name"/>
                    </xsl:attribute>

                    <xsl:attribute name="tests">
                        <xsl:value-of select="@tests"/>
                    </xsl:attribute>

                    <xsl:attribute name="failures">
                        <xsl:if test="@failures">
                            <xsl:value-of select="@failures"/>
                        </xsl:if>
                        <xsl:if test="not(@failures)">
                            <xsl:value-of select="0"/>
                        </xsl:if>
                    </xsl:attribute>

                    <xsl:attribute name="errors">
                        <xsl:if test="@failures">
                            <xsl:value-of select="@errors"/>
                        </xsl:if>
                        <xsl:if test="not(@errors)">
                            <xsl:value-of select="0"/>
                        </xsl:if>
                    </xsl:attribute>

                    <xsl:attribute name="skipped">
                        <xsl:if test="@skipped">
                            <xsl:value-of select="@skipped"/>
                        </xsl:if>
                        <xsl:if test="not(@skipped)">
                            <xsl:value-of select="0"/>
                        </xsl:if>
                    </xsl:attribute>

                    <xsl:attribute name="time">
                        <xsl:if test="@time">
                            <xsl:value-of select="@time"/>
                        </xsl:if>
                        <xsl:if test="not(@time)">
                            <xsl:value-of select="0"/>
                        </xsl:if>
                    </xsl:attribute>

                    <xsl:attribute name="timestamp">
                        <xsl:if test="@timestamp">
                            <xsl:value-of select="@timestamp"/>
                        </xsl:if>
                        <xsl:if test="not(@timestamp)">
                            <xsl:value-of select="0"/>
                        </xsl:if>
                    </xsl:attribute>

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

                            <xsl:choose>
                                <xsl:when test="@fulltestname">
                                    <xsl:attribute name="classname">
                                        <xsl:value-of select="@fulltestname"/>
                                    </xsl:attribute>
                                </xsl:when>
                                <xsl:when test="@filepath">
                                    <xsl:attribute name="classname">
                                        <xsl:value-of select="@filepath"/>
                                    </xsl:attribute>
                                </xsl:when>
                            </xsl:choose>


                            <xsl:attribute name="name">
                                <xsl:value-of select="@testname"/>
                            </xsl:attribute>

                            <xsl:attribute name="time">

                                <xsl:if test="@time">
                                    <xsl:value-of select="@time"/>
                                </xsl:if>
                                <xsl:if test="not(@time)">
                                    <xsl:value-of select="0"/>
                                </xsl:if>

                            </xsl:attribute>

                            <xsl:attribute name="assertions">


                                <xsl:if test="@assertions">
                                    <xsl:value-of select="@assertions"/>
                                </xsl:if>
                                <xsl:if test="not(@assertions)">
                                    <xsl:value-of select="0"/>
                                </xsl:if>


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
            </xsl:for-each>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>