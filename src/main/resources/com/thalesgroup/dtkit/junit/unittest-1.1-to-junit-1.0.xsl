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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <testsuite>
            <xsl:attribute name="errors">
                <xsl:value-of select="unittest-results/@failedtests"/>
            </xsl:attribute>

            <xsl:attribute name="failures">
                <xsl:value-of select="unittest-results/@failures"/>
            </xsl:attribute>

            <xsl:attribute name="tests">
                <xsl:value-of select="unittest-results/@tests"/>
            </xsl:attribute>

            <xsl:attribute name="name">unittest</xsl:attribute>

            <xsl:if test="unittest-results/@time">
                <xsl:attribute name="timestamp">
                    <xsl:value-of select="unittest-results/@time"/>
                </xsl:attribute>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="count(/unittest-results/test)>1">
                    <!--<xsl:call-template name="testCaseProcess">-->
                    <!--<xsl:with-param name="currentNode" select="."/>-->
                    <!--</xsl:call-template>-->
                    <xsl:for-each select="/unittest-results/test">
                        <xsl:call-template name="testCaseProcess">
                            <xsl:with-param name="currentNode" select="current()"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="unittest-results/@time">
                    <xsl:call-template name="testCaseProcessWithTestTimeParam">
                        <xsl:with-param name="currentNode" select="/unittest-results/test"/>
                        <xsl:with-param name="testTimeParam" select="unittest-results/@time"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="/unittest-results/test">
                        <xsl:call-template name="testCaseProcess">
                            <xsl:with-param name="currentNode" select="current()"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>

            <!-- <xsl:apply-templates/> -->
        </testsuite>
    </xsl:template>

    <xsl:template name="testCaseProcess">
        <xsl:param name="currentNode"/>
        <testcase>
            <xsl:attribute name="classname">
                <xsl:value-of select="$currentNode/@suite"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="$currentNode/@name"/>
            </xsl:attribute>
            <xsl:if test="$currentNode/@time">
                <xsl:attribute name="time">
                    <xsl:value-of select="$currentNode/@time"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="$currentNode/child::*"/>
        </testcase>
    </xsl:template>

    <xsl:template name="testCaseProcessWithTestTimeParam">
        <xsl:param name="currentNode"/>
        <xsl:param name="testTimeParam"/>
        <testcase>
            <xsl:attribute name="classname">
                <xsl:value-of select="$currentNode/@suite"/>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="$currentNode/@name"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$currentNode/@time">
                    <xsl:attribute name="time">
                        <xsl:value-of select="$currentNode/@time"/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:when test="$testTimeParam">
                    <xsl:attribute name="time">
                        <xsl:value-of select="$testTimeParam"/>
                    </xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <xsl:copy-of select="$currentNode/child::*"/>
        </testcase>
    </xsl:template>

</xsl:stylesheet>