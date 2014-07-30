<?xml version="1.0" encoding="UTF-8"?>
<!--
/*******************************************************************************
* Copyright (c) 2011 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot, Aravindan Mahendran                              *
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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>


    <xsl:template match="/">
        <testsuite name="{ResultsSession/Exec/Summary/Projects/Project/@name}" time="0"
                   tests="{Summary/Projects/Project/@testCases}"
                   failures="{ResultsSession/Exec/Summary/Projects/Project/@fail}">
            <xsl:apply-templates select="ResultsSession/Exec"></xsl:apply-templates>
            <xsl:apply-templates select="ResultsSession/ExecutedTestsDetails"></xsl:apply-templates>
        </testsuite>
    </xsl:template>

    <xsl:template match="Exec">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Goals">
        <properties>
            <xsl:apply-templates select="Goal"/>
        </properties>
    </xsl:template>

    <xsl:template match="Goal">
        <property name="{@name}" value="{@type}"/>
    </xsl:template>

    <xsl:template match="ExecViols">
        <xsl:apply-templates select="ExecViol"/>
    </xsl:template>

    <xsl:template match="ExecViol">
        <xsl:if test="@cat!=6">
            <testcase classname="{@locFile}" name="{@testName}" time="0">
                <xsl:apply-templates select="Thr"/>
            </testcase>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Thr">
        <xsl:apply-templates select="ThrPart"/>
    </xsl:template>

    <xsl:template match="ThrPart">
        <failure type="{@clName}" message="{@detMsg}"/>
        <system-err>
            <xsl:text>Trace </xsl:text>
            <xsl:apply-templates select="Trace"/>
        </system-err>
    </xsl:template>

    <xsl:template match="Trace">
        <xsl:text>Line :</xsl:text>
        <xsl:value-of select="@ln"/>
        <xsl:text>    File :</xsl:text>
        <xsl:value-of select="@fileName"/>
    </xsl:template>

    <xsl:template match="ExecutedTestsDetails">
        <xsl:apply-templates select="Total"/>
    </xsl:template>

    <xsl:template match="Total">
        <xsl:apply-templates select="Project"/>
    </xsl:template>

    <xsl:template match="Project">
        <xsl:apply-templates select="TestSuite"/>
    </xsl:template>

    <xsl:template match="TestSuite">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="Test">
        <xsl:variable name="fullTestName"><xsl:value-of select="/ResultsSession/Exec/Summary/Projects/Project/@name"/>JUnitTestSuite</xsl:variable>
        <xsl:if test="@pass=1">
            <testcase name="{@name}" classname="{$fullTestName}" time="0"/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>