<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <xsl:element name="testsuite">
            <xsl:attribute name="errors">
                <xsl:value-of select="TestRun/Statistics/Errors"/>
            </xsl:attribute>

            <xsl:attribute name="failures">
                <xsl:value-of select="TestRun/Statistics/Failures"/>
            </xsl:attribute>

            <xsl:attribute name="tests">
                <xsl:value-of select="TestRun/Statistics/Tests"/>
            </xsl:attribute>

            <xsl:attribute name="name">cppunit</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="/TestRun/SuccessfulTests/Test">
        <xsl:call-template name="successTestCase"/>
    </xsl:template>

    <xsl:template match="/TestRun/FailedTests/FailedTest">
        <xsl:call-template name="failureOrErrorTestCase"/>
    </xsl:template>

    <xsl:template match="/TestRun/FailedTests/Test">
        <xsl:call-template name="failureOrErrorTestCase"/>
    </xsl:template>
    <xsl:template match="/TestRun/SkippedTests/Test">
        <xsl:call-template name="skippedTestCase"/>
    </xsl:template>


    <xsl:template name="skippedTestCase">

        <xsl:element name="testcase">
            <xsl:choose>
                <xsl:when test="contains(Name, '::')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:when test="contains(Name, '.')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>

                </xsl:when>

                <xsl:otherwise>
                    <xsl:attribute name="classname">TestClass</xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="Name"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:element name="skipped"/>
        </xsl:element>
    </xsl:template>


    <xsl:template name="successTestCase">

        <xsl:element name="testcase">
            <xsl:choose>
                <xsl:when test="contains(Name, '::')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:when test="contains(Name, '.')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:attribute name="classname">TestClass</xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="Name"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>


    <xsl:template name="failureOrErrorTestCase">

        <xsl:element name="testcase">
            <xsl:choose>
                <xsl:when test="contains(Name, '::')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '::')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:when test="contains(Name, '.')">
                    <xsl:attribute name="classname">
                        <xsl:value-of select="substring-before(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="substring-after(Name, '.')"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:attribute name="classname">TestClass</xsl:attribute>
                    <xsl:attribute name="name">
                        <xsl:value-of select="Name"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="not(Time)">
                            <xsl:attribute name="time">0</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="time">
                                <xsl:value-of select="Time"/>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="FailureType='Error'">
                    <xsl:element name="error">
                        <xsl:attribute name="message">
                            <xsl:value-of select=" normalize-space(Message)"/>
                        </xsl:attribute>
                        <xsl:attribute name="type">
                            <xsl:value-of select="FailureType"/>
                        </xsl:attribute>
                        <xsl:value-of select="normalize-space(Message)"/>
                    </xsl:element>

                    <xsl:element name="system-err">
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[File] - </xsl:text><xsl:value-of select="Location/File"/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Line] - </xsl:text><xsl:value-of select="Location/Line"/>
                        <xsl:text>&#13;</xsl:text>
                    </xsl:element>
                </xsl:when>

                <xsl:otherwise>
                    <xsl:element name="failure">
                        <xsl:attribute name="message">
                            <xsl:value-of select=" normalize-space(Message)"/>
                        </xsl:attribute>
                        <xsl:attribute name="type">
                            <xsl:value-of select="FailureType"/>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="system-err">
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[File] - </xsl:text><xsl:value-of select="Location/File"/>
                        <xsl:text>&#13;</xsl:text>
                        <xsl:text>[Line] - </xsl:text><xsl:value-of select="Location/Line"/>
                        <xsl:text>&#13;</xsl:text>
                    </xsl:element>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:element>
    </xsl:template>

    <xsl:template match="text()|@*"/>
</xsl:stylesheet>
